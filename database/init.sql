-- =====================================================
-- 智慧公安管理系统数据库初始化脚本 (PostgreSQL)
-- =====================================================

-- 机构表
DROP TABLE IF EXISTS sys_org CASCADE;
CREATE TABLE sys_org (
    id BIGSERIAL PRIMARY KEY,
    org_code VARCHAR(50) NOT NULL UNIQUE,
    org_name VARCHAR(100) NOT NULL,
    org_type VARCHAR(20) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    province VARCHAR(50),
    city VARCHAR(50),
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    status SMALLINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 部门表
DROP TABLE IF EXISTS sys_dept CASCADE;
CREATE TABLE sys_dept (
    id BIGSERIAL PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL UNIQUE,
    dept_name VARCHAR(100) NOT NULL,
    org_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    dept_type VARCHAR(20),
    manager_id BIGINT,
    status SMALLINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 用户表
DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    id_card VARCHAR(18),
    org_id BIGINT,
    dept_id BIGINT,
    position VARCHAR(50),
    avatar VARCHAR(255),
    gender SMALLINT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    is_admin SMALLINT DEFAULT 0,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 角色表
DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    role_type VARCHAR(20) DEFAULT 'CUSTOM',
    description VARCHAR(500),
    org_id BIGINT,
    status SMALLINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 菜单表
DROP TABLE IF EXISTS sys_menu CASCADE;
CREATE TABLE sys_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(100) NOT NULL,
    menu_code VARCHAR(50),
    menu_type SMALLINT DEFAULT 1,
    path VARCHAR(255),
    component VARCHAR(255),
    icon VARCHAR(100),
    permission VARCHAR(100),
    sort_order INT DEFAULT 0,
    visible SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- API表
DROP TABLE IF EXISTS sys_api CASCADE;
CREATE TABLE sys_api (
    id BIGSERIAL PRIMARY KEY,
    api_name VARCHAR(100) NOT NULL,
    api_path VARCHAR(255) NOT NULL,
    api_method VARCHAR(10) NOT NULL,
    api_group VARCHAR(100),
    permission VARCHAR(100),
    description VARCHAR(500),
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu CASCADE;
CREATE TABLE sys_role_menu (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色API关联表
DROP TABLE IF EXISTS sys_role_api CASCADE;
CREATE TABLE sys_role_api (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
DROP TABLE IF EXISTS sys_operation_log CASCADE;
CREATE TABLE sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    status SMALLINT DEFAULT 1,
    error_msg TEXT,
    exec_time BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. 操作风险模块表
-- =====================================================

-- 法律文书类型表
DROP TABLE IF EXISTS ops_legal_doc_type CASCADE;
CREATE TABLE ops_legal_doc_type (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    field_config TEXT,
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 法律文书表
DROP TABLE IF EXISTS ops_legal_document CASCADE;
CREATE TABLE ops_legal_document (
    id BIGSERIAL PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL UNIQUE,
    doc_type_id BIGINT,
    doc_title VARCHAR(255),
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT,
    extract_content TEXT,
    ai_confidence DECIMAL(5,2),
    org_name VARCHAR(200),
    amount DECIMAL(18,2),
    status SMALLINT DEFAULT 0,
    confirm_time TIMESTAMP,
    remark VARCHAR(500),
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- =====================================================
-- 3. AI智能助手模块表
-- =====================================================

-- 知识库表
DROP TABLE IF EXISTS ai_knowledge_base CASCADE;
CREATE TABLE ai_knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    kb_name VARCHAR(100) NOT NULL,
    kb_code VARCHAR(50) UNIQUE,
    kb_type VARCHAR(50),
    description VARCHAR(500),
    doc_count INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 知识库文档表
DROP TABLE IF EXISTS ai_knowledge_document CASCADE;
CREATE TABLE ai_knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    doc_name VARCHAR(255) NOT NULL,
    doc_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    chunk_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 知识库分块表（使用pgvector）
DROP TABLE IF EXISTS ai_knowledge_chunk CASCADE;
CREATE TABLE ai_knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding VECTOR(1536),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文档分块表（使用JSON存储向量，不依赖pgvector）
DROP TABLE IF EXISTS ai_document_chunk CASCADE;
CREATE TABLE ai_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL,
    kb_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding TEXT,
    char_count INT DEFAULT 0,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_ai_doc_chunk_doc ON ai_document_chunk(doc_id);
CREATE INDEX idx_ai_doc_chunk_kb ON ai_document_chunk(kb_id);

-- 聊天会话表
DROP TABLE IF EXISTS ai_chat_session CASCADE;
CREATE TABLE ai_chat_session (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    kb_id BIGINT,
    title VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天消息表
DROP TABLE IF EXISTS ai_chat_message CASCADE;
CREATE TABLE ai_chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    sources TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. 智能导航模块表
-- =====================================================

-- 导航意图表
DROP TABLE IF EXISTS nav_intent CASCADE;
CREATE TABLE nav_intent (
    id BIGSERIAL PRIMARY KEY,
    intent_code VARCHAR(50) NOT NULL UNIQUE,
    intent_name VARCHAR(100) NOT NULL,
    keywords TEXT,
    target_path VARCHAR(255),
    target_name VARCHAR(100),
    description VARCHAR(500),
    priority INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 导航历史表
DROP TABLE IF EXISTS nav_history CASCADE;
CREATE TABLE nav_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    input_text VARCHAR(500),
    intent_code VARCHAR(50),
    target_path VARCHAR(255),
    success SMALLINT DEFAULT 1,
    message VARCHAR(500),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 5. 刑侦研判模块表
-- =====================================================

-- 重点人员表
DROP TABLE IF EXISTS fraud_suspicious_customer CASCADE;
CREATE TABLE fraud_suspicious_customer (
    id BIGSERIAL PRIMARY KEY,
    customer_no VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    id_type VARCHAR(20) DEFAULT 'ID_CARD',
    id_no VARCHAR(18),
    gender SMALLINT,
    birth_date DATE,
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    customer_type VARCHAR(50),
    risk_level VARCHAR(20),
    risk_score DECIMAL(5,2),
    blacklist_flag SMALLINT DEFAULT 0,
    watchlist_flag SMALLINT DEFAULT 0,
    suspicious_type VARCHAR(100),
    first_suspicious_time TIMESTAMP,
    last_suspicious_time TIMESTAMP,
    suspicious_count INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 资金流水表
DROP TABLE IF EXISTS fraud_transaction CASCADE;
CREATE TABLE fraud_transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_no VARCHAR(100) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_no VARCHAR(50),
    counter_account_no VARCHAR(50),
    counter_account_name VARCHAR(100),
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    balance_before DECIMAL(18,2),
    balance_after DECIMAL(18,2),
    transaction_time TIMESTAMP NOT NULL,
    channel VARCHAR(50),
    device_id VARCHAR(100),
    ip_address VARCHAR(50),
    location VARCHAR(200),
    remark VARCHAR(500),
    risk_flag SMALLINT DEFAULT 0,
    risk_score DECIMAL(5,2),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 行为数据表
DROP TABLE IF EXISTS fraud_behavior CASCADE;
CREATE TABLE fraud_behavior (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    behavior_type VARCHAR(50),
    behavior_time TIMESTAMP,
    behavior_detail TEXT,
    risk_level VARCHAR(20),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 案例分析表
DROP TABLE IF EXISTS fraud_case_analysis CASCADE;
CREATE TABLE fraud_case_analysis (
    id BIGSERIAL PRIMARY KEY,
    case_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    case_type VARCHAR(50),
    case_source VARCHAR(50),
    suspicious_points TEXT,
    analysis_content TEXT,
    analysis_report TEXT,
    confidence DECIMAL(5,2),
    status SMALLINT DEFAULT 0,
    handler_id BIGINT,
    handle_time TIMESTAMP,
    handle_result VARCHAR(500),
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- =====================================================
-- 6. 反洗钱模块表
-- =====================================================

-- 人员尽调表
DROP TABLE IF EXISTS aml_customer_due_diligence CASCADE;
CREATE TABLE aml_customer_due_diligence (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    dd_type VARCHAR(50),
    dd_reason VARCHAR(500),
    customer_info TEXT,
    beneficial_owner TEXT,
    business_nature VARCHAR(500),
    fund_source VARCHAR(500),
    risk_assessment TEXT,
    ai_analysis TEXT,
    ai_suggestions TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_time TIMESTAMP,
    review_result VARCHAR(500),
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 可疑交易报告表
DROP TABLE IF EXISTS aml_suspicious_transaction_report CASCADE;
CREATE TABLE aml_suspicious_transaction_report (
    id BIGSERIAL PRIMARY KEY,
    report_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    id_card VARCHAR(18),
    transaction_count INT,
    total_amount DECIMAL(18,2),
    alert_type VARCHAR(100),
    suspicious_types VARCHAR(200),
    analysis_result TEXT,
    report_content TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    reporter_id BIGINT,
    report_time TIMESTAMP,
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 风险评分表
DROP TABLE IF EXISTS aml_risk_score CASCADE;
CREATE TABLE aml_risk_score (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    score_type VARCHAR(50),
    score_value INT,
    score_detail TEXT,
    factors TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 创建索引
-- =====================================================
CREATE INDEX idx_sys_user_username ON sys_user(username);
CREATE INDEX idx_sys_user_org ON sys_user(org_id);
CREATE INDEX idx_sys_menu_parent ON sys_menu(parent_id);
CREATE INDEX idx_ops_legal_doc_type ON ops_legal_document(doc_type_id);
CREATE INDEX idx_ai_knowledge_doc_kb ON ai_knowledge_document(kb_id);
CREATE INDEX idx_ai_chat_msg_session ON ai_chat_message(session_id);

-- 刑侦研判模块索引
CREATE INDEX idx_fraud_customer_no ON fraud_suspicious_customer(customer_no);
CREATE INDEX idx_fraud_customer_name ON fraud_suspicious_customer(customer_name);
CREATE INDEX idx_fraud_customer_risk ON fraud_suspicious_customer(risk_level);
CREATE INDEX idx_fraud_customer_status ON fraud_suspicious_customer(status);

CREATE INDEX idx_fraud_trans_customer ON fraud_transaction(customer_id);
CREATE INDEX idx_fraud_trans_time ON fraud_transaction(transaction_time);
CREATE INDEX idx_fraud_trans_type ON fraud_transaction(transaction_type);
CREATE INDEX idx_fraud_trans_risk ON fraud_transaction(risk_flag);

CREATE INDEX idx_fraud_case_customer ON fraud_case_analysis(customer_id);
CREATE INDEX idx_fraud_case_no ON fraud_case_analysis(case_no);
CREATE INDEX idx_fraud_case_status ON fraud_case_analysis(status);

-- 反洗钱模块索引
CREATE INDEX idx_aml_dd_customer ON aml_customer_due_diligence(customer_id);