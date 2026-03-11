-- =====================================================
-- 智慧公安管理系统测试数据 (PostgreSQL)
-- =====================================================

-- INSERT 用户数据 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, email, phone, org_id, gender, status, is_admin, created_by) VALUES
('admin', '$2a$10$YYrTK4.jZn3E9ZMvo4g2wOaJPP3TGti1ijgUPPJkJwz4349bjuxNC', '系统管理员', 'admin@police.gov', '13800138000', 1, 1, 1, 1, 'system'),
('zhangwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张伟', 'zhangwei@police.gov', '13800138001', 2, 1, 1, 0, 'admin'),
('liming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李明', 'liming@police.gov', '13800138002', 3, 1, 1, 0, 'admin'),
('wangfang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王芳', 'wangfang@police.gov', '13800138003', 4, 2, 1, 0, 'admin'),
('chenxiao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '陈晓', 'chenxiao@police.gov', '13800138004', 5, 2, 1, 0, 'admin'),
('liuyang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '刘洋', 'liuyang@police.gov', '13800138005', 2, 1, 1, 0, 'admin');

-- INSERT 机构数据
INSERT INTO sys_org (org_code, org_name, org_type, parent_id, level, province, city, address, contact_phone, status, created_by) VALUES
('HEAD_OFFICE', '省公安厅', 'HEAD', 0, 1, '北京', '北京', '北京市朝阳区建国门大街1号', '010-88888888', 1, 'system'),
('BJ_BUREAU', '市公安局', 'BRANCH', 1, 2, '北京', '北京', '北京市朝阳区建国路88号', '010-66666666', 1, 'system'),
('SH_BUREAU', '市公安局', 'BRANCH', 1, 2, '上海', '上海', '上海市浦东新区陆家嘴', '021-55555555', 1, 'system'),
('SZ_BUREAU', '市公安局', 'BRANCH', 1, 2, '广东', '深圳', '深圳市福田区深南大道', '0755-44444444', 1, 'system'),
('GZ_BUREAU', '市公安局', 'BRANCH', 1, 2, '广东', '广州', '广州市天河区珠江新城', '020-33333333', 1, 'system');

-- INSERT 角色数据
INSERT INTO sys_role (role_code, role_name, role_type, description, status, created_by) VALUES
('ROLE_ADMIN', '系统管理员', 'ADMIN', '系统最高权限管理员', 1, 'system'),
('ROLE_CASE_MGR', '案件管理员', 'CASE_MGR', '案件管理权限', 1, 'system'),
('ROLE_INVESTIGATOR', '侦查员', 'CUSTOM', '案件侦查权限', 1, 'system'),
('ROLE_AUDITOR', '督察员', 'AUDITOR', '督察查看权限', 1, 'system'),
('ROLE_BRANCH_LEADER', '分局领导', 'CUSTOM', '分局管理权限', 1, 'system'),
('ROLE_ANALYST', '情报分析员', 'CUSTOM', '情报分析权限', 1, 'system');

-- INSERT 菜单数据
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, path, component, icon, permission, sort_order, status, created_by) VALUES
(0, '工作台', 'dashboard', 1, '/dashboard', 'views/dashboard/index.vue', 'HomeOutlined', 'dashboard:view', 1, 1, 'system'),
(0, '系统管理', 'system', 0, '/system', 'Layout', 'SettingOutlined', 'system:view', 2, 1, 'system'),
(2, '用户管理', 'system:user', 1, '/system/user', 'views/system/user/index.vue', 'UserOutlined', 'system:user:list', 1, 1, 'system'),
(2, '角色管理', 'system:role', 1, '/system/role', 'views/system/role/index.vue', 'TeamOutlined', 'system:role:list', 2, 1, 'system'),
(2, '机构管理', 'system:org', 1, '/system/org', 'views/system/org/index.vue', 'BankOutlined', 'system:org:list', 3, 1, 'system'),
(0, '执法办案', 'ops-risk', 0, '/ops-risk', 'Layout', 'FileTextOutlined', 'ops:view', 3, 1, 'system'),
(6, '案件信息填报', 'ops:document', 1, '/ops-risk/document', 'views/ops-risk/document/index.vue', 'FileOutlined', 'ops:document:list', 1, 1, 'system'),
(0, '智能助手', 'ai-assistant', 0, '/ai-assistant', 'Layout', 'RobotOutlined', 'ai:view', 4, 1, 'system'),
(9, '知识库管理', 'ai:kb', 1, '/ai-assistant/kb', 'views/ai-assistant/kb/index.vue', 'DatabaseOutlined', 'ai:kb:list', 1, 1, 'system'),
(9, '智能问答', 'ai:chat', 1, '/ai-assistant/chat', 'views/ai-assistant/chat/index.vue', 'MessageOutlined', 'ai:chat:view', 2, 1, 'system'),
(0, '智能导航', 'smart-nav', 0, '/smart-nav', 'Layout', 'CompassOutlined', 'nav:view', 5, 1, 'system'),
(12, '导航入口', 'nav:entry', 1, '/smart-nav/entry', 'views/smart-nav/entry/index.vue', 'EnvironmentOutlined', 'nav:entry:use', 1, 1, 'system'),
(0, '刑侦研判', 'anti-fraud', 0, '/anti-fraud', 'Layout', 'SecurityScanOutlined', 'fraud:view', 6, 1, 'system'),
(14, '重点人员', 'fraud:customer', 1, '/anti-fraud/customer', 'views/anti-fraud/customer/index.vue', 'UserOutlined', 'fraud:customer:list', 1, 1, 'system'),
(14, '资金流水', 'fraud:transaction', 1, '/anti-fraud/transaction', 'views/anti-fraud/transaction/index.vue', 'TransactionOutlined', 'fraud:transaction:list', 2, 1, 'system'),
(14, '案件分析', 'fraud:analysis', 1, '/anti-fraud/analysis', 'views/anti-fraud/analysis/index.vue', 'AnalysisOutlined', 'fraud:analysis:view', 3, 1, 'system'),
(0, '治安管理', 'aml', 0, '/aml', 'Layout', 'EyeOutlined', 'aml:view', 7, 1, 'system'),
(18, '人员核查', 'aml:dd', 1, '/aml/due-diligence', 'views/aml/due-diligence/index.vue', 'AuditOutlined', 'aml:dd:list', 1, 1, 'system'),
(18, '线索管理', 'aml:suspicious', 1, '/aml/suspicious', 'views/aml/suspicious/index.vue', 'WarningOutlined', 'aml:suspicious:list', 2, 1, 'system');

-- INSERT 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6);

