#!/usr/bin/env bash
# =============================================================
#  停止 StudyBuddy 前后端
#  用法： bash stop-all.sh
#  原理： 按端口（8080 后端 / 5173 前端）找到监听进程并结束。
# =============================================================
set -u

stop_port() {
  local port="$1"
  local pids
  pids=$(netstat -ano 2>/dev/null \
        | grep -E "[[:space:]]LISTENING[[:space:]]" \
        | grep -E ":${port}[[:space:]]" \
        | awk '{print $NF}' | sort -u)

  if [ -z "$pids" ]; then
    echo "端口 ${port}：没有正在监听的进程，跳过。"
    return
  fi

  for pid in $pids; do
    [ "$pid" = "0" ] && continue
    echo "端口 ${port}：结束进程 PID=${pid}"
    taskkill //PID "$pid" //F >/dev/null 2>&1 \
      && echo "  已结束。" \
      || echo "  结束失败（可能需要管理员权限）。"
  done
}

echo "========== 停止 StudyBuddy =========="
stop_port 8080
stop_port 5173
echo "====================================="
