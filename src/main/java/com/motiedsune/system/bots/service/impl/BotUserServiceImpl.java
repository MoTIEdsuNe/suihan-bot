package com.motiedsune.system.bots.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motiedsune.system.bots.mapper.BotUserMapper;
import com.motiedsune.system.bots.model.entity.BotUser;
import com.motiedsune.system.bots.service.BotUserService;
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
public class BotUserServiceImpl extends ServiceImpl<BotUserMapper, BotUser> implements BotUserService {

}
