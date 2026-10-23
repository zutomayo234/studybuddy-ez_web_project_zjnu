"""
实验五 · 提示工程小练习：同一需求的"坏提示 vs 好提示"对比
==========================================================
目的：直观看到"给定需求 → 稳定输出"的差异，理解提示工程的价值。

运行：
    python prompt_engineering_demo.py
说明：本脚本只演示"提示词构造"，不调用模型也可理解；
      若已配置 LLM_API_KEY，会自动对比两者的真实输出。
"""

import os

from llm_demo import ask

REQUIREMENT = "帮我写一个用户注册的接口校验逻辑"

# 坏提示：指令模糊、没有角色、没有约束、没有输出格式
BAD_PROMPT = REQUIREMENT

# 好提示：角色设定 + 约束 + 上下文 + 任务 + 输出格式（五段式）
GOOD_PROMPT = """你是资深 Java 后端工程师，熟悉 Spring Boot 3 与 Jakarta Validation。

【上下文】
项目使用 Spring Boot 3.3 + MyBatis，接口为 RESTful 风格，统一返回 {code,message,data}。

【任务】
为一个用户注册 DTO 编写参数校验规则，至少覆盖：用户名、密码、邮箱、手机号。

【约束】
1. 只用 Jakarta Validation 注解，不引入新依赖；
2. 每个字段的校验失败要有中文提示；
3. 不要输出与任务无关的解释。

【输出格式】
先给一段 Java 代码，再用表格列出"字段 / 校验规则 / 失败提示"。
"""


def show(title: str, prompt: str) -> None:
    print("=" * 70)
    print(f"【{title}】")
    print("-" * 70)
    print(prompt)


if __name__ == "__main__":
    show("坏提示（只有一句话）", BAD_PROMPT)
    show("好提示（五段式结构）", GOOD_PROMPT)

    if not os.getenv("LLM_API_KEY"):
        print("\n未配置 LLM_API_KEY，跳过真实调用。")
        print("配置后可看到：坏提示输出随机、结构不稳；好提示输出稳定且可直接复用。")
    else:
        print("\n" + "=" * 70)
        print("【坏提示的实际输出】")
        print(ask(BAD_PROMPT))
        print("\n" + "=" * 70)
        print("【好提示的实际输出】")
        print(ask(GOOD_PROMPT))
