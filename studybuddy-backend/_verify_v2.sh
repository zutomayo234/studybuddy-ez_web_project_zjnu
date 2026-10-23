#!/usr/bin/env bash
# =============================================================
#  StudyBuddy v2 功能增强验证脚本
#  覆盖：RBAC 闭环 / 自助改密 / 多轮对话持久化 / 知识库热更新
#        / 操作审计日志 / 数据看板 / OpenAPI 文档 / 权限拦截回归
#
#  用法： DB_PASSWORD=你的密码 bash _verify_v2.sh
#  说明： 输出标签一律用 ASCII，避免不同终端下中文编码不一致导致乱码。
# =============================================================
set -u

export DB_PASSWORD="${DB_PASSWORD:?请先设置 DB_PASSWORD 环境变量}"

BE_DIR="C:/Users/15397/Desktop/20243190415 方野个人项目实训报告/studybuddy/studybuddy-backend"
LOG="${BE_DIR}/run-v2.log"
JAR="${BE_DIR}/target/studybuddy-backend-1.0.0.jar"
BASE="http://localhost:8080"
MYSQL="D:/mysql/mysql-8.4.7-winx64/bin/mysql.exe"

cd "${BE_DIR}" || exit 1
rm -f "${LOG}"
export DB_PASSWORD

java -jar "${JAR}" --server.port=8080 > "${LOG}" 2>&1 &
PID=$!
echo "### backend starting pid=${PID}"

READY=0
for i in $(seq 1 45); do
  sleep 2
  if [ "$(curl -s -o /dev/null -w '%{http_code}' -m 3 "${BASE}/api/hello" 2>/dev/null)" = "200" ]; then
    echo "### ready in $((i*2))s"; READY=1; break
  fi
done
[ "${READY}" = "0" ] && { echo "### START FAILED, log tail:"; tail -30 "${LOG}"; exit 1; }

