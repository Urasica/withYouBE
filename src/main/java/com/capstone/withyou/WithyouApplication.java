package com.capstone.withyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WithyouApplication {

	public static void main(String[] args) {
		SpringApplication.run(WithyouApplication.class, args);
	}

}
