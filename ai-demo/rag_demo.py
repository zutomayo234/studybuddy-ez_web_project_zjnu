"""
实验五 · RAG（检索增强生成）入门示例
=====================================
完整链路：文档切块 → 向量化(简化) → 检索 Top-K → 拼入提示 → 基于资料作答。

说明：教学环境不强制调用 Embedding 接口，这里用"中文二元组 + 停用词过滤"的
稀疏关键词检索来近似语义检索；生产环境应替换为 Embedding + 向量库（Milvus/pgvector）。

运行：
    python rag_demo.py "密码能不能明文存库"
"""

import math
import os
import sys

from llm_demo import ask

# 知识库文件（复用后端的课程知识库）
KB_PATH = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "studybuddy-backend", "src", "main", "resources", "kb", "course-faq.txt",
)

# 与后端 RagService 保持一致的停用词策略：把虚词排除出相关性判定
STOP_WORDS = {
    "怎么", "什么", "哪些", "哪个", "如何", "是否", "可以", "能不能", "能否",
    "是不是", "有没有", "为什么", "因为", "所以", "但是", "然后", "如果",
    "这个", "那个", "我们", "你们", "他们", "自己", "一个", "一些", "一下",
    "请问", "告诉", "帮我", "给我", "想要", "希望", "进行", "对于", "关于",
}


def tokenize(text: str) -> set:
    """中文按单字 + 二元组切分，英文/数字按小写单词切分。"""
    tokens, chinese, word = set(), [], []
    for ch in text.lower():
        if "\u4e00" <= ch <= "\u9fff":
            if word:
                tokens.add("".join(word))
                word = []
            chinese.append(ch)
        elif ch.isalnum():
            if chinese:
                tokens |= _cn_tokens("".join(chinese))
                chinese = []
            word.append(ch)
        else:
            if word:
                tokens.add("".join(word))
                word = []
            if chinese:
                tokens |= _cn_tokens("".join(chinese))
                chinese = []
    if word:
        tokens.add("".join(word))
    if chinese:
        tokens |= _cn_tokens("".join(chinese))
    return tokens


def _cn_tokens(segment: str) -> set:
    out = set()
    for i, ch in enumerate(segment):
        out.add(ch)
        if i + 1 < len(segment):
            out.add(segment[i:i + 2])
    return out


def split_chunks(path: str) -> list:
    """按空行切分片段，跳过以 # 开头的注释行。"""
    with open(path, encoding="utf-8") as f:
        text = f.read()
    chunks = []
    for block in text.split("\n\n"):
        block = block.strip()
        if block and not block.startswith("#"):
            chunks.append(block)
    return chunks


def retrieve(question: str, chunks: list, top_k: int = 3) -> list:
    """检索：实义词命中数达到阈值才计入，避免偶发单字造成假命中。"""
    q_tokens = tokenize(question)
    meaningful = [t for t in q_tokens if len(t) >= 2 and t not in STOP_WORDS]
    required = min(2, max(len(meaningful), 1))

    scored = []
    for i, chunk in enumerate(chunks, start=1):
        doc_tokens = tokenize(chunk)
        hit = sum(1 for t in meaningful if t in doc_tokens)
        if hit < required:
            continue
        single_hit = sum(1 for t in q_tokens if len(t) == 1 and t in doc_tokens)
        score = (hit * 2 + single_hit * 0.3) / math.sqrt(max(len(doc_tokens), 1))
        scored.append((score, i, chunk))

    scored.sort(reverse=True, key=lambda x: x[0])
    return [f"第{i}条：{c}" for _, i, c in scored[:top_k]]


def build_prompt(question: str, references: list) -> str:
    context = "\n".join(references) if references else "（无检索结果）"
    return (
        "【参考资料】\n"
        f"{context}\n\n"
        "【用户问题】\n"
        f"{question}\n\n"
        "请基于上述参考资料作答，并注明参考来源：第X条。"
    )


if __name__ == "__main__":
    question = sys.argv[1] if len(sys.argv) > 1 else "密码能不能明文存库？"

    if not os.path.exists(KB_PATH):
        raise SystemExit(f"知识库文件不存在：{KB_PATH}")

    chunks = split_chunks(KB_PATH)
    print(f"知识库切分为 {len(chunks)} 个片段\n")

    refs = retrieve(question, chunks, top_k=3)
    print(f"[提问] {question}")
    print("-" * 70)
    print(f"[检索到 {len(refs)} 条资料]")
    for r in refs:
        print("  " + r)
    print("-" * 70)

    prompt = build_prompt(question, refs)
    print("[最终送入模型的提示词]")
    print(prompt)

    if not os.getenv("LLM_API_KEY"):
        print("\n未配置 LLM_API_KEY，跳过模型调用。")
        print("要点：即使不调用模型，检索结果本身已经能「给出出处」，这正是 RAG 降低幻觉的关键。")
    else:
        print("\n[模型基于资料的回答]")
        print(ask(prompt))
