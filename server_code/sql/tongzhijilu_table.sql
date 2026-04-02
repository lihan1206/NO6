-- 通知记录表
-- 用于存储所有通知发送记录，包括发送状态、重试次数等信息

DROP TABLE IF EXISTS `tongzhijilu`;
CREATE TABLE `tongzhijilu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `yuyuebianhao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预约编号',
  `zhanghao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户账号',
  `shouji` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户手机',
  `tongzhileixing` int(11) DEFAULT NULL COMMENT '通知类型（1-预约成功通知，2-就诊前24小时提醒，3-就诊前1小时提醒，4-就诊当天提醒）',
  `tongzhineirong` longtext COLLATE utf8mb4_unicode_ci COMMENT '通知内容',
  `fasongqudao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送渠道（sms-短信，app-应用内，email-邮件）',
  `fasongzhuangtai` int(11) DEFAULT '0' COMMENT '发送状态（0-待发送，1-发送成功，2-发送失败）',
  `shibaiyuanyin` longtext COLLATE utf8mb4_unicode_ci COMMENT '失败原因',
  `chongshicishu` int(11) DEFAULT '0' COMMENT '重试次数',
  `zuidachongshicishu` int(11) DEFAULT '3' COMMENT '最大重试次数',
  `jihuafasongshijian` datetime DEFAULT NULL COMMENT '计划发送时间',
  `shijifasongshijian` datetime DEFAULT NULL COMMENT '实际发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_yuyuebianhao` (`yuyuebianhao`),
  KEY `idx_zhanghao` (`zhanghao`),
  KEY `idx_fasongzhuangtai` (`fasongzhuangtai`),
  KEY `idx_jihuafasongshijian` (`jihuafasongshijian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';
