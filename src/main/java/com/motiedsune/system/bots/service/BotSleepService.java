package com.motiedsune.system.bots.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.motiedsune.system.bots.model.entity.BotSleep;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-25 星期一
 */
public interface BotSleepService extends IService<BotSleep> {
    BotSleep findSleep(Long fromId, Long toId);
}
