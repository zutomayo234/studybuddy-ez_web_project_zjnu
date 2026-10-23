# -*- coding: utf-8 -*-
'''
实验一 ~ 实验三 报告正文内容
说明：正文文本统一用三引号(单引号)包裹，因此内部可安全使用英文双引号做中文引号强调。
行文采用正式书面语，保持第一人称视角与诚实自评；代码块与表格保持原样。
'''

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from _report_builder import (  # noqa: E402
    add_body, add_bullets, add_code, add_heading, add_para, add_screenshot_placeholder,
    add_table,
)

# ======================================================================
# 实验一
# ======================================================================
INTRO_1 = [
    '''实验一是整个实训的起点，任务是用 Maven 构建一个 Web 工程、完成数据源配置、实现用户表的增删改查（CRUD）并使其能够运行。概括地说，本实验要回答的问题是：能否独立搭建一个后端服务，使数据库中的数据可以被程序操作。''',
    '''本项目并未为每个实验单独创建演示工程，而是自始至终围绕同一个系统展开——"学习搭子 StudyBuddy"，一个协助大学生整理课程资料的助手。实验一完成其底层基础（工程结构与用户管理），后续四个实验在此基础上逐步扩展。如此安排的原因是：若每个实验各自更换一个项目，最终只会得到几份互不相关的代码，难以形成对整体系统的理解。''',
    '''本机开发环境为：Windows 11、JDK 21、Maven 3.9.16、MySQL 8.4.7，编码工具为 IntelliJ IDEA，数据库管理使用 Navicat。需要说明一处与课程方案的差异：课程示例给出的版本是 JDK 17，而本机安装的是 JDK 21。起初我担心版本不匹配会导致编译失败，查阅资料后确认 Spring Boot 3.x 要求 JDK 17 及以上，因此使用 JDK 21 完全可行，实测编译与运行均正常。''',
    '''我为本实验设定了四项验收标准：① 工程能够编译并打包；② 能够成功连接数据库；③ 用户表的五个接口均可正常调用；④ 能够打包为可执行 jar 直接启动，不依赖外部单独部署的 Tomcat。''',
]


def tech_1(doc):
    add_body(doc, '''本实验涉及的技术及其版本见下表。表中版本号均与本机实际安装的一致，不是凭印象填写的。''')
    add_table(doc,
              ['技术', '版本', '在本实验中的作用'],
              [['Maven', '3.9.16', '项目构建与依赖管理，统一管理第三方 jar 版本'],
               ['JDK', '21', '运行环境（Spring Boot 3.x 要求 ≥17）'],
               ['Spring Boot', '3.3.5', '自动装配、起步依赖、内嵌 Tomcat，省去大量 XML 配置'],
               ['Spring MVC', '6.1.14', 'Web 层：请求映射、参数绑定、JSON 序列化'],
               ['MyBatis', '3.5.14', '持久层：SQL 与 Java 对象映射'],
               ['MySQL', '8.4.7', '关系型数据库，存储用户、角色、权限数据'],
               ['内嵌 Tomcat', '10.1.31', '随应用启动的 Servlet 容器，无需单独部署 WAR'],
               ['Lombok', '1.18.34', '编译期生成 getter/setter，减少样板代码']],
              widths=[3.0, 2.2, 9.5])

    add_body(doc, '''有一个概念我最初并不清楚：课程方案介绍的是 SSM（Spring + Spring MVC + MyBatis）配合 web.xml 的写法，而本项目实际使用的是 Spring Boot，二者究竟是什么关系？经过查阅资料，我的理解是：SSM 属于"手动装配"，DispatcherServlet、数据源、事务管理器等都需要逐项声明；Spring Boot 则将这些自动配置完成，也就是通常所说的"约定优于配置"。两者的分层思想完全一致，因此理解 Spring Boot 之后，反而更容易看清 SSM 中每一个 XML 配置项所对应的作用。本实验使用的是 Spring Boot 3.3.5。''')


