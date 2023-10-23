package com.sasha.buildland.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@PropertySource("classpath:application.properties")
@Data //generation getters/setters/toString/NoArgsConstructor
public class BotConfig {

//    @Value("${bot.name}")
    @Value("$BuildlandLiquidationBot")
    String botName;

//    @Value("${bot.token}")
    @Value("6744561174:AAEdE5dPppQc6zrSkpJAQtF-eDOw4QL0fdo")
    String token;

//    @Value("${bot.ownerId}")
//    Long ownerId;


}
