package com.motiedsune.system.bots.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motiedsune.system.bots.mapper.BotSleepMapper;
import com.motiedsune.system.bots.model.entity.BotSleep;
import com.motiedsune.system.bots.service.BotSleepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-25 星期一
 */
@Service
@Slf4j
public class BotSleepServiceImpl extends ServiceImpl<BotSleepMapper, BotSleep> implements BotSleepService {
    @Override
    public BotSleep findSleep(Long fromId, Long toId) {
        List<BotSleep> list = this.lambdaQuery()
                .eq(BotSleep::getFromUserId, fromId)
                .eq(BotSleep::getToUserId, toId)
                .ne(BotSleep::getStatus, 90)
                .list();
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            log.warn("Sleep {} to {} 异常！已清空！", fromId, toId);
            this.lambdaUpdate()
                    .eq(BotSleep::getFromUserId, fromId)
                    .eq(BotSleep::getToUserId, toId)
                    .eq(BotSleep::getStatus, 10)
                    .set(BotSleep::getStatus, 90)
                    .update();
            return null;
        }
    }
}
