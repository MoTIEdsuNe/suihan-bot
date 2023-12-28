package com.motiedsune.system.bots.scheduler;

import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.utils.SpringBeanUtils;
import org.apache.http.util.Asserts;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-19 星期二
 */
@Component
public class BotSleepJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        IBotBaseService botBaseService = SpringBeanUtils.getBean(IBotBaseService.class);
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Map<String,Object> map = new HashMap<>(dataMap);
        Long chatId = (Long) map.getOrDefault("chatId",null);
        Integer messageId = (Integer) map.getOrDefault("messageId",null);
        String message = (String) map.getOrDefault("message",null);
        Asserts.notNull(chatId,"推送失败，chatId 为空。");
        Asserts.notNull(messageId,"推送失败，messageId 为空。");
        Asserts.notNull(message,"推送失败，message 为空。");

        botBaseService.sendMessage(chatId, messageId,message);
    }
}
