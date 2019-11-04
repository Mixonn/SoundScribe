package com.soundscribe.core;

import com.soundscribe.converter.ConverterService;
import com.soundscribe.jvamp.Host;
import com.soundscribe.jvamp.JvampService;
import com.soundscribe.utilities.SoundscribeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SoundscribeBeCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoundscribeBeCoreApplication.class, args);
    }

    @Bean
    static JvampService jvampService() {
        return new JvampService();
    }

    @Bean
    static SoundscribeConfiguration soundscribeConfiguration() {
        return new SoundscribeConfiguration();
    }

    @Bean
    static ConverterService converterService() {
        return new ConverterService();
    }

    @Bean
    static Host host() {
        return new Host();
    }
}
