package com.cl.config;

import com.cl.service.NotificationSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知定时任务类
 * 用于定期重试发送失败的通知
 */
@Component
public class NotificationScheduledTask {

    @Autowired
    private NotificationSendService notificationSendService;

    /**
     * 每隔30分钟自动重试一次失败的通知
     * cron表达式：0 0/30 * * * ? 表示每小时的0分和30分执行
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void retryFailedNotifications() {
        try {
            System.out.println("开始执行通知重试定时任务...");
            notificationSendService.retryFailedNotifications();
            System.out.println("通知重试定时任务执行完成！");
        } catch (Exception e) {
            System.err.println("通知重试定时任务执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