def design_1(doc):
    add_heading(doc, '3.1 概要设计', 2)
    add_body(doc, '''系统采用经典的 MVC 三层结构，自上而下依次为接口层、业务层与持久层。三层职责的划分是：Controller 只负责接收请求，不编写 SQL；Mapper 只负责执行 SQL，不做业务判断；业务规则与事务边界统一放在 Service 中。''')
    add_code(doc, '''浏览器 / Postman / curl
        │  HTTP + JSON
        ▼
┌─────────────────────────────────────────────┐
│  Controller 层（接口层）                       │  接收请求、参数校验、返回统一 Result
│  UserController / HelloController            │
└──────────────────┬──────────────────────────┘
                   ▼
┌─────────────────────────────────────────────┐
│  Service 层（业务层）                          │  业务规则、事务、DTO↔Entity↔VO 转换
│  UserService / UserServiceImpl               │
└──────────────────┬──────────────────────────┘
                   ▼
┌─────────────────────────────────────────────┐
│  Mapper 层（持久层）                           │  只负责 SQL 执行与对象映射
│  UserMapper + UserMapper.xml                 │
└──────────────────┬──────────────────────────┘
                   ▼
              MySQL 8.x（studybuddy 库）''', '图 3-1　MVC 分层架构')

    add_body(doc, '''后端工程的包结构如下。最初我倾向于按业务模块分包，后来发现按技术职责分包层次更清晰，便调整为当前的组织方式，查找代码的效率明显提高。下表中标注"增强"的部分，是在五个实验全部完成之后所做的第二轮功能补充，其动因在 3.4 节说明。''')
    add_code(doc, '''com.zjnu.studybuddy
├── StudyBuddyApplication.java   # 启动类（@SpringBootApplication + @MapperScan）
├── controller/                  # 接口层：UserController、HelloController
│                                #   + AuthController、AiController（实验四/五）
│                                #   + RoleController、KbController、LogController、StatsController（增强）
├── service/                     # 业务接口：UserService、AuthService、AiService
│   └── impl/                    # 业务实现：UserServiceImpl、AuthServiceImpl...
│                                #   + RoleService、ChatSessionService、KbService、LogService、StatsService（增强）
├── mapper/                      # MyBatis 接口：UserMapper、RoleMapper、PermissionMapper
│                                #   + ChatSessionMapper、ChatMessageMapper、OperationLogMapper（增强）
├── entity/                      # 数据库实体：User、Role、Permission
│                                #   + ChatSession、ChatMessage、OperationLog（增强）
├── dto/                         # 入参对象：UserCreateDTO、UserUpdateDTO、UserQueryDTO
├── vo/                          # 出参对象：UserVO、LoginVO、ChatVO
├── common/                      # 通用：Result、PageResult、异常处理、脱敏工具
├── config/                      # 配置：SecurityConfig、CorsConfig、属性绑定类
│                                #   + OpenApiConfig（增强，接口文档）
├── security/                    # 安全：JwtUtil、JwtAuthenticationFilter、LoginUser
├── annotation/                  # 自定义注解：@OpLog（增强，操作审计）
├── aspect/                      # 切面：OpLogAspect（增强，审计日志落库）
└── ai/                          # AI：LlmClient、PromptTemplates、RagService
                                 #   + ChatContextAssembler（增强，多轮上下文装配）''')

    add_heading(doc, '3.2 详细设计', 2)
    add_body(doc, '''各层主要的类、其职责与关键方法见下表。''')
    add_table(doc,
              ['层次', '类名', '职责', '关键方法'],
              [['接口层', 'UserController', '暴露 RESTful 接口，参数校验后调用 Service',
                'page / detail / create / update / delete'],
               ['业务层', 'UserService', '定义用户业务契约，便于替换实现与 Mock 测试',
                'page / getById / create / update / delete'],
               ['业务层', 'UserServiceImpl', '实现业务规则：唯一性校验、密码加密、VO 转换与脱敏',
                'create / update / resetPassword / toVO'],
               ['持久层', 'UserMapper', '声明数据访问方法，SQL 写在 XML 中',
                'selectPage / insert / updateById / deleteById'],
               ['实体', 'User', '映射 t_user 表结构，密码字段加 @JsonIgnore', '—'],
               ['出参', 'UserVO', '与实体分离，避免密码外泄，手机号脱敏展示', '—'],
               ['通用', 'Result<T>', '统一响应结构 {code, message, data, timestamp}', 'success / error'],
               ['通用', 'GlobalExceptionHandler', '@RestControllerAdvice 统一异常转 JSON',
                'handleBusiness / handleValid / handleOther']],
              widths=[1.7, 3.2, 5.5, 4.3])

    add_body(doc, '''此处需要说明 DTO、VO 与 Entity 三种对象为何要分开，这是本实验中理解之后收益最明显的一点。Entity 与数据库表直接对应，若将其直接返回给前端，密码、逻辑删除标记等不应暴露的字段会一并泄露；DTO 用于接收前端传入的参数，并可挂载校验注解；VO 决定最终向前端输出哪些字段、以何种形式呈现（例如手机号需脱敏为 138****0008）。这样设计确实需要额外定义若干个类，但对象边界清晰，也能有效避免安全事故，我认为这样的成本是值得的。''')

    add_heading(doc, '3.3 数据库设计', 2)
    add_body(doc, '''数据库名为 studybuddy，字符集采用 utf8mb4。实验一仅使用核心表 t_user；其余各表（实验四的 RBAC 四表，以及后续增强引入的会话、消息、操作日志三表）分别在其所属实验的数据库设计小节中给出。下面先给出 t_user 的建表语句。''')
    add_code(doc, '''CREATE DATABASE studybuddy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE studybuddy;

CREATE TABLE t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录名，唯一',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码密文（严禁明文）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    gender      TINYINT      DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1正常 0禁用',
    remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';''', '代码 3-1　t_user 建表语句（节选自 db/studybuddy.sql）')

    add_body(doc, '''若干字段的设计理由，说明如下：''')
    add_bullets(doc, [
        '''主键采用 BIGINT 自增——查阅资料时看到建议认为 INT 在数据量增长后可能不足，考虑到实现成本并不高，因此如此设定；''',
        '''username 建立唯一索引——这是在实际出现问题之后补充的：最初仅在应用层判断重名，并发场景下仍会插入重复数据，因此在数据库层面增加唯一约束作为兜底，与应用层校验形成双保险；''',
        '''password 长度设为 100——BCrypt 密文固定为 60 个字符，留出一定余量；''',
        '''deleted 采用逻辑删除——删除操作仅将标记置为 1，不做物理删除，以便误删后能够恢复；''',
        '''create_time / update_time 交由数据库默认值生成，以减少应用层代码；''',
        '''status 与 deleted 建立索引——列表查询默认都会附带 deleted = 0 条件，若不建索引容易导致全表扫描。''',
    ])

    add_body(doc, '''表 3-2　t_user 主要字段说明''')
    add_table(doc,
              ['字段', '类型', '约束', '说明'],
              [['id', 'BIGINT', 'PK, AUTO_INCREMENT', '用户唯一标识'],
               ['username', 'VARCHAR(50)', 'NOT NULL, UNIQUE', '登录名，3~20 位'],
               ['password', 'VARCHAR(100)', 'NOT NULL', 'BCrypt 密文，绝不存明文'],
               ['nickname', 'VARCHAR(50)', '可空', '昵称，为空时默认取用户名'],
               ['email', 'VARCHAR(100)', '可空', '邮箱，前端展示时脱敏'],
               ['phone', 'VARCHAR(20)', '可空', '手机号，展示时脱敏为 138****0008'],
               ['gender', 'TINYINT', '默认 0', '0 未知 / 1 男 / 2 女'],
               ['status', 'TINYINT', '默认 1, 索引', '1 正常 / 0 禁用，禁用后不可登录'],
               ['deleted', 'TINYINT', '默认 0, 索引', '逻辑删除标记'],
               ['create_time', 'DATETIME', '默认 NOW()', '创建时间'],
               ['update_time', 'DATETIME', '自动更新', '最后修改时间']],
              widths=[2.6, 3.0, 3.6, 5.5])

    add_body(doc, '''以上为 t_user 的主要字段。需要说明的是，本节虽然只展开 t_user 一张表，但随实验推进与后续增强，整个系统最终共使用 8 张表。为便于建立整体印象，先在此列出全部表及其引入的实验，各表的完整建表语句分别放在相应实验的"数据库设计"小节中。''')
    add_table(doc,
              ['序号', '表名', '用途', '引入于'],
              [['1', 't_user', '用户主表', '实验一'],
               ['2', 't_role', '角色表', '实验四（RBAC）'],
               ['3', 't_permission', '权限表', '实验四（RBAC）'],
               ['4', 't_user_role', '用户-角色关联表', '实验四（RBAC）'],
               ['5', 't_role_permission', '角色-权限关联表', '实验四（RBAC）'],
               ['6', 't_chat_session', '智能问答会话表', '实验五（增强）'],
               ['7', 't_chat_message', '智能问答消息表', '实验五（增强）'],
               ['8', 't_operation_log', '操作审计日志表', '实验四（增强）']],
              widths=[1.3, 4.2, 5.4, 3.1])

    add_heading(doc, '3.4 后续功能增强概览', 2)
    add_body(doc, '''五个实验逐项完成之后，我对系统做了一次通盘的复查。复查的出发点并非"功能还不够多"，而是一个更具体的问题：如果把这个系统交给一个真实的使用者，他在哪些环节会"做不下去"。据此我列出了若干缺口——例如管理员无法在界面上调整角色权限、用户不能自行修改密码、越权行为无据可查、问答无法连续追问、知识库必须重启才能更新。这些虽不属于五个实验的硬性要求，却正是"能跑通"与"像个系统"之间的距离。''')
    add_body(doc, '''于是我在此基础上做了第二轮补充，共七项，并尽量与前面的实验架构保持一致——沿用同一套分层、同一套统一响应与异常处理，不另起炉灶。七项增强与所属实验的对应关系如下，其实现细节分别写入实验二、四、五的报告，本节仅作索引，不重复展开。''')
    add_table(doc,
              ['编号', '增强项', '解决的问题', '归属报告'],
              [['1', 'RBAC 闭环：角色/权限管理与用户角色分配', '管理员无法自助调整权限，权限变更只能改数据库', '实验四'],
               ['2', '用户自助修改密码（校验旧密码）', '改密码只能由管理员重置，本人无法操作', '实验四'],
               ['3', '多轮对话会话持久化', '刷新页面即丢失上下文，无法连续追问', '实验五'],
               ['4', '知识库在线查看与热更新', '更新知识库必须重启后端', '实验五'],
               ['5', '基于 AOP 的操作审计日志', '关键写操作无留痕，出问题难以追溯', '实验四'],
               ['6', '首页数据看板统计', '首页只有静态文字，看不到系统运行状况', '实验二'],
               ['7', 'OpenAPI 接口文档（Swagger UI）', '接口靠手写文档维护，容易与代码脱节', '实验二']],
              widths=[1.2, 5.2, 5.6, 1.9])
    add_body(doc, '''需要预先说明的是，这七项增强完成后，后端单元测试由原来的 11 项增加到 20 项（新增多轮上下文装配、知识库热更新两组用例），并且是在重新执行完整的端到端回归验证之后，才把结论写入本报告的，验证结果见各实验的"运行结果"一节。下文凡涉及增强功能之处，均会明确标注，不与原实验要求相混淆。''')


