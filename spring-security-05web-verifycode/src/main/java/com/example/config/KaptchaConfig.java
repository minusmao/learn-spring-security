package com.example.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptcha() {
        Properties properties = new Properties();
        //1.验证码宽度
        properties.setProperty("kaptcha.image.width", "150");
        //2.验证码高度
        properties.setProperty("kaptcha.image.height", "50");
        //3.验证码字符串
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
        //4.验证码长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

}