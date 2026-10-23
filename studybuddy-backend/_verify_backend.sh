#!/usr/bin/env bash
# 启动 StudyBuddy 后端 -> 等待就绪 -> 验证接口与数据库 -> 关闭
set -u

# 数据库密码从环境变量读取，避免明文留在仓库里
# 用法： DB_PASSWORD=你的密码 bash _verify_backend.sh
export DB_PASSWORD="${DB_PASSWORD:?请先设置 DB_PASSWORD 环境变量}"

BE_DIR="C:/Users/15397/Desktop/20243190415 方野个人项目实训报告/studybuddy/studybuddy-backend"
LOG="${BE_DIR}/run-verify.log"
JAR="${BE_DIR}/target/studybuddy-backend-1.0.0.jar"
MYSQL="D:/mysql/mysql-8.4.7-winx64/bin/mysql.exe"

cd "${BE_DIR}" || exit 1
rm -f "${LOG}"

echo "### JDK"
java -version 2>&1 | head -2

export DB_PASSWORD
java -jar "${JAR}" --server.port=8080 --logging.level.com.zaxxer.hikari=DEBUG > "${LOG}" 2>&1 &
PID=$!
echo "### jar pid=${PID}"

READY=0
for i in $(seq 1 40); do
  sleep 2
  if curl -s -m 3 http://localhost:8080/api/hello > /dev/null 2>&1; then
    if [ "$(curl -s -o /dev/null -w '%{http_code}' -m 3 http://localhost:8080/api/hello)" = "200" ]; then
      echo "### ready after $((i*2))s"; READY=1; break
    fi
  fi
done
[ "${READY}" = "0" ] && echo "### NOT READY after 80s"

echo "### === 日志关键行 ==="
grep -Ei "HikariPool|已创建默认账号|默认账号初始化跳过|Started StudyBuddy|Access denied|Communications link|CannotGetJdbcConnection|Timeout" "${LOG}" | tail -20

echo "### === GET /api/hello ==="
curl -s -m 8 http://localhost:8080/api/hello; echo

echo "### === POST /api/auth/login (admin/admin123) ==="
curl -s -m 25 -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'; echo

echo "### === 数据库 t_user ==="
"${MYSQL}" -uroot -p"${DB_PASSWORD}" --default-character-set=utf8mb4 \
  -e "USE studybuddy; SELECT id,username,nickname,status FROM t_user;" 2>/dev/null

echo "### === 关闭后端 ==="
kill "${PID}" 2>/dev/null && echo "killed pid=${PID}" || echo "kill failed"
sleep 2
