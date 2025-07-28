package com.phuong;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Discovery Server is running");
    }
}