-- INSERT 法律文书类型
INSERT INTO ops_legal_doc_type (type_code, type_name, description, field_config, status, created_by) VALUES
('CIVIL_JUDGMENT', '民事判决书', '人民法院民事判决书', '{"fields":[{"name":"caseNumber","label":"案号"},{"name":"partyName","label":"当事人"},{"name":"amount","label":"涉案金额"},{"name":"court","label":"审理法院"},{"name":"judgmentDate","label":"判决日期"},{"name":"judgmentResult","label":"判决结果"}]}', 1, 'system'),
('CRIMINAL_JUDGMENT', '刑事判决书', '人民法院刑事判决书', '{"fields":[{"name":"caseNumber","label":"案号"},{"name":"defendant","label":"被告人"},{"name":"charge","label":"罪名"},{"name":"court","label":"审理法院"},{"name":"judgmentDate","label":"判决日期"},{"name":"sentence","label":"刑罚"}]}', 1, 'system'),
('ADMIN_PENALTY', '行政处罚决定书', '行政处罚决定书', '{"fields":[{"name":"docNumber","label":"文号"},{"name":"partyName","label":"当事人"},{"name":"violation","label":"违法行为"},{"name":"penalty","label":"处罚内容"},{"name":"authority","label":"处罚机关"},{"name":"penaltyDate","label":"处罚日期"}]}', 1, 'system');

