CREATE TABLE security_sys_role
(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '自增id',
    name VARCHAR(64) COMMENT '角色名称',
    app_id VARCHAR(64) COMMENT '角色属于哪个工程，或者说哪个应用',
    remark VARCHAR(64) COMMENT '描述',
    version BIGINT UNSIGNED DEFAULT 0 COMMENT '版本号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更改时间',
    -- UNIQUE INDEX `uk_field1_field2` (`field1`, `field2`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    INDEX `idx_app_id` (`app_id`)
)COMMENT='系统角色管理' AUTO_INCREMENT=1;

CREATE TABLE security_sys_resource
(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '自增id',
    name VARCHAR(64) COMMENT '资源名称',
    app_id VARCHAR(64) COMMENT '资源属于哪个工程，或者说哪个应用',
    remark VARCHAR(64) COMMENT '描述',
    version BIGINT UNSIGNED DEFAULT 0 COMMENT '版本号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更改时间',
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    INDEX `idx_app_id` (`app_id`)
)COMMENT='系统资源管理' AUTO_INCREMENT=1;

CREATE TABLE security_sys_user
(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '自增id',
    user_account VARCHAR(64) COMMENT '登陆账户',
    password VARCHAR(64) COMMENT '登陆密码',
    user_name VARCHAR(64) COMMENT '用户姓名',
    phone VARCHAR(64) COMMENT '手机',
    is_super_admin BOOLEAN COMMENT '超管',
    remark VARCHAR(64) COMMENT '描述',
    version BIGINT UNSIGNED DEFAULT 0 COMMENT '版本号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更改时间',
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`)
)COMMENT='系统用户管理' AUTO_INCREMENT=1;

CREATE TABLE security_sys_role_perm
(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '自增id',
    role_id BIGINT UNSIGNED COMMENT '角色id',
    resource_id BIGINT UNSIGNED COMMENT '资源id',
    app_id VARCHAR(64) COMMENT '哪个应用，冗余，方便查询',
    perm_num BIGINT UNSIGNED COMMENT '权限值',
    version BIGINT UNSIGNED DEFAULT 0 COMMENT '版本号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更改时间',
    UNIQUE INDEX `uk_role_id_resource_id` (`role_id`, `resource_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    INDEX `idx_app_id` (`app_id`)
)COMMENT='系统角色权限表' AUTO_INCREMENT=1;

CREATE TABLE security_sys_user_role_rel
(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '自增id',
    user_id BIGINT UNSIGNED COMMENT '用户id',
    role_id BIGINT UNSIGNED COMMENT '角色ID',
    app_id VARCHAR(64) COMMENT '哪个应用，冗余，方便查询',
    version BIGINT UNSIGNED DEFAULT 0 COMMENT '版本号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更改时间',
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    INDEX `idx_user_id_app_id` (`user_id`,`app_id`)
)COMMENT='系统用户角色关联表' AUTO_INCREMENT=1;

INSERT INTO security_sys_user VALUES(0, 'admin','f348484022a63c51031de2e5782360e8','super','13333333333',1,'默认超管用户',0,now(),now());

