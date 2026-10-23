#!/usr/bin/env bash
# StudyBuddy 鉴权链路验证：登录拿 token -> 带 token 查用户 -> 无 token 401 -> 低权限 403
set -u

# 数据库密码从环境变量读取，避免明文留在仓库里
# 用法： DB_PASSWORD=你的密码 bash _verify_authz.sh
export DB_PASSWORD="${DB_PASSWORD:?请先设置 DB_PASSWORD 环境变量}"

BE_DIR="C:/Users/15397/Desktop/20243190415 方野个人项目实训报告/studybuddy/studybuddy-backend"
LOG="${BE_DIR}/run-authz.log"
JAR="${BE_DIR}/target/studybuddy-backend-1.0.0.jar"
BASE="http://localhost:8080"

cd "${BE_DIR}" || exit 1
rm -f "${LOG}"
export DB_PASSWORD
java -jar "${JAR}" --server.port=8080 > "${LOG}" 2>&1 &
PID=$!

for i in $(seq 1 40); do
  sleep 2
  if [ "$(curl -s -o /dev/null -w '%{http_code}' -m 3 "${BASE}/api/hello" 2>/dev/null)" = "200" ]; then
    echo "后端就绪（$((i*2))s）"; break
  fi
done

login() {
  curl -s -m 20 -X POST "${BASE}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}"
}

echo ""
echo "========== 1) 管理员登录 =========="
ADMIN_JSON=$(login admin admin123)
echo "${ADMIN_JSON}" | head -c 400; echo
ADMIN_TOKEN=$(echo "${ADMIN_JSON}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

echo ""
echo "========== 2) 学生登录 =========="
STU_JSON=$(login student student123)
echo "${STU_JSON}" | head -c 400; echo
STU_TOKEN=$(echo "${STU_JSON}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

echo ""
echo "========== 3) 无 token 访问 /api/users（期望 401）=========="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -m 10 "${BASE}/api/users"

echo ""
echo "========== 4) 管理员 token 访问 /api/users（期望 200）=========="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -m 10 \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" "${BASE}/api/users"
echo "--- 返回内容（截断）---"
curl -s -m 10 -H "Authorization: Bearer ${ADMIN_TOKEN}" "${BASE}/api/users" | head -c 600; echo

echo ""
echo "========== 5) 学生 token 访问 /api/users（期望 403，无 user:list 权限）=========="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -m 10 \
  -H "Authorization: Bearer ${STU_TOKEN}" "${BASE}/api/users"
curl -s -m 10 -H "Authorization: Bearer ${STU_TOKEN}" "${BASE}/api/users"; echo

echo ""
echo "========== 6) 管理员 GET /api/auth/me（期望 200）=========="
curl -s -m 10 -H "Authorization: Bearer ${ADMIN_TOKEN}" "${BASE}/api/auth/me"; echo

echo ""
echo "========== 7) 越权探测：学生访问用户详情 /api/users/1 =========="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -m 10 \
  -H "Authorization: Bearer ${STU_TOKEN}" "${BASE}/api/users/1"

echo ""
echo "========== 8) 篡改 token 签名（期望 401）=========="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -m 10 \
  -H "Authorization: Bearer ${ADMIN_TOKEN}xx" "${BASE}/api/users"

kill "${PID}" 2>/dev/null && echo "" && echo "后端已关闭 pid=${PID}"
sleep 1