-- INSERT 知识库数据
INSERT INTO ai_knowledge_base (kb_name, kb_code, kb_type, description, doc_count, status, created_by) VALUES
('法律法规知识库', 'KB_LAW', 'LAW', '刑法、治安管理处罚法等法律法规', 15, 1, 'admin'),
('案件案例知识库', 'KB_CASE', 'CASE', '典型案件案例、侦破方法', 10, 1, 'admin'),
('业务知识库', 'KB_BIZ', 'BIZ', '公安业务知识与操作规范', 20, 1, 'admin'),
('文书模板知识库', 'KB_DOC', 'DOC', '各类法律文书模板与规范', 25, 1, 'admin');

-- INSERT 导航意图数据
INSERT INTO nav_intent (intent_code, intent_name, keywords, target_path, target_name, description, priority, status, created_by) VALUES
('SUSPICIOUS_PERSON', '查看重点人员', '重点人员,嫌疑人,关注对象', '/anti-fraud/customer', '重点人员管理', '查看和管理重点人员列表', 1, 1, 'system'),
('CASE_ANALYSIS', '案件分析', '案件分析,研判,侦查分析', '/anti-fraud/analysis', '案件分析', '进行案件分析研判', 2, 1, 'system'),
('LEGAL_DOCUMENT', '法律文书', '法律文书,判决书,笔录', '/ops-risk/document', '案件信息填报', '上传和管理法律文书', 3, 1, 'system'),
('AI_CHAT', '智能问答', '问答,聊天,咨询,提问', '/ai-assistant/chat', '智能问答', '与AI助手进行对话', 4, 1, 'system'),
('PERSON_CHECK', '人员核查', '核查,背景调查,人员调查', '/aml/due-diligence', '人员核查', '人员背景核查管理', 5, 1, 'system'),
('CLUE_MANAGE', '线索管理', '线索,情报,举报', '/aml/suspicious', '线索管理', '线索情报管理', 6, 1, 'system'),
('USER_MANAGE', '用户管理', '用户,账号,人员管理', '/system/user', '用户管理', '管理系统用户', 7, 1, 'system'),
('KB_MANAGE', '知识库管理', '知识库,文档管理', '/ai-assistant/kb', '知识库管理', '管理业务知识库', 8, 1, 'system');

-- INSERT 重点人员数据
INSERT INTO fraud_suspicious_customer (customer_name, id_card, phone, gender, birth_date, occupation, address, account_no, open_account_date, account_status, risk_level, risk_score, suspicious_count, risk_features, mark_time, created_by) VALUES
('张三', '110101199001011234', '13900139001', 1, '1990-01-01', '无业', '北京市朝阳区建国路88号', '6222000000000001', '2022-03-15', 'NORMAL', 'HIGH', 85, 15, '多次涉案,关联可疑人员', '2024-01-20 10:00:00', 'system'),
('李四', '310101198805052345', '13900139002', 1, '1988-05-05', '个体经营', '上海市浦东新区陆家嘴', '6222000000000002', '2021-06-20', 'NORMAL', 'MEDIUM', 65, 8, '频繁更换住址,异常资金往来', '2024-01-19 14:30:00', 'system'),
('王五', '440101199203033456', '13900139003', 1, '1992-03-03', '自由职业', '广州市天河区珠江新城', '6222000000000003', '2023-01-10', 'FROZEN', 'HIGH', 92, 22, '多起案件关联,资金来源不明', '2024-01-18 09:00:00', 'system'),
('赵六', '330101199106064567', '13900139004', 1, '1991-06-06', '企业主', '杭州市西湖区文三路', '6222000000000004', '2020-08-25', 'NORMAL', 'LOW', 35, 3, '偶发可疑行为', '2024-01-17 16:00:00', 'system'),
('钱七', '510101198707075678', '13900139005', 2, '1987-07-07', '教师', '成都市武侯区天府大道', '6222000000000005', '2022-11-30', 'NORMAL', 'MEDIUM', 55, 6, '异常出行记录', '2024-01-16 11:00:00', 'system'),
('孙八', '420101199408086789', '13900139006', 1, '1994-08-08', '程序员', '武汉市洪山区光谷', '6222000000000006', '2023-05-18', 'NORMAL', 'HIGH', 78, 12, '网络犯罪关联,虚拟货币交易', '2024-01-15 08:30:00', 'system'),
('周九', '610101199009097890', '13900139007', 2, '1990-09-09', '医生', '西安市雁塔区高新路', '6222000000000007', '2021-09-05', 'NORMAL', 'MEDIUM', 48, 5, '关联可疑账户', '2024-01-14 15:00:00', 'system'),
('吴十', '210101198810108901', '13900139008', 1, '1988-10-10', '律师', '沈阳市和平区中山路', '6222000000000008', '2020-12-01', 'NORMAL', 'LOW', 28, 2, '正常活动为主', '2024-01-13 10:00:00', 'system');