def code_1(doc):
    add_body(doc, '''下面按"启动 → 配置 → 查询 → 增删改"的顺序，选取若干关键代码段进行说明，这些也是我起初编写时较为生疏的部分。''')

    add_code(doc, '''@SpringBootApplication
@MapperScan("com.zjnu.studybuddy.mapper")
public class StudyBuddyApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyBuddyApplication.class, args);
    }
}''', '代码 4-1　启动类 StudyBuddyApplication.java')
    add_body(doc, '''@SpringBootApplication 是一个组合注解，等价于 @Configuration + @EnableAutoConfiguration + @ComponentScan 三者叠加，我是在完成编写之后才去确认它具体包含哪些注解。@MapperScan 用于指定 MyBatis 应当扫描哪个包下的 Mapper 接口，有了它便无需在每个接口上重复标注 @Mapper。''')

    add_code(doc, '''server:
  port: 8080
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/studybuddy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    # 密码走环境变量，避免明文入库/入仓
    password: ${DB_PASSWORD:123456}
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.zjnu.studybuddy.entity
  configuration:
    map-underscore-to-camel-case: true''', '代码 4-2　application.yml 数据源与 MyBatis 配置')
    add_body(doc, '''这里有三处问题我均实际遇到过：① URL 中必须添加 serverTimezone 参数，否则 MySQL 8 会报时区错误；② 开启 map-underscore-to-camel-case 之后，数据库的 create_time 才能自动映射到 Java 的 createTime，否则查询结果全为 null；③ 密码写成 ${DB_PASSWORD:123456} 的形式，含义是优先读取环境变量，读取不到时使用默认值——这是有意为之，因为该配置需要提交至 Git 仓库，直接写入真实密码等同于将其公开。''')

    add_code(doc, '''@Data
public class User implements Serializable {
    private Long id;
    private String username;
    /** 密码密文（BCrypt）。@JsonIgnore 保证序列化为 JSON 时永不外泄 */
    @JsonIgnore
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer gender;   // 0未知 1男 2女
    private Integer status;   // 1正常 0禁用
    @JsonIgnore
    private Integer deleted;  // 逻辑删除标记，不对外暴露
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Role> roles;
}''', '代码 4-3　实体类 User.java')
    add_body(doc, '''@JsonIgnore 这一注解的收益较高：即使某次编写接口时发生疏忽，直接将 User 对象作为返回值，序列化为 JSON 时也会自动跳过 password 与 deleted 字段，相当于在数据出口处为密码增加了一道保障。''')

    add_code(doc, '''<select id="selectPage" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM t_user
    <include refid="Query_Where"/>
    ORDER BY id DESC
    LIMIT #{q.pageSize} OFFSET #{q.offset}
</select>

<sql id="Query_Where">
    <where>
        deleted = 0
        <if test="q.keyword != null and q.keyword != ''">
            AND (username LIKE CONCAT('%', #{q.keyword}, '%')
                 OR nickname LIKE CONCAT('%', #{q.keyword}, '%')
                 OR email   LIKE CONCAT('%', #{q.keyword}, '%'))
        </if>
        <if test="q.status != null">
            AND status = #{q.status}
        </if>
    </where>
</sql>

<update id="updateById" parameterType="com.zjnu.studybuddy.entity.User">
    UPDATE t_user
    <set>
        <if test="nickname != null">nickname = #{nickname},</if>
        <if test="email    != null">email    = #{email},</if>
        <if test="phone    != null">phone    = #{phone},</if>
        <if test="status   != null">status   = #{status},</if>
        update_time = NOW()
    </set>
    WHERE id = #{id} AND deleted = 0
</update>''', '代码 4-4　UserMapper.xml 动态 SQL（条件分页查询与动态更新）')
    add_body(doc, '''这段动态 SQL 给我留下的印象较深：① 使用 <where> 与 <if> 组合之后，无论是否传入条件都可复用同一段 SQL，不必为每种条件组合单独编写查询；② 全部使用 #{} 而非 ${}，因为 #{} 会被编译为问号占位符，能够防止 SQL 注入，这是查阅资料时反复被强调的一点；③ 更新使用 <set> 与 <if> 组合，从而只更新前端实际传入的字段。第三点是我修正后的结果——最初写的是全字段更新，导致前端未传的字段被覆盖为 null，改为动态更新之后才得以解决。''')

    add_code(doc, '''@GetMapping
public Result<PageResult<UserVO>> page(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    UserQueryDTO query = new UserQueryDTO();
    query.setKeyword(keyword);
    query.setStatus(status);
    query.setPageNum(pageNum);
    query.setPageSize(pageSize);
    return Result.success(userService.page(query));
}

@PostMapping
public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
    return Result.success("新增成功", userService.create(dto));
}

@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return Result.success("删除成功", null);
}''', '代码 4-5　UserController 关键接口（查询 / 新增 / 删除）')

    add_code(doc, '''@Override
@Transactional(rollbackFor = Exception.class)
public UserVO create(UserCreateDTO dto) {
    // 1. 唯一性校验
    if (userMapper.countByUsername(dto.getUsername()) > 0) {
        throw new BusinessException(ResultCode.CONFLICT, "用户名已存在：" + dto.getUsername());
    }
    User user = new User();
    BeanUtils.copyProperties(dto, user);
    if (!StringUtils.hasText(user.getNickname())) {
        user.setNickname(dto.getUsername());
    }
    // 2. 关键安全点：明文密码绝不入库，统一 BCrypt 加密
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    userMapper.insert(user);
    // 3. 默认分配学生角色
    userMapper.insertUserRoles(user.getId(), List.of(3L));
    return getById(user.getId());
}''', '代码 4-6　UserServiceImpl.create —— 业务规则与事务边界')
    add_body(doc, '''此处添加 @Transactional(rollbackFor = Exception.class)，是因为新增用户实际包含两步操作：插入用户记录，再为其分配角色。若第一步成功而第二步失败，将留下"有用户但无角色"的脏数据。引入事务之后，两步操作要么同时成功、要么同时回滚，该问题得以消除。''')

    add_code(doc, '''/** 实体 -> 视图对象（脱敏 + 角色编码） */
private UserVO toVO(User user) {
    UserVO vo = new UserVO();
    BeanUtils.copyProperties(user, vo);
    vo.setPhone(MaskUtil.maskPhone(user.getPhone()));   // 138****0008
    vo.setGenderText(switch (user.getGender() == null ? 0 : user.getGender()) {
        case 1 -> "男";
        case 2 -> "女";
        default -> "未知";
    });
    vo.setRoleCodes(roleMapper.selectRolesByUserId(user.getId())
            .stream().map(Role::getRoleCode).collect(Collectors.toList()));
    return vo;
}''', '代码 4-7　实体到视图对象的转换与脱敏')


def run_1(doc):
    add_body(doc, '''本实验的运行验证分为五个环节：建库建表 → 启动应用 → 连通性测试 → 验证 CRUD 接口 → 打包部署。以下是我需要留存的运行截图。''')

    add_screenshot_placeholder(doc, 1,
        '''数据库建表结果：在 Navicat 中展开 studybuddy 库，显示 t_user 等表；并执行 SELECT * FROM t_user 查看初始数据。''')
    add_screenshot_placeholder(doc, 2,
        '''应用启动日志：控制台输出 "Started StudyBuddyApplication in x.xxx seconds"，以及 "Tomcat started on port 8080" 一行，证明内嵌 Tomcat 部署成功。''')
    add_screenshot_placeholder(doc, 3,
        '''连通性测试：浏览器或 Postman 访问 GET http://localhost:8080/api/hello，返回 {"code":200,...}。''')
    add_screenshot_placeholder(doc, 4,
        '''新增用户：POST http://localhost:8080/api/users，请求体含 username / password / nickname，返回新增后的用户信息。''')
    add_screenshot_placeholder(doc, 5,
        '''分页查询：GET http://localhost:8080/api/users?pageNum=1&pageSize=10，返回 total 与 list。''')
    add_screenshot_placeholder(doc, 6,
        '''修改与删除：PUT /api/users/4 修改昵称；DELETE /api/users/4 逻辑删除后再查询，确认该用户已不可见。''')
    add_screenshot_placeholder(doc, 7,
        '''打成可执行 jar 部署：执行 mvn clean package 生成 studybuddy-backend-1.0.0.jar，再用 java -jar 启动并访问接口成功。''')

    add_body(doc, '''测试结果如下：五个 CRUD 接口均能正常工作；插入重复用户名时唯一索引正确报错（HTTP 409）；参数校验（如邮箱格式错误、密码少于 6 位）返回 400 并附带中文提示；逻辑删除之后数据实际仍保留在表中，只是查询不到。本机实测启动日志为 "Started StudyBuddyApplication in 3.823 seconds"、"Tomcat started on port 8080"，访问 /api/hello 亦正常返回。''')


def reflect_1(doc):
    add_body(doc, '''（一）实验收获''')
    add_bullets(doc, [
        '''理解了 MVC 分层的价值：将参数校验、业务规则与 SQL 执行分离之后，修改任一层基本不影响其他层，问题定位也更为容易；''',
        '''理解了 Spring Boot"约定优于配置"的含义：相比 SSM 的 web.xml 与多个 Spring XML，配置量减少了一个数量级；''',
        '''理解了 DTO / VO / Entity 分离的必要性——这是由实际问题促成的：最初为减少工作量而直接返回 User 对象，结果密码字段被序列化输出，此后才引入 @JsonIgnore 兜底，并改用 UserVO 控制出参；''',
        '''理解了 #{} 与 ${} 的区别：前者为预编译参数，可防注入；后者是字符串拼接，存在注入风险。这一点我最初确实不了解。''',
        '''认识到"完成实验要求"与"完成一个系统"之间存在差距：五个实验走完后我做了第二轮增强（见 3.4 节），补齐了角色管理、自助改密、审计留痕等管理侧能力，它们并不在实验要求之内，却是一个真正可用的系统所必需的。''',
    ])

    add_body(doc, '''（二）遇到的问题与解决''')
    add_table(doc,
              ['问题现象', '原因定位', '解决办法'],
              [['启动报时区错误', 'MySQL 8 默认时区与 JDBC 驱动不匹配',
                'JDBC URL 增加 serverTimezone=Asia/Shanghai'],
               ['前端拿到的时间有偏差', 'Jackson 默认使用 UTC',
                '配置 spring.jackson.time-zone 与 date-format'],
               ['重复用户名未报错却插入了脏数据', '只做了应用层校验，未加数据库约束',
                'username 加 UNIQUE 索引，应用层捕获后返回 409'],
               ['更新用户时未传的字段被清空', '使用了全字段 UPDATE',
                '改为 <set> + <if> 动态更新，只更新非空字段']],
              widths=[4.5, 5.0, 5.2])

    add_body(doc, '''（三）AI 工具使用记录''')
    add_table(doc,
              ['环节', '使用的 AI 工具', '提示词要点', '产出如何验证'],
              [['生成 Maven 依赖清单', 'AI 编程助手',
                '写明 Spring Boot 3.3 + MyBatis + MySQL + Lombok + JDK 21', 'mvn clean test-compile 编译通过'],
               ['生成实体与 DTO 样板', 'AI 编程助手',
                '给出 t_user 表结构，要求 Lombok 加校验注解、密码字段不外泄',
                '人工逐字段比对表结构；实测接口不返回 password'],
               ['生成 MyBatis 动态 SQL', 'AI 编程助手',
                '条件分页查询 + 逻辑删除 + 动态更新', '人工检查无 ${} 拼接，确认无注入风险'],
               ['排查启动报错', 'AI 编程助手', '贴上完整异常堆栈',
                '按建议修改后重新启动，验证通过']],
              widths=[3.2, 2.8, 5.0, 4.5])

    add_body(doc, '''（四）反思与边界意识''')
    add_body(doc, '''客观地说，AI 生成的脚手架确实减少了大量重复劳动，但它并不了解我的表结构细节。例如它首次生成的实体把 phone 写成了 int 类型——这类"看似合理、实则错误"的问题，只能通过逐字段核对发现。此外，AI 默认会把数据库密码直接写入 application.yml，我没有采纳，而是改为环境变量占位，因为这份代码需要提交至 Git 仓库。这让我意识到：AI 提升的是产出速度，而质量与安全的把关仍须由人负责。''')
    add_body(doc, '''本实验属于纯工程搭建，尚未涉及 AI 功能，因此 AI 在此仅作为开发辅助工具，未出现需要纠正的模型幻觉案例，相关问题在实验五中讨论。''')


