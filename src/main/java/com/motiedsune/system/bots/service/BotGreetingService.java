package com.motiedsune.system.bots.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.motiedsune.system.bots.model.condition.GreetingCondition;
import com.motiedsune.system.bots.model.entity.BotGreeting;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */
public interface BotGreetingService extends IService<BotGreeting> {

    // 早安
    String morning(GreetingCondition condition);

    // 晚安
    String night(GreetingCondition condition);

    // 记录
    Boolean record(BotGreeting greeting);
}