-- INSERT 资金流水数据
INSERT INTO fraud_transaction (customer_id, transaction_no, transaction_time, transaction_type, amount, currency, balance, counter_account, counter_name, counter_bank, channel, location, risk_tag, risk_score, status) VALUES
(1, 'TXN20240120001', '2024-01-20 14:30:00', 'TRANSFER', 150000.00, 'CNY', 50000.00, '6222****8888', '某某公司', '工商银行', 'MOBILE', '北京', '大额快速转移', 85, 'SUSPICIOUS'),
(2, 'TXN20240120002', '2024-01-20 10:15:00', 'WITHDRAW', 50000.00, 'CNY', 30000.00, NULL, NULL, NULL, 'ATM', '上海', '异常取现', 70, 'SUSPICIOUS'),
(3, 'TXN20240120003', '2024-01-20 09:00:00', 'TRANSFER', 200000.00, 'CNY', 100000.00, '6217****6666', '某某贸易公司', '建设银行', 'ONLINE', '广州', '对公异常', 90, 'SUSPICIOUS'),
(1, 'TXN20240119001', '2024-01-19 16:00:00', 'TRANSFER', 80000.00, 'CNY', 200000.00, '6225****9999', '某某投资公司', '招商银行', 'COUNTER', '北京', '频繁交易', 75, 'SUSPICIOUS'),
(4, 'TXN20240119002', '2024-01-19 11:00:00', 'DEPOSIT', 30000.00, 'CNY', 80000.00, NULL, NULL, NULL, 'COUNTER', '杭州', '分拆存入', 60, 'NORMAL'),
(5, 'TXN20240119003', '2024-01-19 08:30:00', 'WITHDRAW', 25000.00, 'CNY', 45000.00, NULL, NULL, NULL, 'ATM', '成都', '异常取现', 65, 'SUSPICIOUS');

-- INSERT 人员核查数据
INSERT INTO aml_customer_due_diligence (customer_id, dd_type, dd_reason, customer_info, business_nature, fund_source, status, created_by) VALUES
(1, 'ENHANCED', '重点人员触发', '{"name":"张三","idCard":"110101199001011234","phone":"13900139001"}', '无业', '不明来源', 'IN_PROGRESS', 'admin'),
(2, 'SIMPLIFIED', '新关注对象', '{"name":"李四","idCard":"310101198805052345","phone":"13900139002"}', '个体经营', '经营收入', 'COMPLETED', 'admin'),
(3, 'ENHANCED', '案件关联触发', '{"name":"王五","idCard":"440101199203033456","phone":"13900139003"}', '自由职业', '不明', 'PENDING', 'admin'),
(6, 'ENHANCED', '网络犯罪关联', '{"name":"孙八","idCard":"420101199408086789","phone":"13900139006"}', 'IT行业', '工资收入', 'IN_PROGRESS', 'admin');

-- INSERT 线索数据
INSERT INTO aml_suspicious_transaction_report (report_no, customer_id, customer_name, id_card, transaction_count, total_amount, alert_type, status, created_by) VALUES
('CLUE20240001', 1, '张三', '110101199001011234', 15, 1500000.00, '重大线索', 'PENDING', 'system'),
('CLUE20240002', 3, '王五', '440101199203033456', 22, 2300000.00, '跨区域案件线索', 'CONFIRMED', 'system'),
('CLUE20240003', 2, '李四', '310101198805052345', 8, 800000.00, '群众举报线索', 'PENDING', 'system');
