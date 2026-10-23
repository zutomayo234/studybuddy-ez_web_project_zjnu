#!/usr/bin/env bash
# =============================================================
#  启动 StudyBuddy 后端（Spring Boot）
#  用法： bash start-backend.sh
#  说明： 端口 8080；数据库密码通过环境变量 DB_PASSWORD 传入，
#         不写死在脚本里（课程考核"安全与合规意识"，密钥不进仓库）。
# =============================================================
set -e

HERE="$(cd "$(dirname "$0")" && pwd)"
BE="$HERE/studybuddy-backend"
JAR="$BE/target/studybuddy-backend-1.0.0.jar"
PORT=8080

# 若存在本地私有配置则加载（.env.local 已在 .gitignore 中）
if [ -f "$HERE/.env.local" ]; then
  set -a
  # shellcheck disable=SC1091
  . "$HERE/.env.local"
  set +a
fi

if [ -z "${DB_PASSWORD:-}" ]; then
  cat <<'EOF'
[错误] 未设置数据库密码 DB_PASSWORD。

请任选一种方式：
  1) 启动时临时传入（推荐，用完即忘）：
       DB_PASSWORD=你的密码 bash start-backend.sh
  2) 在 studybuddy/.env.local 里写一行（该文件已被 .gitignore 忽略）：
       DB_PASSWORD=你的密码
     注意：打包提交实验成果前请删除此文件。
EOF
  exit 1
fi

# ---------- 启动前检查：端口是否被占用 ----------
if netstat -ano 2>/dev/null | grep -qE ":${PORT}[[:space:]].*LISTENING"; then
  HOLDER=$(netstat -ano 2>/dev/null | grep -E ":${PORT}[[:space:]].*LISTENING" | awk '{print $NF}' | sort -u | head -1)
  cat <<EOF
[错误] 端口 ${PORT} 已被占用（PID=${HOLDER}），后端无法启动。
       这通常是因为已经有一个后端在运行。

请任选一种方式：
  1) 停掉旧进程后再启动（推荐）： bash stop-all.sh
  2) 强行结束该进程：            taskkill //PID ${HOLDER} //F
EOF
  exit 1
fi

# ---------- 按需打包：jar 不存在，或 src 下有比 jar 更新的文件 ----------
NEED_PKG=0
if [ ! -f "$JAR" ]; then
  NEED_PKG=1
  echo "[提示] 未找到 jar，正在打包（首次约 1~2 分钟）..."
elif [ -n "$(find "$BE/src" -type f -newer "$JAR" -print -quit 2>/dev/null)" ]; then
  NEED_PKG=1
  echo "[提示] 检测到 src 下有比 jar 更新的文件，正在重新打包..."
  echo "       （配置/代码改动只有重新打包后才会生效，请稍候）"
fi

if [ "$NEED_PKG" = "1" ]; then
  MVN=""
  if command -v mvn >/dev/null 2>&1; then
    MVN="mvn"
  else
    MVN="$(ls -d "$HOME/.m2/wrapper/dists/apache-maven-"*/**/bin/mvn.cmd 2>/dev/null | head -1)"
    [ -z "$MVN" ] && MVN="$(ls -d "$HOME/.m2/wrapper/dists/apache-maven-"*/*/bin/mvn.cmd 2>/dev/null | head -1)"
  fi
  if [ -z "$MVN" ]; then
    echo "[错误] 未找到 Maven，请先安装 Maven 或确认 ~/.m2/wrapper 下的发行版。"
    exit 1
  fi
  ( cd "$BE" && "$MVN" -B -q clean package -DskipTests )
  echo "[完成] 打包成功。"
fi

echo "=============================================="
echo " 后端启动中 ...  http://localhost:8080"
echo " 健康检查：      http://localhost:8080/api/hello"
echo " 停止：          Ctrl + C"
echo "=============================================="
cd "$BE"
exec java -jar "$JAR" --server.port="$PORT"
