package com.cl.service.impl;

import com.cl.entity.TongzhijiluEntity;
import com.cl.entity.YishengyuyueEntity;
import com.cl.entity.YonghuEntity;
import com.cl.service.TongzhijiluService;
import com.cl.service.TongzhiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 通知服务实现类
 * @author 
 * @email 
 * @date 2025-03-27 15:44:15
 */
@Service("tongzhiService")
public class TongzhiServiceImpl implements TongzhiService {

    @Autowired
    private TongzhijiluService tongzhijiluService;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public UserChannelStatus getUserChannelStatus(YonghuEntity yonghu) {
        UserChannelStatus status = new UserChannelStatus();

        if (yonghu == null) {
            return status;
        }

        // 检查手机号是否有效（短信渠道）
        String phone = yonghu.getShouji();
        if (phone != null && !phone.trim().isEmpty() && phone.matches("^1[3-9]\\d{9}$")) {
            status.setSmsEnabled(true);
            status.setPhoneNumber(phone);
        }

        // 应用内通知默认可用（用户已登录系统）
        status.setAppEnabled(true);

        // 检查邮箱是否有效（邮件渠道）- 当前用户实体没有邮箱字段，可根据需要扩展
        status.setEmailEnabled(false);

        return status;
    }

    @Override
    @Transactional
    public List<SendResult> sendAllNotificationsOnBooking(YishengyuyueEntity yuyue, YonghuEntity yonghu) {
        List<SendResult> results = new ArrayList<>();

        if (yuyue == null || yonghu == null) {
            SendResult result = new SendResult();
            result.setSuccess(false);
            result.setErrorMsg("预约信息或用户信息为空");
            results.add(result);
            return results;
        }

        // 获取用户渠道状态
        UserChannelStatus channelStatus = getUserChannelStatus(yonghu);

        if (!channelStatus.hasAvailableChannel()) {
            SendResult result = new SendResult();
            result.setSuccess(false);
            result.setErrorMsg("用户没有可用的通知渠道");
            results.add(result);
            return results;
        }

        String bestChannel = channelStatus.getBestChannel();
        Date yuyueTime = yuyue.getYuyueshijian();
        Date now = new Date();

        // 1. 预约成功通知 - 立即发送
        String content1 = buildNotificationContent(1, yuyue, yonghu, yuyueTime);
        TongzhijiluEntity record1 = createNotificationRecord(
                yuyue.getYuyuebianhao(),
                yonghu.getZhanghao(),
                yonghu.getShouji(),
                1,
                content1,
                bestChannel,
                now
        );
        SendResult result1 = sendNotification(record1);
        results.add(result1);

        // 2. 就诊前24小时提醒 - 立即创建记录，如果预约时间在24小时内则立即发送
        Date remind24h = calculateReminderTime(yuyueTime, -24);
        String content2 = buildNotificationContent(2, yuyue, yonghu, yuyueTime);
        TongzhijiluEntity record2 = createNotificationRecord(
                yuyue.getYuyuebianhao(),
                yonghu.getZhanghao(),
                yonghu.getShouji(),
                2,
                content2,
                bestChannel,
                remind24h.after(now) ? remind24h : now
        );
        // 如果预约时间在24小时内，立即发送
        if (remind24h.before(now) || remind24h.equals(now)) {
            SendResult result2 = sendNotification(record2);
            results.add(result2);
        }

        // 3. 就诊前1小时提醒 - 立即创建记录，如果预约时间在1小时内则立即发送
        Date remind1h = calculateReminderTime(yuyueTime, -1);
        String content3 = buildNotificationContent(3, yuyue, yonghu, yuyueTime);
        TongzhijiluEntity record3 = createNotificationRecord(
                yuyue.getYuyuebianhao(),
                yonghu.getZhanghao(),
                yonghu.getShouji(),
                3,
                content3,
                bestChannel,
                remind1h.after(now) ? remind1h : now
        );
        // 如果预约时间在1小时内，立即发送
        if (remind1h.before(now) || remind1h.equals(now)) {
            SendResult result3 = sendNotification(record3);
            results.add(result3);
        }

        // 4. 就诊当天提醒 - 立即创建记录，如果就诊时间就是今天则立即发送
        Date remindSameDay = calculateSameDayReminder(yuyueTime);
        String content4 = buildNotificationContent(4, yuyue, yonghu, yuyueTime);
        TongzhijiluEntity record4 = createNotificationRecord(
                yuyue.getYuyuebianhao(),
                yonghu.getZhanghao(),
                yonghu.getShouji(),
                4,
                content4,
                bestChannel,
                remindSameDay.after(now) ? remindSameDay : now
        );
        // 如果就诊时间就是今天，立即发送
        if (isSameDay(remindSameDay, now) || remindSameDay.before(now)) {
            SendResult result4 = sendNotification(record4);
            results.add(result4);
        }

        return results;
    }

