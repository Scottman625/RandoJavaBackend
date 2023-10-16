package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.MatchRepository;
import com.rando.springboot.randoJavaBackend.dao.UserLikeRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserLike;
import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserLikeService {

    @Autowired
    private UserLikeRepository userLikeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    public void importUserLike(){
        for (int i = 200; i <= 300; i++){
            UserLike userLike = new UserLike();
            String string = String.valueOf(i);
            String phone = "0910000000";
            phone = phone.substring(0, phone.length() - string.length()) + string;
            Optional<User> userOptional = userRepository.findByPhone(phone);
            User user = userOptional.get();
            userLike.setUser(user);
            userLike.setLikedUser(userRepository.findById(1));
            userLike.setLikeStatus(true);
            userLikeRepository.save(userLike);
        }
    }

    public void saveUserLike(UserLike userLike) {
        userLikeRepository.save(userLike);

        if (userLike.getLikeStatus() &&
                userLikeRepository.existsByUserAndLikedUserAndLikeStatus(userLike.getLikedUser(), userLike.getUser(), true) &&
                matchRepository.countByUser1AndUser2(userLike.getUser(), userLike.getLikedUser()) == 0) {

            UserMatch match = new UserMatch();
            match.setUser1(userLike.getUser());
            match.setUser2(userLike.getLikedUser());
            matchRepository.save(match);
        }
    }
}

