package com.motiedsune.system.bots.update;

import com.motiedsune.system.bots.service.IBotSender;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-14 星期四
 */
@Slf4j
@Service
public class UpdateScheduled implements IUpdateCommand {


    @Resource
    private Scheduler scheduler;

    @Resource
    IBotSender sender;
    static Set<String> commands = new HashSet<>(Arrays.asList("scheduled", "timer"));


    @Override
    public Set<String> commands() {
        return commands;
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();
        String[] split = text.split(" ");
        if (split.length < 2) {
            SendMessage result = new SendMessage();
            result.setChatId(chatId);
            result.setText("你想做点什么？\n 格式：\n-> /scheduled [start/stop] msg");
            sender.sender(result);
            return false;
        }

        // 创建JobDetail
        String msg = "喵星晚安协会提醒您，距离休息时间，不足 1 小时！";

        JobDetail jobDetail = JobBuilder.newJob(BaseJob.class)
                .withIdentity("job1", "group1")
                .usingJobData("chatId", chatId)
                .usingJobData("text", msg)
                .storeDurably(true)
                .build();

        // 创建Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .startNow()
                .build();
        try {
            // 启动
            if ("start".equals(split[1])) {
                log.info("start");
                scheduler.deleteJob(jobDetail.getKey());
                scheduler.scheduleJob(jobDetail, trigger);
            }
            // 不要暂停和恢复，只要开始和删除

//             暂停
//            if ("pause".equals(split[1])) {
//                log.info("pause");
//                scheduler.pauseJob(jobDetail.getKey());
//            }
//             恢复
//            if ("resume".equals(split[1])) {
//                log.info("resume");
//                scheduler.resumeJob(jobDetail.getKey());
//            }
            // 删除
            if ("delete".equals(split[1])) {
                log.info("delete");
                scheduler.deleteJob(jobDetail.getKey());
            }
            // 停止所有的定时任务
            if ("shutdown_all".equals(split[1])) {
                log.info("shutdown_all");
                scheduler.shutdown();
            }

            // 列表查看所有的 group
            if ("list".equals(split[1])) {
                String groupName = "group1";
                List<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName))
                        .stream()
                        .toList();

                // 打印每个Job的名称
                for (JobKey jobKey : jobKeys) {
                    System.out.println(jobKey.getName());
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return Boolean.TRUE;
    }

    @Data
    class BaseJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDetail detail = context.getJobDetail();

            // 检查Job是否被暂停
            JobKey jobKey = context.getJobDetail().getKey();
            Scheduler scheduler = context.getScheduler();
            try {
                if (scheduler.checkExists(jobKey) && scheduler.getPausedTriggerGroups().contains(jobKey.getGroup())) {
                    System.out.println("Job已暂停");
                    return;
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

            JobDataMap dataMap = detail.getJobDataMap();
            long id = dataMap.getLong("chatId");
            String msg = dataMap.getString("text");

            Logger logger = LoggerFactory.getLogger(getClass());
            logger.info(msg);
            SendMessage result = new SendMessage();
            result.setChatId(id);
            result.setText(msg);
            sender.sender(result);
        }
    }
}
