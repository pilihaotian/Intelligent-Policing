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

-- INSERT 重点人员数据（明确指定ID确保关联正确）
INSERT INTO fraud_suspicious_customer (id, customer_no, customer_name, id_type, id_no, gender, birth_date, phone, email, address, customer_type, risk_level, risk_score, blacklist_flag, watchlist_flag, suspicious_type, first_suspicious_time, last_suspicious_time, suspicious_count, status, created_by) VALUES
(1, 'CUS20240001', '张三', 'ID_CARD', '110101199001011234', 1, '1990-01-01', '13900139001', 'zhangsan@email.com', '北京市朝阳区建国路88号', 'HIGH_RISK', 'HIGH', 85.00, 0, 1, 'TELECOM_FRAUD', '2023-06-15 10:00:00', '2024-01-20 10:00:00', 15, 1, 'system'),
(2, 'CUS20240002', '李四', 'ID_CARD', '310101198805052345', 1, '1988-05-05', '13900139002', 'lisi@email.com', '上海市浦东新区陆家嘴', 'MEDIUM_RISK', 'MEDIUM', 65.00, 0, 1, 'MONEY_LAUNDERING', '2023-08-20 14:30:00', '2024-01-19 14:30:00', 8, 1, 'system'),
(3, 'CUS20240003', '王五', 'ID_CARD', '440101199203033456', 1, '1992-03-03', '13900139003', 'wangwu@email.com', '广州市天河区珠江新城', 'HIGH_RISK', 'HIGH', 92.00, 1, 1, 'GAMBLING', '2023-03-10 09:00:00', '2024-01-18 09:00:00', 22, 1, 'system'),
(4, 'CUS20240004', '赵六', 'ID_CARD', '330101199106064567', 1, '1991-06-06', '13900139004', 'zhaoliu@email.com', '杭州市西湖区文三路', 'LOW_RISK', 'LOW', 35.00, 0, 0, 'SUSPICIOUS_TRANSFER', '2023-11-01 16:00:00', '2024-01-17 16:00:00', 3, 1, 'system'),
(5, 'CUS20240005', '钱七', 'ID_CARD', '510101198707075678', 2, '1987-07-07', '13900139005', 'qianqi@email.com', '成都市武侯区天府大道', 'MEDIUM_RISK', 'MEDIUM', 55.00, 0, 1, 'ILLEGAL_FUNDRAISING', '2023-09-15 11:00:00', '2024-01-16 11:00:00', 6, 1, 'system'),
(6, 'CUS20240006', '孙八', 'ID_CARD', '420101199408086789', 1, '1994-08-08', '13900139006', 'sunba@email.com', '武汉市洪山区光谷', 'HIGH_RISK', 'HIGH', 78.00, 1, 1, 'CYBER_CRIME', '2023-05-20 08:30:00', '2024-01-15 08:30:00', 12, 1, 'system'),
(7, 'CUS20240007', '周九', 'ID_CARD', '610101199009097890', 2, '1990-09-09', '13900139007', 'zhoujiu@email.com', '西安市雁塔区高新路', 'MEDIUM_RISK', 'MEDIUM', 48.00, 0, 1, 'ASSOCIATION_SUSPICIOUS', '2023-10-05 15:00:00', '2024-01-14 15:00:00', 5, 1, 'system'),
(8, 'CUS20240008', '吴十', 'ID_CARD', '210101198810108901', 1, '1988-10-10', '13900139008', 'wushi@email.com', '沈阳市和平区中山路', 'LOW_RISK', 'LOW', 28.00, 0, 0, 'ABNORMAL_BEHAVIOR', '2023-12-01 10:00:00', '2024-01-13 10:00:00', 2, 1, 'system'),
(9, 'CUS20240009', '郑明', 'ID_CARD', '350101199512125678', 1, '1995-12-12', '13900139009', 'zhengming@email.com', '福州市鼓楼区五一路', 'HIGH_RISK', 'HIGH', 88.00, 1, 1, 'DRUG_TRAFFICKING', '2023-04-01 09:00:00', '2024-01-12 18:00:00', 18, 1, 'system'),
(10, 'CUS20240010', '陈芳', 'ID_CARD', '370101198903036789', 2, '1989-03-03', '13900139010', 'chenfang@email.com', '济南市历下区泉城路', 'MEDIUM_RISK', 'MEDIUM', 62.00, 0, 1, 'FRAUD_SUSPECTED', '2023-07-20 14:00:00', '2024-01-11 11:00:00', 7, 1, 'system');
-- 重置序列
SELECT setval('fraud_suspicious_customer_id_seq', 10);

