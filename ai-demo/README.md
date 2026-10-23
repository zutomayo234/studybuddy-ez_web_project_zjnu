# 实验五 Python 演示脚本

三个零额外依赖（仅用 Python 标准库）的演示脚本，对应实验五的四个步骤要点。

| 脚本 | 对应步骤 | 说明 |
| --- | --- | --- |
| `llm_demo.py` | 接口调用 | 封装 `ask(question)`，调用兼容 OpenAI 协议的大模型接口 |
| `prompt_engineering_demo.py` | 提示工程 | "坏提示 vs 好提示"对比，理解五段式提示结构 |
| `rag_demo.py` | RAG 入门 | 文档切块 → 检索 Top-K → 拼入提示 → 基于资料作答 |
| `safety_demo.py` | 反思与安全 | 提示注入检测演示 + 幻觉应对清单 |

## 运行环境

Python 3.10+（仅标准库，**无需 pip install**）。

## 配置大模型（可选）

不配置也能运行 `rag_demo.py` 与 `safety_demo.py`（会跳过模型调用，只演示检索与安全逻辑）。
需要真实调用模型时设置环境变量：

```powershell
# PowerShell（默认对接 DeepSeek 官方，base_url 与 model 可省略）
$env:LLM_API_KEY="sk-你的密钥"
$env:LLM_BASE_URL="https://api.deepseek.com"      # DeepSeek 官方
$env:LLM_MODEL="deepseek-flash"
```

```cmd
:: CMD
set LLM_API_KEY=sk-你的密钥
set LLM_MODEL=deepseek-flash
```

换阿里云百炼（通义千问）时，**key 与地址要一起换**：

```powershell
$env:LLM_BASE_URL="https://dashscope.aliyuncs.com/compatible-mode/v1"
$env:LLM_MODEL="qwen-turbo"
```

本地 Ollama 示例：

```powershell
$env:LLM_BASE_URL="http://localhost:11434/v1"
$env:LLM_MODEL="qwen2.5:7b"
$env:LLM_API_KEY="ollama"   # Ollama 不校验，但字段不能为空
```

## 运行

```bash
python llm_demo.py "用一句话解释什么是 RESTful API"
python prompt_engineering_demo.py
python rag_demo.py "密码能不能明文存库？"
python safety_demo.py
```

> 安全提醒：脚本中不存在任何硬编码密钥，密钥一律来自环境变量，与实验四的 AI 安全要求一致。
