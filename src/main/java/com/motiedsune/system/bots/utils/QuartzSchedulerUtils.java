package com.motiedsune.system.bots.utils;

import jakarta.annotation.Resource;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-18 星期一
 */

@Component
public class QuartzSchedulerUtils {

    @Resource
    private Scheduler scheduler;
    private JobDetail jobDetail;
    private Trigger trigger;

    Logger logger = LoggerFactory.getLogger(getClass());

    private QuartzSchedulerUtils() {
        try {
            if(scheduler != null) return;
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("定时任务异常!");
        }
    }

    public static QuartzSchedulerUtils create() {
        return new QuartzSchedulerUtils();
    }

    public static QuartzSchedulerUtils create(JobDetailSupplier jobDetailSupplier, TriggerSupplier triggerSupplier) {
        return new QuartzSchedulerUtils().withJobDetail(jobDetailSupplier).withTrigger(triggerSupplier);
    }


    public QuartzSchedulerUtils withJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
        return this;
    }

    public QuartzSchedulerUtils withTrigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }

    public QuartzSchedulerUtils withJobDetail(JobDetailSupplier supplier) {
        jobDetail = supplier.get();
        return this;
    }

    public QuartzSchedulerUtils withTrigger(TriggerSupplier supplier) {
        trigger = supplier.get();
        return this;
    }

    public void schedule() {
        if (jobDetail == null) {
            logger.info("定时任务加入失败！缺失 jobDetail!");
            return;
        }
        if (trigger == null) {
            logger.info("定时任务加入失败！缺失 trigger!");
            return;
        }
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            logger.error("定时任务启用失败！", e);
        }
    }

    public void interrupt(){
        try {
            scheduler.pauseJob(jobDetail.getKey());
            scheduler.deleteJob(jobDetail.getKey());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface JobDetailSupplier {
        JobDetail get();
    }

    @FunctionalInterface
    public interface TriggerSupplier {
        Trigger get();
    }
}
