#!/usr/bin/env bash
# =============================================================
#  启动 StudyBuddy 前端开发服务器（Vite）
#  用法： bash start-frontend.sh
#  说明： 端口 5173；/api 请求会由 Vite 代理到后端 8080。
#         首次运行若缺依赖会自动 npm install。
# =============================================================
set -e

HERE="$(cd "$(dirname "$0")" && pwd)"
FE="$HERE/studybuddy-frontend"

# 定位 npm（系统 PATH 找不到时回落到 D:\nodejs）
NPM=""
if command -v npm >/dev/null 2>&1; then
  NPM="npm"
elif [ -f "D:/nodejs/npm.cmd" ]; then
  NPM="D:/nodejs/npm.cmd"
else
  echo "[错误] 未找到 npm，请安装 Node.js 或把它加入 PATH。"
  exit 1
fi

cd "$FE"

if [ ! -d node_modules ]; then
  echo "[提示] 未检测到 node_modules，正在安装依赖..."
  "$NPM" install
fi

echo "=============================================="
echo " 前端启动中 ...  http://127.0.0.1:5173"
echo " （接口 /api 自动代理到 http://localhost:8080）"
echo " 停止：          Ctrl + C"
echo "=============================================="
exec "$NPM" run dev
