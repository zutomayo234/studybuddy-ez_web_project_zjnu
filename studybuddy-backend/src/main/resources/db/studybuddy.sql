-- =============================================================
--  学习搭子 StudyBuddy 数据库脚本
--  课程：《团队规范项目实训》00931    学号：20243190415  姓名：方野
--  MySQL 8.x / 字符集 utf8mb4
--  执行方式：mysql -uroot -p < studybuddy.sql   （或在 Navicat 中运行）
-- =============================================================

DROP DATABASE IF EXISTS studybuddy;
CREATE DATABASE studybuddy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE studybuddy;

-- -------------------------------------------------------------
-- 1. 用户表（实验一）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录名，唯一',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码密文（严禁明文）',
    nickname    VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    avatar      VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    gender      TINYINT               DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    status      TINYINT               DEFAULT 1 COMMENT '状态 1正常 0禁用',
    remark      VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    deleted     TINYINT               DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    create_time DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- -------------------------------------------------------------
-- 2. 角色表（实验四 RBAC）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code   VARCHAR(50) NOT NULL COMMENT '角色编码，如 ROLE_ADMIN',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255)         DEFAULT NULL COMMENT '描述',
    create_time DATETIME             DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色表';

-- -------------------------------------------------------------
-- 3. 权限表（实验四 RBAC）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_permission;
CREATE TABLE t_permission
(
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    perm_code     VARCHAR(50) NOT NULL COMMENT '权限编码，如 user:list',
    perm_name     VARCHAR(50) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(20)          DEFAULT 'API' COMMENT '资源类型 MENU/BUTTON/API',
    description   VARCHAR(255)         DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='权限表';

-- -------------------------------------------------------------
-- 4. 用户-角色关联表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_user_role;
CREATE TABLE t_user_role
(
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色关联表';

-- -------------------------------------------------------------
-- 5. 角色-权限关联表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_role_permission;
CREATE TABLE t_role_permission
(
    id      BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    perm_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, perm_id),
    KEY idx_role (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色权限关联表';

-- -------------------------------------------------------------
-- 6. 会话表（实验五增强：多轮对话上下文）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_chat_session;
CREATE TABLE t_chat_session
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

-- -------------------------------------------------------------
-- 7. 消息表（实验五增强：一问一答落库）
-- 注意：字段名不用 references —— 它是 MySQL 保留字。
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_chat_message;
CREATE TABLE t_chat_message
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id   BIGINT       NOT NULL COMMENT '所属会话',
    role         VARCHAR(20)  NOT NULL COMMENT '角色 user/assistant',
    content      TEXT         NOT NULL COMMENT '消息正文',
    ref_excerpt  VARCHAR(1000)         DEFAULT NULL COMMENT '命中的知识库片段摘要（便于回溯出处）',
    fallback     TINYINT               DEFAULT 0 COMMENT '是否降级回答 0否 1是',
    create_time  DATETIME              DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_session (session_id, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='智能问答消息表';

-- -------------------------------------------------------------
-- 8. 操作日志表（实验四/五增强：审计留痕）
-- 由 AOP 切面在标注了 @OpLog 的方法执行后写入。
-- -------------------------------------------------------------
DROP TABLE IF EXISTS t_operation_log;
CREATE TABLE t_operation_log
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT                DEFAULT NULL COMMENT '操作人ID（匿名调用为 NULL）',
    username    VARCHAR(50)           DEFAULT NULL COMMENT '操作人登录名（冗余存储，便于用户被删后追溯）',
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

-- =============================================================
--  初始化数据（角色 / 权限 / 角色权限映射）
--  说明：默认用户由后端 DataInitializer 启动时用 BCrypt 创建，
--        以保证密码密文与运行环境的编码器一致（避免硬编码哈希失效）。
-- =============================================================

INSERT INTO t_role (id, role_code, role_name, description)
VALUES (1, 'ROLE_ADMIN', '系统管理员', '拥有全部权限，负责用户管理'),
       (2, 'ROLE_TEACHER', '教师', '可查看学生与知识库，使用智能问答'),
       (3, 'ROLE_STUDENT', '学生', '普通学习者，使用智能问答');

INSERT INTO t_permission (id, perm_code, perm_name, resource_type, description)
VALUES (1, 'user:list', '用户查询', 'API', '查询用户列表与详情'),
       (2, 'user:create', '用户新增', 'API', '新增用户'),
       (3, 'user:update', '用户修改', 'API', '修改用户信息与重置密码'),
       (4, 'user:delete', '用户删除', 'API', '删除用户'),
       (5, 'ai:chat', '智能问答', 'API', '调用学习搭子智能问答'),
       (6, 'role:list', '角色查询', 'API', '查询角色列表'),
       (7, 'role:assign', '角色分配', 'API', '为用户分配角色 / 调整角色权限（增强）'),
       (8, 'kb:manage', '知识库管理', 'API', '查看知识库片段与热更新（增强）'),
       (9, 'log:list', '日志查询', 'API', '查询操作审计日志（增强）'),
       (10, 'stats:view', '统计查看', 'API', '查看数据看板统计（增强）');

-- 管理员：全部权限
INSERT INTO t_role_permission (role_id, perm_id)
VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- 教师：查询 + 智能问答 + 知识库管理 + 日志 + 统计（不含用户增删改与角色分配）
INSERT INTO t_role_permission (role_id, perm_id)
VALUES (2, 1), (2, 5), (2, 6), (2, 8), (2, 9), (2, 10);

-- 学生：仅智能问答 + 看板查看
INSERT INTO t_role_permission (role_id, perm_id)
VALUES (3, 5), (3, 10);

-- -------------------------------------------------------------
-- 演示用示例数据（可选，便于实验一截图）
-- 密码统一为 123456 的 BCrypt 密文由 DataInitializer 保证一致性；
-- 这里只插入业务字段，密码留空由初始化器补齐（见 DataInitializer）。
-- -------------------------------------------------------------
