package ru.isg.invest.helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class InvestHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestHelperApplication.class, args);
    }

}
