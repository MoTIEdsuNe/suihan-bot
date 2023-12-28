package com.motiedsune.system.bots.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.motiedsune.system.bots.model.entity.BotMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-11 星期一
 */
@Mapper
public interface BotMessageMapper extends BaseMapper<BotMessage> {
}