# ======================================================================
# 实验二
# ======================================================================
INTRO_2 = [
    '''实验二的任务是将实验一中前后端耦合的应用改造为前后端分离结构：后端只负责提供 RESTful 风格的 JSON 接口，前端使用 Vue.js 构建单页应用（SPA）调用接口并渲染页面，同时解决两端端口不同所导致的跨域（CORS）问题。''',
    '''本实验中，我在前端新增了登录页、用户管理页与智能问答页三个页面；后端在原有 CRUD 的基础上理清了接口语义，增加 CorsConfig 跨域配置，并引入统一响应结构 Result 与 PageResult，使前端可以用同一套逻辑处理成功与失败两种结果。''',
    '''在后续的功能增强（见实验一 3.4 节）中，本实验的部分也被一并扩充：前端视图文件由 5 个增加到 9 个，新增角色权限、知识库管理、操作日志、个人中心四个页面，并将"菜单与页面是否可见"由原先写死在代码里，改为由后端下发的权限编码驱动；后端则接入 OpenAPI 文档，使接口契约能够由代码自动生成，减少手写文档与代码脱节的风险。这两项都属于"增强"，与原实验要求分开说明。''',
    '''我为实验二设定的验收标准是：① 后端接口符合 RESTful 风格；② 前端能通过 Axios 调用接口并正确渲染；③ 跨域问题得到解决，浏览器控制台无 CORS 报错；④ 前后端能够各自独立构建与部署。''',
]


def tech_2(doc):
    add_body(doc, '''本实验涉及的技术如下。''')
    add_table(doc,
              ['技术', '版本', '作用'],
              [['RESTful 风格', '—', '用 HTTP 动词表达语义，用复数名词表示资源，接口自解释'],
               ['JSON', '—', '前后端之间轻量的数据交换格式'],
               ['Vue 3', '3.4', '前端框架，采用组合式 API（script setup）'],
               ['Vite', '5.4', '前端构建与开发服务器，启动快、支持代理'],
               ['Vue Router', '4.4', '前端路由，实现 SPA 页面切换与登录守卫'],
               ['Element Plus', '2.8', 'UI 组件库，提供表格、表单、弹窗等组件'],
               ['Axios', '1.7', 'HTTP 客户端，通过拦截器统一加令牌、统一处理错误'],
               ['Spring CORS', '—', '后端声明允许的跨域来源'],
               ['springdoc-openapi', '2.6.0', '由代码注解生成 OpenAPI 文档，配套 Swagger UI（增强）']],
              widths=[3.0, 2.2, 9.5])

    add_body(doc, '''关于 RESTful，我的理解是：过去我编写接口习惯使用动词，如 /getUserList、/addUser，一个动作对应一个地址；RESTful 则相反——地址只用于标识"要操作哪个资源"，采用复数名词（/api/users），而"执行什么操作"交由 HTTP 方法表达：GET 查询、POST 新增、PUT 更新、DELETE 删除。这样同一个 /api/users 配合不同方法即代表不同操作，接口语义更清晰，也更便于他人与工具理解。''')


