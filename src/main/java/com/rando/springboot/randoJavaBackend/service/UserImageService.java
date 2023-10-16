package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.UserImageRepository;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserImageService {

    @Autowired
    private UserImageRepository userImageRepository;

    public UserImage saveUserImage(UserImage userImage) {
        UserImage savedImage = userImageRepository.save(userImage);
        User user = savedImage.getUser();
        user.setImage(savedImage.getImage());  // Assuming the user entity has a setImage method
        // save the user as well if needed
        return savedImage;
    }

    public void deleteUserImage(UserImage userImage) {
        userImageRepository.delete(userImage);
        User user = userImage.getUser();
        UserImage latestImage = userImageRepository.findFirstByUserOrderByUpdateAtDesc(user);
        if (latestImage != null) {
            user.setImage(latestImage.getImage());
            // save the user as well if needed
        }
    }
}