    @Override
    @Transactional
    public SendResult sendNotification(TongzhijiluEntity tongzhijilu) {
        SendResult result = new SendResult();

        if (tongzhijilu == null) {
            result.setSuccess(false);
            result.setErrorMsg("通知记录为空");
            return result;
        }

        String channel = tongzhijilu.getFasongqudao();
        result.setChannel(channel);

        try {
            boolean success = false;

            switch (channel) {
                case "sms":
                    success = sendSmsNotification(tongzhijilu);
                    break;
                case "app":
                    success = sendAppNotification(tongzhijilu);
                    break;
                case "email":
                    success = sendEmailNotification(tongzhijilu);
                    break;
                default:
                    result.setErrorMsg("未知的发送渠道: " + channel);
                    result.setSuccess(false);
                    updateNotificationStatus(tongzhijilu.getId(), 2, result.getErrorMsg());
                    return result;
            }

            if (success) {
                result.setSuccess(true);
                result.setMessage("通知发送成功");
                updateNotificationStatus(tongzhijilu.getId(), 1, null);
            } else {
                result.setSuccess(false);
                result.setErrorMsg("通知发送失败");
                // 增加重试次数
                tongzhijilu.setChongshicishu(tongzhijilu.getChongshicishu() + 1);
                tongzhijiluService.updateById(tongzhijilu);
                updateNotificationStatus(tongzhijilu.getId(), 2, result.getErrorMsg());
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMsg("发送异常: " + e.getMessage());
            // 增加重试次数
            tongzhijilu.setChongshicishu(tongzhijilu.getChongshicishu() + 1);
            tongzhijiluService.updateById(tongzhijilu);
            updateNotificationStatus(tongzhijilu.getId(), 2, result.getErrorMsg());
        }

        return result;
    }

    @Override
    @Transactional
    public boolean retrySendNotification(TongzhijiluEntity tongzhijilu) {
        if (tongzhijilu == null) {
            return false;
        }

        // 检查是否超过最大重试次数
        if (tongzhijilu.getChongshicishu() >= tongzhijilu.getZuidachongshicishu()) {
            return false;
        }

        // 重置状态为待发送
        tongzhijilu.setFasongzhuangtai(0);
        tongzhijilu.setShibaiyuanyin(null);
        tongzhijiluService.updateById(tongzhijilu);

        // 重新发送
        SendResult result = sendNotification(tongzhijilu);

        return result.isSuccess();
    }

    @Override
    public Map<String, Object> getNotificationStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 查询所有通知记录
        List<TongzhijiluEntity> allRecords = tongzhijiluService.selectList(
                new com.baomidou.mybatisplus.mapper.EntityWrapper<TongzhijiluEntity>()
        );

        int totalCount = allRecords.size();
        int successCount = 0;
        int failCount = 0;
        int pendingCount = 0;

        for (TongzhijiluEntity record : allRecords) {
            switch (record.getFasongzhuangtai()) {
                case 0:
                    pendingCount++;
                    break;
                case 1:
                    successCount++;
                    break;
                case 2:
                    failCount++;
                    break;
            }
        }

        statistics.put("totalCount", totalCount);
        statistics.put("successCount", successCount);
        statistics.put("failCount", failCount);
        statistics.put("pendingCount", pendingCount);

        // 计算成功率
        if (totalCount > 0) {
            double successRate = (double) successCount / totalCount * 100;
            statistics.put("successRate", String.format("%.2f%%", successRate));
        } else {
            statistics.put("successRate", "0.00%");
        }

        return statistics;
    }

