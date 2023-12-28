package com.motiedsune.system.bots.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motiedsune.system.bots.mapper.BotGreetingMapper;
import com.motiedsune.system.bots.model.condition.GreetingCondition;
import com.motiedsune.system.bots.model.entity.BotGreeting;
import com.motiedsune.system.bots.service.BotGreetingService;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */
@Service
@RequiredArgsConstructor
public class BotGreetingServiceImpl extends ServiceImpl<BotGreetingMapper, BotGreeting> implements BotGreetingService {

    @Override
    public String morning(GreetingCondition condition) {
        // 查询用户上次讲晚安的时间
        // 若有 计算时间差
        return null;
    }

    @Override
    public String night(GreetingCondition condition) {
        return null;
    }

    @Override
    public Boolean record(BotGreeting greeting) {
        return null;
    }
}
