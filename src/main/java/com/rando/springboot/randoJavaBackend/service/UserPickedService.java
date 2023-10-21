package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.dao.UserLikeRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.dto.UserDTO;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserLike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPickedService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserLikeRepository userLikeRepository;

    public List<UserDTO> getPickedUpUser(User currentUser){
        List<Long> pickedUsersIdList = userLikeRepository.findByUserAndLikeStatusIsNotNull(currentUser)
                .stream()
                .map(UserLike::getLikedUser)
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());

        List<User> notPickedYetUsers;
        if (pickedUsersIdList.isEmpty()) {
            notPickedYetUsers = userRepository.findByIdNotAndGenderNot(currentUser.getId(), currentUser.getGender());
        } else {
            notPickedYetUsers = userRepository.findByIdNotAndGenderNotAndIdNotIn(currentUser.getId(), currentUser.getGender(), pickedUsersIdList);
        }

        return userService.convertToUserDTOs(notPickedYetUsers);
    }

}