def design_2(doc):
    add_heading(doc, '3.1 概要设计', 2)
    add_body(doc, '''前后端拆分后的调用关系如下图。开发阶段前端通过 Vite 代理转发请求，从浏览器视角看属于同源请求；生产环境则改用 Nginx 反向代理或后端 CORS 白名单。''')
    add_code(doc, '''┌──────────────────────────────┐
│  浏览器  http://localhost:5173 │
│  Vue3 SPA（Element Plus）      │
└───────────────┬──────────────┘
                │ Axios 请求 /api/**
                │ （开发期由 Vite proxy 转发）
                ▼
┌──────────────────────────────┐
│  Vite Dev Server :5173        │
│  server.proxy → :8080         │
└───────────────┬──────────────┘
                │ 转发（浏览器视角同源）
                ▼
┌──────────────────────────────┐
│  Spring Boot :8080            │
│  Controller → Service → Mapper│
│  CorsConfig 跨域白名单         │
└───────────────┬──────────────┘
                ▼
            MySQL studybuddy''', '图 3-1　前后端分离架构与请求链路')

    add_body(doc, '''跨域处理上我采用了双保险方案，也顺便说明各自的适用场景：''')
    add_bullets(doc, [
        '''开发阶段：通过 Vite 的 server.proxy 将 /api 转发至 8080，浏览器认为请求同源，不会触发跨域，这是最简便的方式；''',
        '''后端兜底：在 CorsConfig 中明确允许的来源（http://localhost:5173），即使前端直连 8080 也不会被拦截；''',
        '''生产环境：更合理的做法是使用 Nginx 反向代理，将 /api 与静态页面置于同一域名下，跨域问题自然消除，后端只需开放精确白名单，不使用通配符。''',
    ])

    add_heading(doc, '3.2 详细设计', 2)
    add_body(doc, '''（1）接口设计。后端接口遵循 RESTful 规范，统一封装为 Result 返回，分页接口使用 PageResult。''')
    add_table(doc,
              ['方法', '路径', '语义', '成功返回 data'],
              [['GET', '/api/hello', '连通性测试', '应用名与时间'],
               ['GET', '/api/users', '分页条件查询', 'PageResult{total, list}'],
               ['GET', '/api/users/{id}', '查询单个用户', 'UserVO'],
               ['POST', '/api/users', '新增用户', '新增后的 UserVO'],
               ['PUT', '/api/users/{id}', '更新用户', '更新后的 UserVO'],
               ['DELETE', '/api/users/{id}', '删除用户', 'null'],
               ['POST', '/api/auth/login', '登录', 'LoginVO{token, user}'],
               ['POST', '/api/ai/chat', '智能问答', 'ChatVO{answer, references}']],
              widths=[1.8, 4.0, 3.2, 5.7])

    add_body(doc, '''上表是实验二原有的八个接口。在功能增强中又补充了下列接口：其中角色、知识库、日志、统计四组仅对拥有相应权限的角色开放（权限编码的划分见实验四），会话三组用于支撑实验五的多轮问答。''')
    add_table(doc,
              ['方法', '路径', '语义', '所需权限'],
              [['GET', '/api/auth/me', '查询当前登录者信息与其权限', '登录即可'],
               ['PUT', '/api/auth/password', '修改本人密码（需校验旧密码）', '登录即可'],
               ['GET', '/api/roles', '角色列表（含权限明细与人数）', 'role:list'],
               ['GET', '/api/roles/permissions', '查询全部权限点', 'role:list'],
               ['PUT', '/api/roles/{id}/permissions', '调整某角色的权限集合', 'role:assign'],
               ['PUT', '/api/users/{id}/roles', '为用户分配角色', 'role:assign'],
               ['GET', '/api/kb/chunks', '查看知识库全部片段', 'kb:manage'],
               ['GET', '/api/kb/stats', '知识库统计（片段数、来源文件等）', 'kb:manage'],
               ['POST', '/api/kb/reload', '重新加载知识库（热更新）', 'kb:manage'],
               ['POST', '/api/kb/chunks', '追加一个知识库片段', 'kb:manage'],
               ['GET', '/api/logs', '分页查询操作审计日志', 'log:list'],
               ['GET', '/api/stats/overview', '首页数据看板统计', 'stats:view'],
               ['GET', '/api/ai/sessions', '查询我的会话列表', 'ai:chat'],
               ['GET', '/api/ai/sessions/{id}/messages', '查询某会话的消息历史', 'ai:chat'],
               ['DELETE', '/api/ai/sessions/{id}', '删除某会话', 'ai:chat']],
              widths=[1.4, 6.0, 4.8, 2.4])

    add_body(doc, '''（2）前端的页面与目录结构。我按照"页面（views）+ 接口（api）+ 路由（router）"的方式组织，将对后端的调用单独抽取为 api 模块，页面中只调用 api 暴露的方法，不直接编写 axios 细节，页面代码因此简洁许多。''')
    add_code(doc, '''studybuddy-frontend/
├── index.html
├── package.json
├── vite.config.js          # 端口 5173 + /api 代理
└── src/
    ├── main.js             # 挂载 Vue + Element Plus + 路由
    ├── App.vue             # 根组件（路由出口）
    ├── assets/main.css     # 全局样式
    ├── utils/
    │   └── auth.js         # 登录态与权限的集中存取（增强）
    ├── api/                # 接口层：与后端一一对应
    │   ├── request.js      # Axios 实例 + 请求/响应拦截器
    │   ├── user.js         # 用户 CRUD 接口
    │   ├── auth.js         # 登录/注册/修改密码接口
    │   ├── ai.js           # 智能问答与会话接口
    │   ├── role.js         # 角色权限接口（增强）
    │   ├── kb.js           # 知识库接口（增强）
    │   ├── log.js          # 操作日志接口（增强）
    │   └── stats.js        # 看板统计接口（增强）
    ├── router/index.js     # 路由表 + 登录守卫 + 页面级权限守卫（增强）
    └── views/
        ├── Login.vue       # 登录页
        ├── Layout.vue      # 侧边栏 + 顶栏布局（菜单按权限渲染）
        ├── Home.vue        # 首页概览（数据看板）
        ├── UserManage.vue  # 用户管理（CRUD + 角色分配）
        ├── Chat.vue        # 学习搭子智能问答（多轮会话）
        ├── RoleManage.vue  # 角色权限（增强）
        ├── KnowledgeBase.vue  # 知识库管理（增强）
        ├── OperationLog.vue   # 操作日志（增强）
        └── Profile.vue     # 个人中心 / 修改密码（增强）''')

    add_body(doc, '''（3）请求处理流程。Axios 拦截器是本实验中设计得较为巧妙的一处：请求拦截器统一附加 JWT 令牌，响应拦截器统一拆解 Result 并处理 401 / 403。有了它，各页面无需重复编写鉴权与错误处理逻辑——最初我在每个页面都重复编写，写到第三个页面时便难以为继。''')
    add_code(doc, '''页面调用 userApi.page()
   → 请求拦截器：从本地存储取 token，加上 Authorization: Bearer xxx
      → 发送到 /api/users（Vite 代理到 8080）
         → 后端返回 {code, message, data}
      → 响应拦截器：code 为 200 直接返回 res；否则报错
         → 若 HTTP 401：清除本地 token 并跳转登录页
   → 页面拿到 res.data.list 渲染表格''', '图 3-2　Axios 请求处理流程')

    add_heading(doc, '3.3 增强一：权限驱动的前端', 2)
    add_body(doc, '''这是本实验在增强阶段改动最大的一处。原先菜单是写死在 Layout.vue 里的，路由守卫也只判断"有没有登录"。由此带来两个问题：其一，一个学生账号同样能看到"用户管理"菜单，点进去才被后端以 403 拦回，体验上很突兀；其二，菜单本身就暴露了系统的功能结构。''')
    add_body(doc, '''增强后的做法是：登录接口在返回令牌的同时，一并下发当前用户的角色编码与权限编码；前端将它们与令牌一起保存，并由 utils/auth.js 这一组函数集中管理（键名与序列化格式只在一处定义，避免散落各处后改一处漏一处）；随后侧边栏菜单与路由的 meta.perm 都改由权限编码决定。下面三段代码依次是权限判断工具、菜单过滤与路由守卫。''')
    add_code(doc, '''// utils/auth.js —— 权限判断（节选）
const PERMS_KEY = 'sb_permissions'

export function currentPermissions() {
  return readJson(PERMS_KEY, [])
}

/** 是否具备某个权限（管理员默认放行） */
export function hasPermission(perm) {
  return currentPermissions().includes(perm) || currentRoles().includes('ROLE_ADMIN')
}''', '代码 3-1　utils/auth.js 权限判断（节选）')

    add_code(doc, '''// Layout.vue —— 菜单按权限过滤，无权则不渲染
const menus = [
  { path: '/home',    label: '首页概览' },
  { path: '/users',   label: '用户管理',   perm: 'user:list' },
  { path: '/roles',   label: '角色权限',   perm: 'role:list' },
  { path: '/chat',    label: '智能问答',   perm: 'ai:chat' },
  { path: '/kb',      label: '知识库管理', perm: 'kb:manage' },
  { path: '/logs',    label: '操作日志',   perm: 'log:list' }
]

const visibleMenus = computed(() => {
  const isAdmin = roles.value.includes('ROLE_ADMIN')
  return menus.filter((m) => !m.perm || isAdmin || permissions.value.includes(m.perm))
})''', '代码 3-2　侧边栏菜单的权限过滤')

    add_code(doc, '''// router/index.js —— 全局前置守卫（节选）
// 注意：此处的拦截只是"体验优化"（避免用户点进一个必然 403 的空页面），
// 真正的权限边界仍在后端 @PreAuthorize —— 前端代码对用户完全可见，
// 任何人都能改掉 meta 或直接调接口。把权限判断只放在前端等于没有权限。
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('sb_token')
  if (to.path !== '/login' && !token) { next('/login'); return }
  if (to.path === '/login' && token)  { next('/home');  return }
  if (to.meta.perm && !currentPermissions().includes(to.meta.perm)) {
    next('/home')       // 无权访问的页面直接回到首页，避免白屏
    return
  }
  next()
})''', '代码 3-3　路由守卫中的页面级权限校验')

    add_body(doc, '''这一段最需要强调的是上面注释里的那层意思：前端拦截只解决"好不好用"，不解决"安不安全"。前端代码在浏览器中完全可见，把它当作安全边界是靠不住的；因此这些页面虽然在前端被隐藏，后端对应的每一个接口仍各自带有 @PreAuthorize，两者是"体验"与"边界"的分工，而非重复。''')

    add_heading(doc, '3.4 增强二：OpenAPI 接口文档', 2)
    add_body(doc, '''实验二使用手写 Markdown 维护接口文档，其固有缺陷是"代码改了，文档未必改"。增强阶段我引入了 springdoc-openapi：它在运行时扫描 Controller 上的 @Tag 与 @Operation 注解，生成符合 OpenAPI 3 规范的结构化描述，并配有一个 Swagger UI 页面，可以直接在浏览器中点选接口、填入参数发起调试。''')
    add_code(doc, '''@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studyBuddyOpenAPI() {
        // 注册 Bearer 令牌方案：否则 Swagger UI 里调不了需要登录的接口
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info().title("学习搭子 StudyBuddy 接口文档")
                        .version("v2.0")
                        .description("《团队规范项目实训》个人项目"))
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}''', '代码 3-4　OpenApiConfig（注册 Bearer 安全方案）')
    add_body(doc, '''这里有两处必须一并处理，否则文档页会"打不开"：其一，要把 /swagger-ui/** 与 /v3/api-docs/** 加入 SecurityConfig 的白名单，因为文档页自身也要经过安全过滤器链，若不放行，它会被自己的 401 拦住；其二，要注册上述 Bearer 安全方案，文档页上才会出现"Authorize"按钮，从而把登录得到的令牌带进请求，调试受保护的接口。本机实测，文档共收录 21 个接口路径，页面与 JSON 描述均正常返回。''')

    add_heading(doc, '3.5 数据库设计', 2)
    add_body(doc, '''本实验未新增数据表，仍使用实验一的 t_user。前端需要展示的时间格式，后端通过 Jackson 统一格式化即可，数据库无需改动。''')


