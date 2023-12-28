package com.motiedsune.system.bots.model.condition;

import lombok.Data;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */

@Data
public class GreetingCondition {

    private Long userId;

    private String userFirstName;

    private String type;
}