login() {
  curl -s -m 20 -X POST "${BASE}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}"
}
tok() { echo "$1" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'; }
code() { curl -s -o /dev/null -w "%{http_code}" -m 20 "$@"; }

ADMIN_JSON=$(login admin admin123);   ADMIN=$(tok "${ADMIN_JSON}")
STU_JSON=$(login student student123); STU=$(tok "${STU_JSON}")
TCH_JSON=$(login teacher teacher123); TCH=$(tok "${TCH_JSON}")

echo ""
echo "=== [1] login returns roles and permissions ==="
echo "${ADMIN_JSON}" | python -c "import sys,json;d=json.load(sys.stdin)['data'];print('roles      =',d.get('roles'));print('permissions=',d.get('permissions'))" 2>/dev/null

echo ""
echo "=== [2] FEATURE 1 RBAC: role list with permissions and user count ==="
curl -s -m 20 -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/roles" \
  | python -c "
import sys,json
for r in json.load(sys.stdin)['data']:
    print('  %-13s %-8s users=%s perms=%d %s' % (r['roleCode'], r['roleName'], r['userCount'], len(r.get('permissions') or []), r.get('permissionCodes')))
" 2>/dev/null
printf '  permission points total -> '
curl -s -m 20 -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/roles/permissions" \
  | python -c "import sys,json;print(len(json.load(sys.stdin)['data']))" 2>/dev/null

echo ""
echo "=== [3] FEATURE 6 dashboard stats ==="
curl -s -m 20 -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/stats/overview" \
  | python -c "
import sys,json
d=json.load(sys.stdin)['data']
for k in ['userTotal','userActive','roleCount','permCount','sessionTotal','messageTotal','kbChunkCount','logTotal','logFailed']:
    print('  %-14s = %s' % (k, d.get(k)))
print('  roleDistribution =', [(x['name'], x['value']) for x in d.get('roleDistribution', [])])
" 2>/dev/null

echo ""
echo "=== [4] FEATURE 4 knowledge base: stats / add chunk / reload ==="
curl -s -m 20 -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/kb/stats" \
  | python -c "import sys,json;d=json.load(sys.stdin)['data'];print('  before add    :',d)" 2>/dev/null
curl -s -m 20 -X POST "${BASE}/api/kb/chunks" \
  -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
  -d '{"content":"接口幂等性：同一请求重复执行多次，对系统状态的影响与执行一次相同。常见实现有唯一业务号去重、数据库唯一索引、乐观锁版本号。"}' \
  | python -c "import sys,json;r=json.load(sys.stdin);print('  add chunk     : code=%s %s' % (r['code'], r.get('data')))" 2>/dev/null
curl -s -m 20 -X POST "${BASE}/api/kb/reload" -H "Authorization: Bearer ${ADMIN}" \
  | python -c "import sys,json;r=json.load(sys.stdin);print('  reload        : code=%s %s' % (r['code'], r.get('data')))" 2>/dev/null
echo "  --- chunk retrievable? (ask about 幂等) ---"
curl -s -m 30 -X POST "${BASE}/api/ai/chat" \
  -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
  -d '{"question":"接口幂等性怎么实现？","useRag":true}' \
  | python -c "import sys,json;d=json.load(sys.stdin)['data'];print('  references =', len(d.get('references') or []));[print('   ',r[:70]) for r in (d.get('references') or [])]" 2>/dev/null

echo ""
echo "=== [5] FEATURE 3 multi-turn: turn 1 creates session ==="
R1=$(curl -s -m 30 -X POST "${BASE}/api/ai/chat" \
  -H "Authorization: Bearer ${STU}" -H "Content-Type: application/json" \
  -d '{"question":"密码能不能明文存库？","useRag":true}')
echo "${R1}" | python -c "import sys,json;d=json.load(sys.stdin)['data'];print('  sessionId   =',d.get('sessionId'));print('  contextSize =',d.get('contextSize'));print('  fallback    =',d.get('fallback'));print('  references  =',len(d.get('references') or []))" 2>/dev/null
SID=$(echo "${R1}" | python -c "import sys,json;print(json.load(sys.stdin)['data'].get('sessionId'))" 2>/dev/null)

echo ""
echo "=== [6] FEATURE 3 multi-turn: turn 2 with sessionId (contextSize should be > 0) ==="
curl -s -m 30 -X POST "${BASE}/api/ai/chat" \
  -H "Authorization: Bearer ${STU}" -H "Content-Type: application/json" \
  -d "{\"question\":\"那它和 JWT 有什么区别？\",\"useRag\":true,\"sessionId\":\"${SID}\"}" \
  | python -c "import sys,json;d=json.load(sys.stdin)['data'];print('  sessionId   =',d.get('sessionId'));print('  contextSize =',d.get('contextSize'),'  <- 应大于 0，证明历史被回灌')" 2>/dev/null

echo ""
echo "=== [7] FEATURE 3 session list and message history ==="
curl -s -m 20 -H "Authorization: Bearer ${STU}" "${BASE}/api/ai/sessions" \
  | python -c "import sys,json;[print('  session id=%s title=%s messages=%s' % (s['id'],s['title'],s['messageCount'])) for s in json.load(sys.stdin)['data']]" 2>/dev/null
curl -s -m 20 -H "Authorization: Bearer ${STU}" "${BASE}/api/ai/sessions/${SID}/messages" \
  | python -c "
import sys,json
d=json.load(sys.stdin)['data']
print('  message count =', len(d))
for m in d:
    print('   %-9s %s' % (m['role'], m['content'][:46].replace('\n',' ')))
" 2>/dev/null

echo ""
echo "=== [8] cross-user guard: admin reads student session (expect 404) ==="
printf '  HTTP %s\n' "$(code -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/ai/sessions/${SID}/messages")"

echo ""
echo "=== [9] FEATURE 5 operation audit logs ==="
curl -s -m 20 -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/logs?pageNum=1&pageSize=6" \
  | python -c "
import sys,json
d=json.load(sys.stdin)['data']
print('  total =', d['total'])
for r in d['list']:
    print('   id=%-3s %-8s %-10s %-16s %-6s ok=%s %sms' % (r['id'], r.get('username'), r['module'], r['action'], r['httpMethod'], r['success'], r['costMs']))
" 2>/dev/null

echo ""
echo "=== [10] authorization regression (experiment 4) ==="
printf '  no token  -> /api/users        expect 401, got %s\n' "$(code "${BASE}/api/users")"
printf '  student   -> /api/users        expect 403, got %s\n' "$(code -H "Authorization: Bearer ${STU}" "${BASE}/api/users")"
printf '  student   -> /api/logs         expect 403, got %s\n' "$(code -H "Authorization: Bearer ${STU}" "${BASE}/api/logs")"
printf '  student   -> /api/kb/chunks    expect 403, got %s\n' "$(code -H "Authorization: Bearer ${STU}" "${BASE}/api/kb/chunks")"
printf '  teacher   -> /api/kb/chunks    expect 200, got %s\n' "$(code -H "Authorization: Bearer ${TCH}" "${BASE}/api/kb/chunks")"
printf '  teacher   -> /api/logs         expect 200, got %s\n' "$(code -H "Authorization: Bearer ${TCH}" "${BASE}/api/logs")"
printf '  teacher   -> DELETE /api/users/1  expect 403, got %s\n' "$(code -X DELETE -H "Authorization: Bearer ${TCH}" "${BASE}/api/users/1")"
printf '  tampered token -> /api/users   expect 401, got %s\n' "$(code -H "Authorization: Bearer ${ADMIN}xx" "${BASE}/api/users")"

