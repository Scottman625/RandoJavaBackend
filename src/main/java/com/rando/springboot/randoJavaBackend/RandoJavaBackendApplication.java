package com.rando.springboot.randoJavaBackend;

import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.service.ImportService;
import com.rando.springboot.randoJavaBackend.service.S3Service;
import com.rando.springboot.randoJavaBackend.service.UserLikeService;
import com.rando.springboot.randoJavaBackend.service.UserService;
import io.github.cdimascio.dotenv.Dotenv;
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
	private UserRepository userRepository;

	@Autowired
	private UserLikeService userLikeService;

	@Autowired
	private ImportService importService;

	@Autowired
	private S3Service s3Service;

	public static void main(String[] args) {
//		Dotenv dotenv = Dotenv.load();
//		String accessKey = dotenv.get("AWS_ACCESS_KEY_ID");
//		String secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY");
//
//		System.out.println("Access Key: " + accessKey);
//		System.out.println("Secret Key: " + secretKey);

		SpringApplication.run(RandoJavaBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(){
		return runner -> {
//			userService.updateUser();
//			userLikeService.importUserLike();
//			importService.importCareer();
//			importService.importUserMatch();
//			importService.importChatRoom();
//			userService.setRandomBirthDatesForAllUsers();
//			s3Service.regeneratePresignedUrl(userRepository.findAll());

		};
	}


}
