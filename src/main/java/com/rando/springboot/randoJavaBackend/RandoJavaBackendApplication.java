package com.rando.springboot.randoJavaBackend;

import com.rando.springboot.randoJavaBackend.service.ImportService;
import com.rando.springboot.randoJavaBackend.service.UserLikeService;
import com.rando.springboot.randoJavaBackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class RandoJavaBackendApplication {

	@Autowired
	private UserService userService;

	@Autowired
	private UserLikeService userLikeService;

	@Autowired
	private ImportService importService;

	public static void main(String[] args) {

		SpringApplication.run(RandoJavaBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(){
		return runner -> {
//			userService.updateUser();
//			userLikeService.importUserLike();
//			importService.importCareer();
			importService.importUserMatch();
			importService.importChatRoom();

		};
	}


}
