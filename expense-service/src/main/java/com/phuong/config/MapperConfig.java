package com.phuong.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.phuong.mapper")
public class MapperConfig {
    // MapStruct's mappers are automatically detected and registered as Spring beans
    // when using componentModel = "spring" in @Mapper annotation
}