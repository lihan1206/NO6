-- 就诊通知功能优化 - 数据库更新脚本
-- 执行日期: 2026-04-02

-- 1. 更新 jiuzhentongzhi 表，添加新字段
ALTER TABLE `jiuzhentongzhi` 
ADD COLUMN `tongzhileixing` VARCHAR(200) NULL COMMENT '通知类型' AFTER `tongzhibeizhu`,
ADD COLUMN `sendstatus` INT(11) NULL DEFAULT 0 COMMENT '发送状态：0-待发送，1-发送成功，2-发送失败' AFTER `tongzhileixing`,
ADD COLUMN `retrycount` INT(11) NULL DEFAULT 0 COMMENT '重试次数' AFTER `sendstatus`,
ADD COLUMN `lasttrytime` DATETIME NULL COMMENT '最后尝试发送时间' AFTER `retrycount`;

-- 2. 创建 tongzhisongjiliao 表（通知发送记录表）
DROP TABLE IF EXISTS `tongzhisongjiliao`;
CREATE TABLE `tongzhisongjiliao` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tongzhibianhao` VARCHAR(200) NULL COMMENT '通知编号',
  `tongzhileixing` VARCHAR(200) NULL COMMENT '通知类型',
  `zhanghao` VARCHAR(200) NULL COMMENT '账号',
  `shouji` VARCHAR(200) NULL COMMENT '手机',
  `songzhuangtai` INT(11) NULL COMMENT '发送状态：0-失败，1-成功',
  `cuowuxinxi` LONGTEXT NULL COMMENT '错误信息',
  `songshijian` DATETIME(0) NULL COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知发送记录' ROW_FORMAT = Dynamic;

-- 完成提示
SELECT '数据库表结构更新完成！' AS message;
