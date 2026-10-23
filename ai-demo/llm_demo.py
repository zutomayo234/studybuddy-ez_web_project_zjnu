"""
实验五 · 大模型接口调用最小示例
=================================
目标：用 SDK/HTTP 调用兼容 OpenAI 协议的大模型接口，封装一个 ask(question) 函数。

设计说明：
- 只用 Python 标准库 urllib，避免额外依赖，保证在任何机器上都能直接跑；
- API Key 从环境变量 LLM_API_KEY 读取，绝不硬编码（实验四"AI 安全"要求）；
- 兼容通义千问 / DeepSeek / 文心(兼容模式) / 本地 Ollama —— 换 base_url 与 model 即可。

运行：
    set LLM_API_KEY=sk-xxxx            # Windows CMD
    $env:LLM_API_KEY="sk-xxxx"         # Windows PowerShell
    python llm_demo.py "什么是 RAG？"
"""

import json
import os
import sys
import urllib.error
import urllib.request

# ---------- 配置：全部来自环境变量，便于切换厂商 ----------
# 默认对接 DeepSeek 官方；换厂商需同时改 BASE_URL 与 MODEL（Key 要属同一家）
BASE_URL = os.getenv("LLM_BASE_URL", "https://api.deepseek.com")
API_KEY = os.getenv("LLM_API_KEY", "")
MODEL = os.getenv("LLM_MODEL", "deepseek-flash")

# 系统提示：角色设定 + 约束（提示工程四段式中的前两段）
SYSTEM_PROMPT = (
    "你是学习搭子，一名面向大学生的课程学习助手。"
    "回答要简洁、分点、给出可运行的最小代码示例。"
    "资料未覆盖时明确说明不确定，不得编造。"
)


def ask(question: str, system_prompt: str = SYSTEM_PROMPT, temperature: float = 0.3) -> str:
    """封装一个 ask(question) 函数：输入问题，返回模型回答文本。"""
    if not API_KEY:
        raise RuntimeError(
            "未检测到 LLM_API_KEY。请先设置环境变量，例如：\n"
            '  PowerShell:  $env:LLM_API_KEY="sk-xxxx"\n'
            '  CMD:         set LLM_API_KEY=sk-xxxx'
        )

    payload = {
        "model": MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": question},
        ],
        "temperature": temperature,
        "stream": False,
    }

    req = urllib.request.Request(
        url=f"{BASE_URL}/chat/completions",
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {API_KEY}",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            body = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        detail = e.read().decode("utf-8", errors="ignore")
        raise RuntimeError(f"HTTP {e.code} 调用模型失败：{detail}") from e
    except urllib.error.URLError as e:
        raise RuntimeError(f"网络错误：{e.reason}") from e

    return body["choices"][0]["message"]["content"]


if __name__ == "__main__":
    q = sys.argv[1] if len(sys.argv) > 1 else "用一句话解释什么是 RESTful API"
    print(f"[模型] {MODEL}")
    print(f"[提问] {q}")
    print("-" * 60)
    print(ask(q))