echo ""
echo "=== [11] FEATURE 7 OpenAPI docs (whitelisted, no auth) ==="
printf '  /v3/api-docs           -> %s\n' "$(code "${BASE}/v3/api-docs")"
printf '  /swagger-ui/index.html -> %s\n' "$(code "${BASE}/swagger-ui/index.html")"
printf '  documented paths       -> '
curl -s -m 20 "${BASE}/v3/api-docs" | python -c "import sys,json;print(len(json.load(sys.stdin).get('paths',{})))" 2>/dev/null

echo ""
echo "=== [12] FEATURE 2 change own password (old password required) ==="
printf '  wrong old password     expect 400, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${STU}" -H "Content-Type: application/json" \
      -d '{"oldPassword":"wrongpass","newPassword":"newpass123"}' "${BASE}/api/auth/password")"
printf '  same as old password   expect 400, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${STU}" -H "Content-Type: application/json" \
      -d '{"oldPassword":"student123","newPassword":"student123"}' "${BASE}/api/auth/password")"
printf '  correct old password   expect 200, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${STU}" -H "Content-Type: application/json" \
      -d '{"oldPassword":"student123","newPassword":"newpass123"}' "${BASE}/api/auth/password")"
printf '  login with new password expect 200, got %s\n' \
  "$(code -X POST -H "Content-Type: application/json" \
      -d '{"username":"student","password":"newpass123"}' "${BASE}/api/auth/login")"
curl -s -m 20 -X PUT "${BASE}/api/auth/password" \
  -H "Authorization: Bearer ${STU}" -H "Content-Type: application/json" \
  -d '{"oldPassword":"newpass123","newPassword":"student123"}' > /dev/null
printf '  restored, login again  expect 200, got %s\n' \
  "$(code -X POST -H "Content-Type: application/json" \
      -d '{"username":"student","password":"student123"}' "${BASE}/api/auth/login")"

echo ""
echo "=== [13] FEATURE 1 role assignment (overwrite + guard) ==="
printf '  add TEACHER to student      expect 200, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
      -d '{"roleIds":[2,3]}' "${BASE}/api/users/3/roles")"
curl -s -m 20 -H "Authorization: Bearer ${ADMIN}" "${BASE}/api/users/3" \
  | python -c "import sys,json;d=json.load(sys.stdin)['data'];print('  student roleCodes =',d.get('roleCodes'),' roleIds =',d.get('roleIds'))" 2>/dev/null
printf '  illegal roleId 999          expect 400, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
      -d '{"roleIds":[999]}' "${BASE}/api/users/3/roles")"
printf '  empty roleIds               expect 400, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
      -d '{"roleIds":[]}' "${BASE}/api/users/3/roles")"
printf '  demote last admin           expect 409, got %s\n' \
  "$(code -X PUT -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
      -d '{"roleIds":[3]}' "${BASE}/api/users/1/roles")"
curl -s -m 20 -X PUT "${BASE}/api/users/3/roles" \
  -H "Authorization: Bearer ${ADMIN}" -H "Content-Type: application/json" \
  -d '{"roleIds":[3]}' > /dev/null
echo "  restored student to ROLE_STUDENT only"

echo ""
echo "=== [14] database verification ==="
"${MYSQL}" -uroot -p"${DB_PASSWORD}" --default-character-set=utf8mb4 -e "
USE studybuddy;
SELECT 'permissions' AS item, COUNT(*) AS cnt FROM t_permission
UNION ALL SELECT 'role_permission', COUNT(*) FROM t_role_permission
UNION ALL SELECT 'chat_session', COUNT(*) FROM t_chat_session
UNION ALL SELECT 'chat_message', COUNT(*) FROM t_chat_message
UNION ALL SELECT 'operation_log', COUNT(*) FROM t_operation_log;
SELECT id, username, module, action, success, cost_ms FROM t_operation_log ORDER BY id DESC LIMIT 10;
" 2>/dev/null

echo ""
echo "=== [15] shutdown backend ==="
kill "${PID}" 2>/dev/null && echo "  stopped pid=${PID}" || echo "  stop failed"
sleep 2
