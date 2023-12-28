package com.motiedsune.system.bots.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-18 星期一
 */

public enum Status {

    DISABLE(0,"未启用"),
    ENABLE(10,"启用"),
    DELETE(90,"删除");

    @Getter
    @EnumValue
    private final Integer value;

    @Getter
    private final String label;
    Status(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