    @Override
    @Transactional
    public TongzhijiluEntity createNotificationRecord(String yuyuebianhao, String zhanghao, String shouji,
                                                      Integer tongzhileixing, String tongzhineirong,
                                                      String fasongqudao, Date jihuafasongshijian) {
        TongzhijiluEntity record = new TongzhijiluEntity();
        record.setYuyuebianhao(yuyuebianhao);
        record.setZhanghao(zhanghao);
        record.setShouji(shouji);
        record.setTongzhileixing(tongzhileixing);
        record.setTongzhineirong(tongzhineirong);
        record.setFasongqudao(fasongqudao);
        record.setFasongzhuangtai(0); // 待发送
        record.setChongshicishu(0);
        record.setZuidachongshicishu(MAX_RETRY_COUNT);
        record.setJihuafasongshijian(jihuafasongshijian);
        record.setAddtime(new Date());

        tongzhijiluService.insert(record);
        return record;
    }

    @Override
    @Transactional
    public void updateNotificationStatus(Long id, Integer status, String errorMsg) {
        TongzhijiluEntity record = tongzhijiluService.selectById(id);
        if (record != null) {
            record.setFasongzhuangtai(status);
            record.setShijifasongshijian(new Date());
            if (errorMsg != null) {
                record.setShibaiyuanyin(errorMsg);
            }
            tongzhijiluService.updateById(record);
        }
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(Integer type, YishengyuyueEntity yuyue,
                                           YonghuEntity yonghu, Date yuyueTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = yuyueTime != null ? sdf.format(yuyueTime) : "未知时间";

        switch (type) {
            case 1:
                return String.format("【预约成功】尊敬的%s，您已成功预约医生%s，预约时间：%s，请按时就诊。",
                        yonghu.getXingming(), yuyue.getYishengzhanghao(), timeStr);
            case 2:
                return String.format("【就诊提醒】尊敬的%s，您预约的就诊将在24小时后（%s）开始，请提前做好准备。",
                        yonghu.getXingming(), timeStr);
            case 3:
                return String.format("【就诊提醒】尊敬的%s，您预约的就诊将在1小时后（%s）开始，请尽快前往医院。",
                        yonghu.getXingming(), timeStr);
            case 4:
                return String.format("【就诊提醒】尊敬的%s，您预约的就诊今天（%s）开始，请准时到达。",
                        yonghu.getXingming(), timeStr);
            default:
                return "就诊通知";
        }
    }

    /**
     * 计算提醒时间
     */
    private Date calculateReminderTime(Date baseTime, int hoursBefore) {
        if (baseTime == null) {
            return new Date();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseTime);
        cal.add(Calendar.HOUR_OF_DAY, hoursBefore);
        return cal.getTime();
    }

    /**
     * 计算当天提醒时间（就诊当天上午8点）
     */
    private Date calculateSameDayReminder(Date baseTime) {
        if (baseTime == null) {
            return new Date();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseTime);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    /**
     * 判断两个日期是否为同一天
     */
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 发送短信通知
     * 实际项目中需要接入短信服务商API
     */
    private boolean sendSmsNotification(TongzhijiluEntity tongzhijilu) {
        // 模拟短信发送
        // 实际项目中调用短信服务商API
        System.out.println("发送短信通知到: " + tongzhijilu.getShouji());
        System.out.println("内容: " + tongzhijilu.getTongzhineirong());

        // 模拟90%成功率
        return Math.random() > 0.1;
    }

    /**
     * 发送应用内通知
     */
    private boolean sendAppNotification(TongzhijiluEntity tongzhijilu) {
        // 模拟应用内通知
        System.out.println("发送应用内通知到用户: " + tongzhijilu.getZhanghao());
        System.out.println("内容: " + tongzhijilu.getTongzhineirong());

        // 应用内通知通常成功率较高
        return true;
    }

    /**
     * 发送邮件通知
     * 实际项目中需要接入邮件服务
     */
    private boolean sendEmailNotification(TongzhijiluEntity tongzhijilu) {
        // 模拟邮件发送
        System.out.println("发送邮件通知");
        System.out.println("内容: " + tongzhijilu.getTongzhineirong());

        // 模拟80%成功率
        return Math.random() > 0.2;
    }
}
