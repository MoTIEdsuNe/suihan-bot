package com.motiedsune.system.bots.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 用途：
 *
 * @author Xander Lau
 * @date 2023-12-28 星期四
 */
@Component
@Slf4j
public class BeanConfig {

    @Bean
    public ObjectMapper configObjectMapper(){
        return new ObjectMapper();
    }
}