def code_2(doc):
    add_code(doc, '''@Data
public class Result<T> implements Serializable {
    private Integer code;       // 200 成功，其它为失败
    private String message;
    private T data;
    private Long timestamp = System.currentTimeMillis();

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}''', '代码 4-1　统一响应结构 Result.java')
    add_body(doc, '''统一响应结构带来的好处是：前端只需编写一套判断逻辑（先判断 code，再取 data），不必为每个接口编写不同的解析代码。这一点在实验二后期体会尤为明显。''')

    add_code(doc, '''@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users —— 查询列表 */
    @GetMapping
    public Result<PageResult<UserVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) { ... }

    /** POST /api/users —— 新增 */
    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) { ... }

    /** PUT /api/users/{id} —— 更新 */
    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) { ... }

    /** DELETE /api/users/{id} —— 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { ... }
}''', '代码 4-2　RESTful 风格的 UserController')
    add_body(doc, '''@RestController 等价于 @Controller 加 @ResponseBody，返回值会被 Jackson 自动转换为 JSON；@RequestBody 用于将请求体的 JSON 解析为 DTO；@PathVariable 用于获取路径中的变量。这几个注解我起初经常混淆，使用次数多了才逐渐记牢。''')

    add_code(doc, '''@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 精确白名单，绝不用 "*" 配合 allowCredentials=true（会被浏览器拒绝且不安全）
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}''', '代码 4-3　后端 CORS 配置 CorsConfig.java')
    add_body(doc, '''说明几点：① 允许来源我写成了配置项（application.yml 中的 studybuddy.cors.allowed-origins），后续修改无需改动代码；② 这里有一个容易出错的地方——当 allowCredentials 为 true 时，允许来源不能写成通配符 "*"，否则浏览器会直接拒绝该响应，我最初正是这样编写的，排查了较长时间才发现；③ maxAge 用于让浏览器缓存预检结果，减少 OPTIONS 请求次数。''')

    add_code(doc, '''export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: '127.0.0.1',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})''', '代码 4-4　Vite 开发代理（vite.config.js 节选）')

    add_code(doc, '''const request = axios.create({ baseURL: '/api', timeout: 30000 })

// 请求拦截器：自动附加 JWT
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('sb_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一解包 Result + 统一处理 401 / 403 / 404 / 422
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status === 401) {
      // 统一走 clearLogin：避免只清了令牌、权限仍留在本地，
      // 从而出现「菜单还在显示、点进去必然 403」的错位状态
      ElMessage.error(msg || '登录已过期，请重新登录')
      clearLogin()
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error(msg || '权限不足')
    } else if (status === 404) {
      ElMessage.error(msg || '请求的资源不存在')
    } else if (status === 422) {
      ElMessage.warning(msg || '内容未通过安全校验')   // 提示注入拦截
    } else {
      ElMessage.error(msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request''', '代码 4-5　Axios 统一封装 request.js（含增强后的错误分支）')

    add_code(doc, '''export const userApi = {
  page(params)         { return request.get('/users', { params }) },
  detail(id)           { return request.get(`/users/${id}`) },
  create(data)         { return request.post('/users', data) },
  update(id, data)     { return request.put(`/users/${id}`, data) },
  remove(id)           { return request.delete(`/users/${id}`) },
  resetPassword(id, p) { return request.put(`/users/${id}/password`, null, { params: { password: p } }) }
}''', '代码 4-6　前端接口层 user.js（与后端接口一一对应）')

    add_code(doc, '''<template>
  <el-table v-loading="loading" :data="rows" border stripe>
    <el-table-column prop="id" label="ID" width="70" />
    <el-table-column prop="username" label="用户名" width="120" />
    <el-table-column prop="phone" label="手机号(脱敏)" width="140" />
    <el-table-column label="角色" width="180">
      <template #default="{ row }">
        <el-tag v-for="r in row.roleCodes" :key="r" size="small">{{ r }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="220" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        <el-button link type="danger" @click="onDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
const load = async () => {
  loading.value = true
  try {
    const res = await userApi.page({ ...query })   // 调用后端 RESTful 接口
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>''', '代码 4-7　用户管理页面 UserManage.vue 核心（列表渲染与接口调用）')

    add_body(doc, '''（增强）首页看板是"后端聚合、前端只渲染"的典型例子。原先首页的几个数字是写死在页面里的，增强后改为由 /api/stats/overview 一次性返回全部指标，首页只负责展示。后端侧的实现相当直白——每个指标各发一条语义清晰的 COUNT，不为了"一条 SQL 拿全部"而拼复杂子查询：在教学项目的量级下性能差异可以忽略，而可读性与可维护性的收益是明确的。''')
    add_code(doc, '''@Override
public StatsOverviewVO overview() {
    StatsOverviewVO vo = new StatsOverviewVO();
    vo.setUserTotal(userMapper.countAll());
    vo.setUserActive(userMapper.countByStatus(1));
    vo.setRoleCount(roleMapper.countAll());
    vo.setPermCount(permissionMapper.countAll());
    vo.setSessionTotal(sessionMapper.countAll());
    vo.setMessageTotal(messageMapper.countAll());
    vo.setKbChunkCount(ragService.chunkCount());       // 直接问内存里的知识库
    vo.setLogTotal(logMapper.countByQuery(new LogQueryDTO()));
    vo.setLogFailed(logMapper.countFailed());
    vo.setRoleDistribution(userMapper.countByRole());
    return vo;
}''', '代码 4-8　数据看板的聚合实现 StatsServiceImpl.overview')

    add_body(doc, '''这里有一处细节值得说明：统计"角色人数"时，若按"用户表 JOIN 角色表后分组"的常规写法，那些尚无任何用户的角色会因为内连接而被过滤掉、在图表里凭空消失；因此该条 SQL 改为以角色表为主表的 LEFT JOIN，并用 COUNT(u.id) 而非 COUNT(*)，从而让"零人角色"也能正确显示为 0，而不是消失。''')


def run_2(doc):
    add_body(doc, '''本实验的运行验证重点在于三个方面：接口是否按 RESTful 规范运行、前端能否正确渲染、跨域问题是否真正得到解决。''')
    add_screenshot_placeholder(doc, 1,
        '''前端登录页：浏览器访问 http://localhost:5173/login，显示"学习搭子 StudyBuddy"登录表单。''')
    add_screenshot_placeholder(doc, 2,
        '''用户管理页：登录后进入用户管理，表格展示用户列表，含脱敏手机号、角色标签、状态标签与分页器。''')
    add_screenshot_placeholder(doc, 3,
        '''前端调用后端接口成功：浏览器开发者工具 Network 面板可见 GET /api/users 返回 200 与 JSON 数据，响应头含 Access-Control-Allow-Origin。''')
    add_screenshot_placeholder(doc, 4,
        '''跨域预检请求：Network 中可见对 /api/users 的 OPTIONS 预检请求返回 204/200，证明 CORS 配置生效。''')
    add_screenshot_placeholder(doc, 5,
        '''新增 / 编辑弹窗：点击"新增用户"填写表单提交成功，列表自动刷新出现新用户。''')
    add_screenshot_placeholder(doc, 6,
        '''前端生产构建：终端执行 npm run build，输出 dist 目录与构建产物清单（本机实测 1689 modules transformed，无编译错误）。''')
    add_screenshot_placeholder(doc, 7,
        '''（增强）首页数据看板：登录后首页显示用户总数、角色与权限点数量、会话与消息数、知识库片段数、审计日志数与角色分布，数据来自 GET /api/stats/overview。''')
    add_screenshot_placeholder(doc, 8,
        '''（增强）角色权限页：以管理员登录后进入"角色权限"，表格列出三个角色、各自权限明细与人数，可勾选调整某角色的权限。''')
    add_screenshot_placeholder(doc, 9,
        '''（增强）操作日志页：进入"操作日志"，可按模块与结果筛选，表格显示操作人、模块、动作、HTTP 方法、路径、耗时与结果。''')
    add_screenshot_placeholder(doc, 10,
        '''（增强）接口文档：浏览器访问 http://localhost:8080/swagger-ui/index.html，页面按模块列出全部 21 个接口，右上角有 Authorize 按钮可填入令牌调试。''')

    add_body(doc, '''测试结论如下：后端接口均按 RESTful 语义工作；前端通过 Axios 获取数据并正确渲染；跨域方面，开发阶段依靠 Vite 代理、后端再以 CORS 白名单兜底，浏览器控制台无任何 CORS 报错；前端生产构建能正常产出 dist 目录。功能增强完成后，前端视图文件由 5 个增加到 9 个（新增角色权限、知识库管理、操作日志、个人中心），生产构建相应输出 9 个视图分包（另有布局块与公共块），本机实测 1689 个模块转换成功、无编译错误；后端接入 OpenAPI 文档后，访问 /v3/api-docs 与 /swagger-ui/index.html 均返回 200，共收录 21 个接口路径。''')


