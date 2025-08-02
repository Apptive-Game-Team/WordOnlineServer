package com.wordonline.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WordOnlineServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordOnlineServerApplication.class, args);
    }

}
