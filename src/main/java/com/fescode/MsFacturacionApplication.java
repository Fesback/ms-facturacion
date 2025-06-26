package com.fescode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,SecurityAutoConfiguration.class })
public class MsFacturacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsFacturacionApplication.class, args);
    }

    // configuracmos pues no usara la base de datos el microservicio

}
