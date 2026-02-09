package com.introms.config;

import org.apache.tika.Tika;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceProcessorConfig {
    @Bean
    public Tika tika(){
        return new Tika();
    }

    @Bean
    public AutoDetectParser autoDetectParser(){
        return new AutoDetectParser();
    }

}