-- INSERT 资金流水数据
INSERT INTO fraud_transaction (transaction_no, customer_id, account_no, counter_account_no, counter_account_name, transaction_type, amount, currency, balance_before, balance_after, transaction_time, channel, device_id, ip_address, location, remark, risk_flag, risk_score, created_time) VALUES
-- 张三(1)的交易记录
('TXN20240120001', 1, '6222000000000001', '6222888800000001', '某某科技公司', 'TRANSFER_OUT', 150000.00, 'CNY', 200000.00, 50000.00, '2024-01-20 14:30:00', 'MOBILE_BANKING', 'DEVICE_001', '192.168.1.100', '北京朝阳', '大额转账', 1, 85.00, '2024-01-20 14:30:00'),
('TXN20240120002', 1, '6222000000000001', '6222888800000002', '某某投资公司', 'TRANSFER_OUT', 80000.00, 'CNY', 130000.00, 50000.00, '2024-01-20 10:15:00', 'ONLINE_BANKING', 'DEVICE_001', '192.168.1.100', '北京朝阳', '投资转账', 1, 75.00, '2024-01-20 10:15:00'),
('TXN20240119001', 1, '6222000000000001', NULL, NULL, 'WITHDRAW', 50000.00, 'CNY', 250000.00, 200000.00, '2024-01-19 16:00:00', 'ATM', NULL, NULL, '北京朝阳', '柜台取现', 1, 70.00, '2024-01-19 16:00:00'),
('TXN20240119002', 1, '6222000000000001', NULL, NULL, 'DEPOSIT', 300000.00, 'CNY', 0.00, 300000.00, '2024-01-19 09:00:00', 'COUNTER', NULL, NULL, '北京朝阳', '现金存入', 1, 80.00, '2024-01-19 09:00:00'),
('TXN20240118001', 1, '6222000000000001', '6222888800000003', '某某商贸公司', 'TRANSFER_OUT', 120000.00, 'CNY', 420000.00, 300000.00, '2024-01-18 11:30:00', 'MOBILE_BANKING', 'DEVICE_002', '192.168.1.101', '北京海淀', '货款支付', 1, 65.00, '2024-01-18 11:30:00'),
-- 李四(2)的交易记录
('TXN20240120003', 2, '6222000000000002', '6222777700000001', '某某贸易公司', 'TRANSFER_OUT', 50000.00, 'CNY', 100000.00, 50000.00, '2024-01-20 09:00:00', 'ONLINE_BANKING', 'DEVICE_003', '192.168.2.100', '上海浦东', '贸易款', 1, 60.00, '2024-01-20 09:00:00'),
('TXN20240119003', 2, '6222000000000002', NULL, NULL, 'WITHDRAW', 30000.00, 'CNY', 80000.00, 50000.00, '2024-01-19 14:00:00', 'ATM', NULL, NULL, '上海浦东', 'ATM取现', 0, 45.00, '2024-01-19 14:00:00'),
('TXN20240118002', 2, '6222000000000002', NULL, NULL, 'DEPOSIT', 80000.00, 'CNY', 50000.00, 130000.00, '2024-01-18 10:00:00', 'COUNTER', NULL, NULL, '上海浦东', '经营收入', 0, 30.00, '2024-01-18 10:00:00'),
-- 王五(3)的交易记录
('TXN20240120004', 3, '6222000000000003', '6222666600000001', '某某网络公司', 'TRANSFER_OUT', 200000.00, 'CNY', 350000.00, 150000.00, '2024-01-20 11:00:00', 'MOBILE_BANKING', 'DEVICE_004', '192.168.3.100', '广州天河', '网络服务费', 1, 90.00, '2024-01-20 11:00:00'),
('TXN20240120005', 3, '6222000000000003', '6222666600000002', '某某游戏公司', 'TRANSFER_OUT', 150000.00, 'CNY', 300000.00, 150000.00, '2024-01-20 08:30:00', 'ONLINE_BANKING', 'DEVICE_004', '192.168.3.100', '广州天河', '游戏充值', 1, 95.00, '2024-01-20 08:30:00'),
('TXN20240119004', 3, '6222000000000003', '6222666600000003', '某某支付公司', 'TRANSFER_OUT', 180000.00, 'CNY', 530000.00, 350000.00, '2024-01-19 15:00:00', 'MOBILE_BANKING', 'DEVICE_005', '192.168.3.101', '广州天河', '第三方支付', 1, 88.00, '2024-01-19 15:00:00'),
('TXN20240119005', 3, '6222000000000003', NULL, NULL, 'WITHDRAW', 100000.00, 'CNY', 630000.00, 530000.00, '2024-01-19 09:00:00', 'ATM', NULL, NULL, '广州天河', '大额取现', 1, 85.00, '2024-01-19 09:00:00'),
-- 赵六(4)的交易记录
('TXN20240120011', 4, '6222000000000004', '6222111100000001', '某某投资咨询', 'TRANSFER_OUT', 35000.00, 'CNY', 80000.00, 45000.00, '2024-01-20 13:00:00', 'ONLINE_BANKING', 'DEVICE_009', '192.168.4.100', '杭州西湖', '投资款', 0, 40.00, '2024-01-20 13:00:00'),
('TXN20240119009', 4, '6222000000000004', NULL, NULL, 'DEPOSIT', 50000.00, 'CNY', 30000.00, 80000.00, '2024-01-19 10:00:00', 'COUNTER', NULL, NULL, '杭州西湖', '经营收入', 0, 25.00, '2024-01-19 10:00:00'),
-- 钱七(5)的交易记录
('TXN20240120012', 5, '6222000000000005', '6222222200000001', '某某教育机构', 'TRANSFER_OUT', 20000.00, 'CNY', 50000.00, 30000.00, '2024-01-20 11:00:00', 'MOBILE_BANKING', 'DEVICE_010', '192.168.5.100', '成都武侯', '培训费用', 0, 30.00, '2024-01-20 11:00:00'),
('TXN20240119010', 5, '6222000000000005', NULL, NULL, 'WITHDRAW', 15000.00, 'CNY', 65000.00, 50000.00, '2024-01-19 15:00:00', 'ATM', NULL, NULL, '成都武侯', '日常取现', 0, 20.00, '2024-01-19 15:00:00'),
-- 孙八(6)的交易记录
('TXN20240120006', 6, '6222000000000006', '6222555500000001', '某某数据公司', 'TRANSFER_OUT', 80000.00, 'CNY', 150000.00, 70000.00, '2024-01-20 16:00:00', 'ONLINE_BANKING', 'DEVICE_006', '192.168.6.100', '武汉洪山', '数据服务', 1, 78.00, '2024-01-20 16:00:00'),
('TXN20240120007', 6, '6222000000000006', '6222555500000002', '某某云服务公司', 'TRANSFER_OUT', 60000.00, 'CNY', 130000.00, 70000.00, '2024-01-20 10:00:00', 'MOBILE_BANKING', 'DEVICE_006', '192.168.6.100', '武汉洪山', '云服务费', 1, 72.00, '2024-01-20 10:00:00'),
('TXN20240119006', 6, '6222000000000006', NULL, NULL, 'DEPOSIT', 200000.00, 'CNY', 0.00, 200000.00, '2024-01-19 17:00:00', 'COUNTER', NULL, NULL, '武汉洪山', '项目收入', 1, 65.00, '2024-01-19 17:00:00'),
-- 周九(7)的交易记录
('TXN20240120013', 7, '6222000000000007', '6222333300000002', '某某医疗器械', 'TRANSFER_OUT', 40000.00, 'CNY', 80000.00, 40000.00, '2024-01-20 09:30:00', 'ONLINE_BANKING', 'DEVICE_011', '192.168.7.100', '西安雁塔', '设备采购', 0, 35.00, '2024-01-20 09:30:00'),
('TXN20240119011', 7, '6222000000000007', NULL, NULL, 'DEPOSIT', 60000.00, 'CNY', 20000.00, 80000.00, '2024-01-19 08:00:00', 'COUNTER', NULL, NULL, '西安雁塔', '工资收入', 0, 15.00, '2024-01-19 08:00:00'),
-- 吴十(8)的交易记录
('TXN20240120014', 8, '6222000000000008', '6222444400000003', '某某律所账户', 'TRANSFER_OUT', 25000.00, 'CNY', 60000.00, 35000.00, '2024-01-20 10:00:00', 'ONLINE_BANKING', 'DEVICE_012', '192.168.8.100', '沈阳和平', '法律服务费', 0, 20.00, '2024-01-20 10:00:00'),
('TXN20240119012', 8, '6222000000000008', NULL, NULL, 'WITHDRAW', 10000.00, 'CNY', 70000.00, 60000.00, '2024-01-19 16:00:00', 'ATM', NULL, NULL, '沈阳和平', '日常取现', 0, 15.00, '2024-01-19 16:00:00'),
-- 郑明(9)的交易记录
('TXN20240120008', 9, '6222000000000009', '6222444400000001', '某某物流公司', 'TRANSFER_OUT', 250000.00, 'CNY', 400000.00, 150000.00, '2024-01-20 12:00:00', 'ONLINE_BANKING', 'DEVICE_007', '192.168.9.100', '福州鼓楼', '物流费用', 1, 92.00, '2024-01-20 12:00:00'),
('TXN20240120009', 9, '6222000000000009', '6222444400000002', '某某贸易公司', 'TRANSFER_OUT', 180000.00, 'CNY', 330000.00, 150000.00, '2024-01-20 08:00:00', 'MOBILE_BANKING', 'DEVICE_007', '192.168.9.100', '福州鼓楼', '贸易结算', 1, 88.00, '2024-01-20 08:00:00'),
('TXN20240119007', 9, '6222000000000009', NULL, NULL, 'WITHDRAW', 150000.00, 'CNY', 550000.00, 400000.00, '2024-01-19 14:00:00', 'ATM', NULL, NULL, '福州鼓楼', '大额取现', 1, 90.00, '2024-01-19 14:00:00'),
-- 陈芳(10)的交易记录
('TXN20240120010', 10, '6222000000000010', '6222333300000001', '某某咨询公司', 'TRANSFER_OUT', 45000.00, 'CNY', 90000.00, 45000.00, '2024-01-20 15:00:00', 'MOBILE_BANKING', 'DEVICE_008', '192.168.10.100', '济南历下', '咨询服务费', 0, 55.00, '2024-01-20 15:00:00'),
('TXN20240119008', 10, '6222000000000010', NULL, NULL, 'WITHDRAW', 25000.00, 'CNY', 75000.00, 50000.00, '2024-01-19 11:00:00', 'ATM', NULL, NULL, '济南历下', '日常取现', 0, 35.00, '2024-01-19 11:00:00'),
('TXN20240118003', 10, '6222000000000010', NULL, NULL, 'DEPOSIT', 50000.00, 'CNY', 40000.00, 90000.00, '2024-01-18 09:00:00', 'COUNTER', NULL, NULL, '济南历下', '工资收入', 0, 20.00, '2024-01-18 09:00:00');

