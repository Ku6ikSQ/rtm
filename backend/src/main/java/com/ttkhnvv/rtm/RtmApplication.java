package com.ttkhnvv.rtm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;

@SpringBootApplication(exclude = {
		FlywayAutoConfiguration.class,
})
public class RtmApplication {
	public static void main(String[] args) {
		SpringApplication.run(RtmApplication.class, args);
	}
}
