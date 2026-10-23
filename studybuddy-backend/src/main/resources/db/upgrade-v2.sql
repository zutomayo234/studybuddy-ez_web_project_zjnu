-- =============================================================
--  StudyBuddy v2 增量升级脚本（功能增强）
--  课程：《团队规范项目实训》00931    学号：20243190415  姓名：方野
--
--  用途：在已经执行过 studybuddy.sql（v1）的库上做增量升级，
--        保留既有业务数据，不删库、不重建表。
--        全新安装请直接执行 studybuddy.sql（已含 v2 结构）。
--
--  执行方式：mysql -uroot -p studybuddy < upgrade-v2.sql
--  幂等性：使用 IF NOT EXISTS / INSERT IGNORE，可重复执行。
-- =============================================================

USE studybuddy;

-- ---------- 新增表：会话 / 消息 / 操作日志 ----------

CREATE TABLE IF NOT EXISTS t_chat_session
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '归属用户',
    title       VARCHAR(100)          DEFAULT NULL COMMENT '会话标题（取首问的截断）',
    create_time DATETIME              DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_update_time (update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='智能问答会话表';

CREATE TABLE IF NOT EXISTS t_chat_message
(
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id  BIGINT        NOT NULL COMMENT '所属会话',
    role        VARCHAR(20)   NOT NULL COMMENT '角色 user/assistant',
    content     TEXT          NOT NULL COMMENT '消息正文',
    ref_excerpt VARCHAR(1000)          DEFAULT NULL COMMENT '命中的知识库片段摘要',
    fallback    TINYINT                DEFAULT 0 COMMENT '是否降级回答 0否 1是',
    create_time DATETIME               DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_session (session_id, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='智能问答消息表';

CREATE TABLE IF NOT EXISTS t_operation_log
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT                DEFAULT NULL COMMENT '操作人ID',
    username    VARCHAR(50)           DEFAULT NULL COMMENT '操作人登录名',
    module      VARCHAR(50)  NOT NULL COMMENT '业务模块',
    action      VARCHAR(100) NOT NULL COMMENT '操作描述',
    http_method VARCHAR(10)           DEFAULT NULL COMMENT 'HTTP 方法',
    uri         VARCHAR(255)          DEFAULT NULL COMMENT '请求路径',
    ip          VARCHAR(64)           DEFAULT NULL COMMENT '来源 IP',
    success     TINYINT               DEFAULT 1 COMMENT '是否成功 0否 1是',
    cost_ms     BIGINT                DEFAULT 0 COMMENT '耗时（毫秒）',
    detail      VARCHAR(500)          DEFAULT NULL COMMENT '失败原因等补充信息',
    create_time DATETIME              DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';

-- ---------- 新增权限点（7~10） ----------

INSERT IGNORE INTO t_permission (id, perm_code, perm_name, resource_type, description)
VALUES (7, 'role:assign', '角色分配', 'API', '为用户分配角色 / 调整角色权限（增强）'),
       (8, 'kb:manage', '知识库管理', 'API', '查看知识库片段与热更新（增强）'),
       (9, 'log:list', '日志查询', 'API', '查询操作审计日志（增强）'),
       (10, 'stats:view', '统计查看', 'API', '查看数据看板统计（增强）');

-- ---------- 角色权限映射增补 ----------

-- 管理员：补齐全部权限
INSERT IGNORE INTO t_role_permission (role_id, perm_id)
VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- 教师：查询 + 智能问答 + 知识库管理 + 日志 + 统计
INSERT IGNORE INTO t_role_permission (role_id, perm_id)
VALUES (2, 1), (2, 5), (2, 6), (2, 8), (2, 9), (2, 10);

-- 学生：智能问答 + 看板统计
INSERT IGNORE INTO t_role_permission (role_id, perm_id)
VALUES (3, 5), (3, 10);

-- ---------- 校验 ----------
SELECT '角色总数' AS item, COUNT(*) AS cnt FROM t_role
UNION ALL SELECT '权限总数', COUNT(*) FROM t_permission
UNION ALL SELECT '角色权限映射数', COUNT(*) FROM t_role_permission
UNION ALL SELECT '会话表', COUNT(*) FROM t_chat_session
UNION ALL SELECT '消息表', COUNT(*) FROM t_chat_message
UNION ALL SELECT '操作日志表', COUNT(*) FROM t_operation_log;