-- INSERT 人员核查数据
INSERT INTO aml_customer_due_diligence (customer_id, dd_type, dd_reason, customer_info, business_nature, fund_source, status, ai_analysis, ai_suggestions, created_by) VALUES
(1, 'ENHANCED', '重点人员触发', '{"name":"张三","idCard":"110101199001011234","phone":"13900139001"}', '无业', '不明来源', 'IN_PROGRESS', 
'```json
{
    "risk_assessment": {
        "level": "高",
        "factors": ["无固定职业但有大额资金往来", "资金来源不明确", "多次触发风险预警"]
    },
    "suspicious_points": [
        {"point": "短期内频繁大额转账，与职业状况不符", "severity": "高"},
        {"point": "资金来源无法合理解释", "severity": "高"},
        {"point": "与多个可疑账户有交易往来", "severity": "中"}
    ],
    "suggestions": ["深入调查资金来源", "追踪交易对手方信息", "调取银行流水详细记录", "核实实际居住地与活动轨迹"],
    "confidence": 85
}
```',
'["深入调查资金来源","追踪交易对手方信息","调取银行流水详细记录","核实实际居住地与活动轨迹"]', 
'admin'),
(2, 'SIMPLIFIED', '新关注对象', '{"name":"李四","idCard":"310101198805052345","phone":"13900139002"}', '个体经营', '经营收入', 'COMPLETED', NULL, NULL, 'admin'),
(3, 'ENHANCED', '案件关联触发', '{"name":"王五","idCard":"440101199203033456","phone":"13900139003"}', '自由职业', '不明', 'PENDING', NULL, NULL, 'admin'),
(6, 'ENHANCED', '网络犯罪关联', '{"name":"孙八","idCard":"420101199408086789","phone":"13900139006"}', 'IT行业', '工资收入', 'IN_PROGRESS', NULL, NULL, 'admin'),
(9, 'ENHANCED', '毒品犯罪关联', '{"name":"郑明","idCard":"350101199512125678","phone":"13900139009"}', '无业', '不明', 'PENDING',
'```json
{
    "risk_assessment": {
        "level": "极高",
        "factors": ["涉嫌毒品犯罪", "大额资金频繁流转", "多地区活动轨迹"]
    },
    "suspicious_points": [
        {"point": "与已知毒品犯罪人员有资金往来", "severity": "高"},
        {"point": "物流费用异常高额，疑似用于运输违禁品", "severity": "高"},
        {"point": "多地区频繁取现，规避银行监控", "severity": "高"},
        {"point": "无正当职业却有大量资金流入", "severity": "中"}
    ],
    "suggestions": ["立即启动专案调查", "协调多地公安机关联合侦查", "重点监控物流渠道", "调查关联人员网络"],
    "confidence": 92
}
```',
'["立即启动专案调查","协调多地公安机关联合侦查","重点监控物流渠道","调查关联人员网络"]',
'system');

-- INSERT 线索数据
INSERT INTO aml_suspicious_transaction_report (report_no, customer_id, customer_name, id_card, transaction_count, total_amount, alert_type, suspicious_types, status, analysis_result, created_by) VALUES
('CLUE20240001', 1, '张三', '110101199001011234', 15, 1500000.00, '重大线索', '电信诈骗,洗钱', 'PENDING', NULL, 'system'),
('CLUE20240002', 3, '王五', '440101199203033456', 22, 2300000.00, '跨区域案件线索', '网络赌博,洗钱', 'CONFIRMED', 
'```json
{
    "clue_assessment": {
        "credibility": "高",
        "urgency": "紧急",
        "risk_level": "高"
    },
    "crime_pattern": {
        "type": "网络赌博",
        "method": "通过第三方支付平台进行资金流转，利用游戏充值、网络服务费等名义掩盖赌博资金"
    },
    "key_evidence": [
        {"evidence": "大额资金频繁转入游戏公司账户", "importance": "高"},
        {"evidence": "交易时间集中在深夜时段", "importance": "中"},
        {"evidence": "多个关联账户存在相似交易模式", "importance": "高"}
    ],
    "suspicious_points": [
        {"point": "交易金额与正常游戏消费严重不符", "severity": "高"},
        {"point": "资金快进快出，符合洗钱特征", "severity": "高"},
        {"point": "与多个已关闭的赌博网站存在关联", "severity": "高"}
    ],
    "investigation_suggestions": ["追踪第三方支付平台资金流向", "调查游戏公司经营资质", "排查关联人员背景", "协调网监部门协助侦查"],
    "confidence": 88
}
```',
'system'),
('CLUE20240003', 2, '李四', '310101198805052345', 8, 800000.00, '群众举报线索', '可疑转账', 'PENDING', NULL, 'system'),
('CLUE20240004', 9, '郑明', '350101199512125678', 18, 580000.00, '重大线索', '毒品犯罪', 'PENDING',
'```json
{
    "clue_assessment": {
        "credibility": "高",
        "urgency": "紧急",
        "risk_level": "高"
    },
    "crime_pattern": {
        "type": "毒品犯罪",
        "method": "利用物流渠道进行毒品运输，通过大额取现规避银行监控，资金流转复杂难以追踪"
    },
    "key_evidence": [
        {"evidence": "高额物流费用支出记录", "importance": "高"},
        {"evidence": "多地ATM大额取现行为", "importance": "高"},
        {"evidence": "与已知毒品犯罪人员有资金往来", "importance": "高"}
    ],
    "suspicious_points": [
        {"point": "物流费用与业务规模严重不匹配", "severity": "高"},
        {"point": "频繁跨地区取现，规避银行监控", "severity": "高"},
        {"point": "无正当收入来源却有大额资金流转", "severity": "高"},
        {"point": "交易对手方多为可疑账户", "severity": "中"}
    ],
    "investigation_suggestions": ["立即启动专案侦查程序", "协调禁毒部门联合行动", "重点监控物流渠道", "追踪资金链上下游"],
    "confidence": 90
}
```',
'system');
