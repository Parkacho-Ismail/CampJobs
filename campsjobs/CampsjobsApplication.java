package com.example.campsjobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableScheduling  // Enables scheduled tasks
public class CampsjobsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampsjobsApplication.class, args);
	}


}
