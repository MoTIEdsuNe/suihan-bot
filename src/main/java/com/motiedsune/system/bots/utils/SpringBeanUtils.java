package com.motiedsune.system.bots.utils;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

/**
 * 用途：获取 Bean
 */
@Component
public class SpringBeanUtils implements ApplicationContextAware {

    @Getter
    private static ApplicationContext content;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext content) throws BeansException {
        SpringBeanUtils.content = content;
    }


    /** 获取 bean */
    public static <T> T getBean(Class<T> clazz){
        return clazz == null ? null : content.getBean(clazz);
    }

    /** 按名称获取 bean */
    public static Object getBean(String beanName){
        if(Strings.isBlank(beanName)) return null;
        return content.getBean(beanName);
    }

    /** 获取特定 bean */
    public static <T> T getBean(String beanName, Class<T> clazz){
        if(Strings.isBlank(beanName) || clazz == null) return null;
        return content.getBean(beanName,clazz);
    }

    /** 发布事件 */
    public static void publishEvent(ApplicationEvent event){
        if( content == null || event == null) return;
        content.publishEvent(event);
    }
}