def reflect_2(doc):
    add_body(doc, '''（一）实验收获''')
    add_bullets(doc, [
        '''理解了前后端分离的本质：后端只关注数据与业务，前端只关注展示与交互，两端通过接口文档这一"契约"对接；''',
        '''理解了跨域的成因（浏览器同源策略），以及代理与 CORS 两种解决方案各自的适用场景；''',
        '''理解了拦截器模式的价值：将鉴权与错误处理收敛到一处，页面代码大幅简化；''',
        '''理解了 RESTful 相较"动词式接口"的优势：语义统一，便于团队协作与接口文档自动化。''',
        '''理解了前端权限与后端权限的分工：前端隐藏菜单与拦截路由只解决"体验"，真正的安全边界始终在后端 @PreAuthorize；把权限判断只放在前端等于没有权限。''',
        '''理解了接口文档"由代码生成"的意义：手写文档迟早会与代码脱节，而 OpenAPI 由注解在运行时生成，代码即文档，改错了会立刻反映到文档上。''',
    ])

    add_body(doc, '''（二）遇到的问题与解决''')
    add_table(doc,
              ['问题现象', '原因定位', '解决办法'],
              [['浏览器报 CORS 错误', '前端 5173 直连后端 8080，来源不同',
                '开发期用 Vite proxy，同时后端配置精确来源白名单'],
               ['配置了 CORS 仍被浏览器拒绝', 'allowedOrigins 用了通配符且 allowCredentials 为 true',
                '改为精确来源列表 http://localhost:5173'],
               ['响应拦截器取不到 data', '拦截器返回的是整个 Result 而非 response.data',
                '统一在拦截器里返回 res，页面直接解构 res.data'],
               ['登录过期后页面白屏', '401 未统一处理',
                '响应拦截器捕获 401，清除 token 并跳转登录页'],
               ['（增强）退出后菜单仍显示，点进去却 403', '401 时只清了令牌，权限编码仍留在本地',
                '统一改走 clearLogin() 清除全部登录态键值']],
              widths=[4.5, 5.0, 5.2])

    add_body(doc, '''（三）AI 工具使用记录''')
    add_table(doc,
              ['环节', '使用的 AI 工具', '提示词要点', '产出如何验证'],
              [['生成 Vue 组件骨架', 'AI 编程助手',
                'Element Plus 表格 + 分页 + 新增/编辑弹窗，组合式 API', '本地 npm run dev 实测页面与交互'],
               ['生成 Axios 封装', 'AI 编程助手',
                '请求拦截器加 token、响应拦截器解包 Result 并处理 401',
                '人工核对错误分支；实测令牌注入与 401 跳转'],
               ['生成 CORS 配置', 'AI 编程助手',
                'Spring Boot 3 的 CorsConfigurationSource 写法',
                '人工修正：AI 初版用通配符来源并开启凭证，会导致浏览器拒绝'],
               ['生成接口文档', 'AI 编程助手', '按现有 Controller 生成 Markdown 接口文档',
                '逐字段与源码比对，并补充错误码说明']],
              widths=[3.2, 2.8, 5.0, 4.5])

    add_body(doc, '''（四）反思与边界意识''')
    add_body(doc, '''本实验暴露的一个典型问题让我印象较深：AI 给出的 CORS 配置"能够编译通过"，但在浏览器中会被直接拒绝——因为通配符来源与凭证模式互斥。也就是说，判断 AI 的产出是否正确，仅看"能否编译"是不够的，必须放到真实环境中端到端运行才能确认。此外，接口文档同样由 AI 生成，后来我发现其中一个错误码写错，对照源码才得以改正——文档与代码是否一致，同样只能依靠人工核对。''')
    add_body(doc, '''功能增强阶段还让我体会到一点：界面上的缺失往往在不知不觉中被忽略。例如"用户管理"页只有增删改查，却没有"给用户分配角色"这一步——因为最初的版本里角色是写死的，并不需要界面；直到我在页面上试着把一个学生改成教师，才发现根本无处可点。可见缺口常常不是靠读代码读出来的，而是在"真的去用一次"时才浮现。''')
    add_body(doc, '''本实验的 AI 使用仍属于开发辅助（生成组件与文档），未涉及模型能力本身，因此没有幻觉案例；大模型在运行时的局限性留待实验五讨论。''')


# ======================================================================
# 实验三
# ======================================================================
INTRO_3 = [
    '''实验三的目标是掌握团队协作中的版本控制与持续集成规范：通过 Git 分支模型管理并行开发，通过 Pull Request（PR）与 Code Review 保障代码质量，并引入基础 CI（GitHub Actions）在合并前自动完成构建与测试。''',
    '''由于本次是个人实训，为了尽量贴近团队协作的真实场景，我依照团队规范搭建了分支模型、PR 模板与 CI 流水线，并以"扮演两个角色"的方式演练了分支合并与冲突解决。此处需要先作说明：真正意义上的多人协作我并未经历过，下文会明确标注哪些是实际完成的、哪些是在本地模拟的，不作夸大。''',
    '''我为实验三设定的验收标准是：① 仓库具备明确的分支模型与命名规范；② 有 PR 模板并写明评审清单；③ CI 能在 push / PR 时自动完成编译与测试；④ 能够独立解决一次合并冲突。''',
]


def tech_3(doc):
    add_table(doc,
              ['技术 / 规范', '说明'],
              [['Git', '分布式版本控制系统，本机版本 2.55.0'],
               ['分支模型', '简化 Git Flow：main / develop / feature / hotfix'],
               ['Conventional Commits', '规范化提交信息，便于生成变更日志'],
               ['Pull Request', '变更合并的唯一入口，必须经过评审'],
               ['GitHub Actions', 'CI/CD 平台，用 YAML 描述流水线'],
               ['Maven verify', '后端 CI 目标：编译 + 单元测试 + 打包'],
               ['npm ci / build', '前端 CI 目标：按 lock 文件安装 + 生产构建']],
              widths=[4.0, 10.0])

    add_body(doc, '''谈谈我对 CI 的理解：在没有 CI 的项目中，集成问题往往积压到临近发布时才集中暴露，此时的排查成本会显著升高。CI 的核心作用就是将其提前——每次提交都自动执行一遍编译与测试，数秒内即可获知结果，而不必等到数日之后才发现主干无法编译。''')
    add_body(doc, '''此外有一点需特别说明，我认为这也是本实验最贴近真实的部分：项目由我一人完成，因此"两人修改同一文件同一处并解决冲突"的情形，我实际上并未真正遇到。我所做的是——将协作规范落实为仓库中可见的产物（分支模型文档、PR 模板、CI 流水线），再在本地仓库上完整模拟了一遍标准流程。之所以如实写明，是因为我不希望营造出经历过多人协作的印象，否则经不起追问。''')


def design_3(doc):
    add_heading(doc, '3.1 分支模型设计', 2)
    add_code(doc, '''main        ●────────────────●────────────●─────────►  仅存放可发布版本（受保护）
             ↑                ↑            ↑
             │  Merge PR      │            │
develop     ●───●───●────●────●───●────●───●─────────►  集成分支，随时可构建
                 ↑    ↑        ↑
feature/*   ●────●    ●────────●                       功能分支，一事一分支
hotfix/*             （从 main 拉出，修复后同时合回 main 与 develop）''', '图 3-1　简化 Git Flow 分支模型')

    add_body(doc, '''课程五个实验对应五个功能分支，命名与提交信息均可一一对应，映射关系如下。''')
    add_table(doc,
              ['实验', '功能分支', '代表性提交信息'],
              [['实验一', 'feature/exp1-springboot-crud', 'feat(exp1): 完成后端工程骨架与用户表 CRUD'],
               ['实验二', 'feature/exp2-vue-restful', 'feat(exp2): 新增 Vue3 前端与 RESTful 联调'],
               ['实验三', 'feature/exp3-git-ci', 'chore(ci): 添加后端与前端双 job 流水线'],
               ['实验四', 'feature/exp4-jwt-rbac', 'feat(exp4): 新增 JWT 登录与 RBAC 鉴权'],
               ['实验五', 'feature/exp5-llm-rag', 'feat(exp5): 接入大模型并实现 RAG 检索增强'],
               ['增强', 'feature/v2-enhancements', 'feat(v2): 角色管理、自助改密、多轮会话、审计日志等七项增强']],
              widths=[2.0, 5.5, 6.5])

    add_body(doc, '''最后一行的 v2 增强分支正是按同一套规范走的：由于它不属于任何一个实验，若直接改在 develop 上，一旦中途出问题便难以回退。因此我同样从 develop 拉出功能分支，在分支上完成开发与自测，再以一次合并提交回到 develop，使"实验增量"与"增强增量"在提交历史中清晰可辨，而非混作一团。''')

    add_body(doc, '''关于命名的含义，我的理解是：feature/ 前缀表示这是一个功能分支，exp1 表示所属实验，其后为简短的英文描述。如此一眼即可看出该分支的用途及所属实验，比 branch1、test 之类的命名清晰得多。提交信息中的 feat: / chore: 前缀是 Conventional Commits 的规定，feat 表示新功能，chore 表示杂项（如修改配置）。''')

    add_heading(doc, '3.2 CI 流水线设计', 2)
    add_body(doc, '''CI 流水线包含两个并行执行的 job：后端（JDK 21 + Maven verify）与前端（Node 22 + npm build）。只要其中一个失败，PR 便无法合并。''')
    add_code(doc, '''触发：push 到 main/develop，或向 main/develop 发起 PR
   │
   ├──► job: backend-build（ubuntu-latest）
   │       checkout → setup-java(21) → mvn -B clean verify → 上传 jar 产物
   │
   └──► job: frontend-build（ubuntu-latest）
           checkout → setup-node(22) → npm ci → npm run build''', '图 3-2　CI 流水线结构')

    add_heading(doc, '3.3 仓库目录与文档规范', 2)
    add_code(doc, '''studybuddy/
├── .github/
│   ├── workflows/ci.yml            # CI 流水线定义
│   └── pull_request_template.md    # PR 模板（含评审清单与 AI 使用声明）
├── .gitignore                      # 忽略 target/ node_modules/ 以及敏感配置
├── README.md                       # 项目说明与复现指引
├── docs/
│   ├── 分支模型与协作规范.md
│   ├── AI使用公约与记录.md
│   └── 接口文档.md
├── studybuddy-backend/             # 后端
├── studybuddy-frontend/            # 前端
└── ai-demo/                        # 实验五演示脚本''')

    add_body(doc, '''文档规范的要求是：README 说明如何运行，接口文档说明契约，AI 使用公约说明哪些环节可以使用 AI、如何标注、如何复核。其中"AI 使用公约"是本课程新增的团队规范，我理解其目的是将 AI 的使用控制在可控范围内，避免将整个模块直接交由 AI 而不再审查。''')


