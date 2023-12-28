package com.motiedsune.system.bots.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motiedsune.system.bots.mapper.BotChatMapper;
import com.motiedsune.system.bots.model.entity.BotChat;
import com.motiedsune.system.bots.service.BotChatService;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-08 星期五
 */
@Service
@RequiredArgsConstructor
public class BotChatServiceImpl extends ServiceImpl<BotChatMapper, BotChat> implements BotChatService {

}
