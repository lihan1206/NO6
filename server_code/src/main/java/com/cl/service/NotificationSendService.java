package com.cl.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.TongzhisongjiliaoEntity;
import com.cl.entity.YishengyuyueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 通知发送服务类
 * 负责创建通知记录、发送通知和重试机制
 */
@Service
public class NotificationSendService {

    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;

    @Autowired
    private TongzhisongjiliaoService tongzhisongjiliaoService;

    private static final int MAX_RETRY_COUNT = 3;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 预约成功后立即创建所有通知记录并尝试发送
     * @param yuyue 预约记录
     */
    @Transactional
    @Async
    public void createAndSendNotifications(YishengyuyueEntity yuyue) {
        try {
            // 1. 创建预约成功通知
            createNotification(yuyue, "预约成功通知", "您的预约已成功，请准时就诊！", new Date());
            
            // 2. 创建就诊前一天提醒
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(yuyue.getYuyueshijian());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            createNotification(yuyue, "就诊前提醒", "明天就是您的就诊时间，请提前做好准备！", calendar.getTime());
            
            // 3. 创建就诊当天提醒
            calendar = Calendar.getInstance();
            calendar.setTime(yuyue.getYuyueshijian());
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND, 0);
            createNotification(yuyue, "就诊当天提醒", "今天是您的就诊时间，请准时到达！", calendar.getTime());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建单条通知记录
     */
    private void createNotification(YishengyuyueEntity yuyue, String type, String content, Date notifyTime) {
        JiuzhentongzhiEntity notification = new JiuzhentongzhiEntity();
        notification.setTongzhibianhao(generateNotificationNo());
        notification.setYishengzhanghao(yuyue.getYishengzhanghao());
        notification.setDianhua(yuyue.getDianhua());
        notification.setJiuzhenshijian(yuyue.getYuyueshijian());
        notification.setTongzhishijian(notifyTime);
        notification.setZhanghao(yuyue.getZhanghao());
        notification.setShouji(yuyue.getShouji());
        notification.setTongzhibeizhu(content);
        notification.setTongzhileixing(type);
        notification.setSendstatus(0); // 待发送
        notification.setRetrycount(0);
        notification.setAddtime(new Date());
        
        jiuzhentongzhiService.insert(notification);
        
        // 立即尝试发送
        sendNotification(notification);
    }

    /**
     * 发送通知
     */
    @Transactional
    public void sendNotification(JiuzhentongzhiEntity notification) {
        boolean sendSuccess = false;
        String errorMessage = "";
        
        try {
            // 这里模拟通知发送逻辑
            // 实际项目中应该接入短信平台、邮件服务或推送服务
            // 这里我们假设发送成功，为了演示，也可以模拟失败情况
            sendSuccess = true;
            
            // 如果要测试失败重试机制，可以取消下面这行的注释
            // if (notification.getRetrycount() < 1) throw new RuntimeException("模拟发送失败");
            
        } catch (Exception e) {
            sendSuccess = false;
            errorMessage = e.getMessage();
        }
        
        // 记录发送日志
        recordSendLog(notification, sendSuccess, errorMessage);
        
        // 更新通知状态
        updateNotificationStatus(notification, sendSuccess);
    }

    /**
     * 记录发送日志
     */
    private void recordSendLog(JiuzhentongzhiEntity notification, boolean success, String errorMsg) {
        TongzhisongjiliaoEntity log = new TongzhisongjiliaoEntity();
        log.setTongzhibianhao(notification.getTongzhibianhao());
        log.setTongzhileixing(notification.getTongzhileixing());
        log.setZhanghao(notification.getZhanghao());
        log.setShouji(notification.getShouji());
        log.setSongzhuangtai(success ? 1 : 0);
        log.setCuowuxinxi(errorMsg);
        log.setSongshijian(new Date());
        log.setAddtime(new Date());
        
        tongzhisongjiliaoService.insert(log);
    }

    /**
     * 更新通知状态
     */
    private void updateNotificationStatus(JiuzhentongzhiEntity notification, boolean success) {
        if (success) {
            notification.setSendstatus(1); // 发送成功
        } else {
            notification.setSendstatus(2); // 发送失败
            notification.setRetrycount(notification.getRetrycount() + 1);
        }
        notification.setLasttrytime(new Date());
        
        jiuzhentongzhiService.updateById(notification);
    }

    /**
     * 重试发送失败的通知
     */
    @Transactional
    public void retryFailedNotifications() {
        // 查询发送失败且未超过最大重试次数的通知
        EntityWrapper<JiuzhentongzhiEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("sendstatus", 2);
        wrapper.lt("retrycount", MAX_RETRY_COUNT);
        
        List<JiuzhentongzhiEntity> failedNotifications = jiuzhentongzhiService.selectList(wrapper);
        
        for (JiuzhentongzhiEntity notification : failedNotifications) {
            sendNotification(notification);
        }
    }

    /**
     * 手动重试指定的通知
     */
    @Transactional
    public void retryNotification(Long notificationId) {
        JiuzhentongzhiEntity notification = jiuzhentongzhiService.selectById(notificationId);
        if (notification != null && notification.getSendstatus() == 2) {
            sendNotification(notification);
        }
    }

    /**
     * 生成通知编号
     */
    private String generateNotificationNo() {
        return "TZ" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