def code_3(doc):
    add_code(doc, '''name: CI
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  backend-build:
    name: 后端编译与测试
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: studybuddy-backend
    steps:
      - name: 拉取代码
        uses: actions/checkout@v4
      - name: 安装 JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - name: Maven 编译 + 单元测试
        # -B 批处理模式；verify 会执行 test 阶段，测试不通过则流水线失败
        run: mvn -B clean verify
      - name: 上传构建产物（可运行的 jar）
        uses: actions/upload-artifact@v4
        with:
          name: studybuddy-backend-jar
          path: studybuddy-backend/target/*.jar
          if-no-files-found: warn

  frontend-build:
    name: 前端构建
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: studybuddy-frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: npm
          cache-dependency-path: studybuddy-frontend/package-lock.json
      - run: npm ci
      - run: npm run build''', '代码 4-1　.github/workflows/ci.yml 完整内容')
    add_body(doc, '''三个要点：① cache 相关配置用于复用 Maven 与 npm 的依赖缓存，可显著缩短流水线耗时；② mvn verify 会执行 test 阶段，测试失败则流水线中断，这正是"以测试守门"；③ 前端使用 npm ci 而非 npm install——npm ci 严格按照 lock 文件安装，能够保证每次构建的依赖版本完全一致，CI 最忌讳"上次能过、这次不能过"，因此必须使用它。''')

    add_code(doc, '''## 自测清单
- [ ] 本地 `mvn -B clean verify` 通过
- [ ] 本地前端 `npm run build` 通过
- [ ] 新接口已在 docs/接口文档.md 中补充
- [ ] 无密钥、密码、Token 等敏感信息被提交

## AI 使用声明（本课程强制）
- [ ] 本次变更使用了 AI 辅助（若是，请在下方记录：工具、环节、提示词要点、验证方式）
- [ ] AI 生成的关键代码已逐行理解，并添加了 `// AI-assisted` 注释
- [ ] 关键决策（架构、安全）由人工确认''', '代码 4-2　PR 模板节选（评审清单 + AI 使用声明）')

    add_code(doc, '''# 1. 初始化仓库并首次提交
git init
git add .
git commit -m "chore: 初始化 StudyBuddy 项目结构"

# 2. 关联远程并推送主干
git remote add origin git@github.com:<your-name>/studybuddy.git
git branch -M main
git push -u origin main

# 3. 从 develop 拉出功能分支开发（一事一分支）
git checkout -b feature/exp4-jwt-rbac develop
# ...开发...
git add .
git commit -m "feat(exp4): 新增 JWT 登录与 RBAC 鉴权"
git push -u origin feature/exp4-jwt-rbac

# 4. 保持与主干同步，减少冲突规模
git fetch origin
git rebase origin/develop

# 5. 查看提交历史（用于报告截图）
git log --oneline --graph --all --decorate''', '代码 4-3　实验三核心 Git 命令')

    add_code(doc, '''# 冲突产生
git checkout feature/a
git merge develop
# CONFLICT (content): Merge conflict in README.md

# 冲突文件中的标记（必须人工协商后决定保留哪一版）
# <<<<<<< HEAD
#   成员 A 的版本
# =======
#   成员 B 的版本
# >>>>>>> develop

# 解决后提交
git add README.md
git commit -m "merge: 解决 README 冲突，保留成员 A 的措辞"''', '代码 4-4　合并冲突的识别与解决')
    add_body(doc, '''这段冲突代码是我在本地模拟产生的。做法是自己修改同一文件以制造一次冲突，再手工解决。解决过程本身并不复杂：Git 只会在冲突处插入 <<<<<<<、=======、>>>>>>> 标记，将两个版本并列展示，但保留哪一版由开发者自行判断——需要阅读代码、判断哪一版在语义上正确，删除标记后再提交。我最初以为会有工具自动完成选择，后来才明白这一步只能由人判断，因为它涉及"代码语义是否正确"，而非"格式是否规范"。''')


def run_3(doc):
    add_body(doc, '''本实验的运行验证围绕四个方面展开：分支模型可追溯、PR 流程完整、CI 通过、冲突可解决。''')
    add_screenshot_placeholder(doc, 1,
        '''分支图：终端执行 git log --oneline --graph --all --decorate，展示 main / develop / feature 分支的演进关系。''')
    add_screenshot_placeholder(doc, 2,
        '''分支列表：git branch -a 显示所有本地与远程分支，命名符合 feature/expN-xxx 规范。''')
    add_screenshot_placeholder(doc, 3,
        '''Pull Request 页面：在 GitHub 上发起 PR，标题与描述按模板填写，显示"自测清单"与"AI 使用声明"已勾选。''')
    add_screenshot_placeholder(doc, 4,
        '''CI 执行结果：Actions 页面显示 backend-build 与 frontend-build 两个 job 均通过（绿色对勾），并展示 mvn verify 的测试统计（Tests run: 20, Failures: 0）。''')
    add_screenshot_placeholder(doc, 5,
        '''合并冲突与解决：终端展示 CONFLICT 提示与文件中的冲突标记，以及解决后 git commit 成功、git log 显示 merge 提交。''')
    add_screenshot_placeholder(doc, 6,
        '''代码评审记录：PR 的 Files changed 页面，说明 AI 辅助评审意见与人工评审结论（人工为最终责任方）。''')

    add_body(doc, '''结论如下：分支模型清晰、可追溯；PR 流程与模板齐备；CI 能在 push / PR 时自动跑通后端编译测试（本机实测 20 项测试全部通过）与前端构建；合并冲突能够被正确识别并手工解决。需要再次说明的是，第 3、6 张截图中的 PR 与评审为本地模拟流程，不代表真实的多人协作。''')


def reflect_3(doc):
    add_body(doc, '''（一）实验收获''')
    add_bullets(doc, [
        '''理解了分支模型的意义：main 随时可发布，日常开发在 develop 与 feature 上进行，降低了主干被污染的风险；''',
        '''理解了 PR 不仅是一次合并入口，更是知识共享与质量把关的机制；''',
        '''理解了 CI 的价值在于"将集成问题提前暴露"，而非等到发布前集中排查；''',
        '''理解了规范化的提交信息（Conventional Commits）能让提交历史本身具备可读性；''',
        '''（增强阶段）体会到分支模型在"计划外的迭代"上同样有效：v2 增强并不属于任何一个实验，但依然从 develop 拉出功能分支、自测通过后再合并，提交历史因此能清楚分辨"实验增量"与"增强增量"。''',
    ])

    add_body(doc, '''（二）遇到的问题与解决''')
    add_table(doc,
              ['问题现象', '原因定位', '解决办法'],
              [['CI 上 mvn 下载依赖极慢或超时', '未配置缓存', 'setup-java 开启 cache: maven'],
               ['前端 CI 构建失败', 'lock 文件未提交，npm ci 无依据',
                '提交 package-lock.json 并统一使用 npm ci'],
               ['误把 target/ 提交进仓库', '.gitignore 未覆盖或已误提交',
                '完善 .gitignore；对已跟踪文件执行 git rm -r --cached target'],
               ['合并时冲突标记被误提交', '解决冲突后未检查文件内容',
                '提交前 git diff 复查，确认无 <<<<<< 残留']],
              widths=[4.5, 5.0, 5.2])

    add_body(doc, '''（三）AI 工具使用记录''')
    add_table(doc,
              ['环节', '使用的 AI 工具', '提示词要点', '产出如何验证'],
              [['生成 CI 流水线', 'AI 编程助手',
                'JDK21 后端 verify 加 Node22 前端 build 的双 job 配置',
                '本地执行等价命令验证；YAML 缩进人工检查'],
               ['生成 PR 模板', 'AI 编程助手',
                '评审清单加本课程要求的 AI 使用声明',
                '与课程方案"AI 使用公约"逐条比对，补齐 3 项'],
               ['AI 辅助代码评审', 'AI 编程助手',
                '贴出 diff，要求指出潜在 bug 与安全风险',
                '仅作初步意见；人工评审为最终责任方，AI 提的 2 条中采纳 1 条'],
               ['生成协作规范文档', 'AI 编程助手', '分支模型与提交信息规范',
                '人工按实际使用的命令逐条核对']],
              widths=[3.2, 2.8, 5.0, 4.5])

    add_body(doc, '''（四）反思与边界意识''')
    add_body(doc, '''本实验让我体会最深的是"责任界定"这一问题。AI 能够迅速给出一批评审意见，其中一条我认为有价值——它提示"用户删除接口缺少幂等性考虑"，我予以采纳，补充了重复删除的处理；但它同时建议"将密码改为 MD5 以提升性能"，这一条明显错误，MD5 早已被证明不安全，绝不能用于存储密码。这说明 AI 的建议必须经过人的专业判断，安全相关的决策尤其不能交由 AI。此外，AI 生成 CI 配置时首次将 JDK 版本写成 11，与 Spring Boot 3 最低要求的 17 不符，属于典型的"版本幻觉"，依靠本地实跑才暴露出来。''')
    add_body(doc, '''对照课程所述的"团队 AI 使用公约"，我在本实验中落实了三件事：① 所有 AI 生成的代码均添加 // AI-assisted 注释；② PR 模板强制填写 AI 使用记录；③ 明确"AI 可以辅助评审，但人工才是最终责任方"。''')
