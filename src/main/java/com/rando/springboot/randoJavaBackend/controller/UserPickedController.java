package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dao.MatchRepository;
import com.rando.springboot.randoJavaBackend.dao.UserLikeRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.dto.UserDTO;
import com.rando.springboot.randoJavaBackend.entity.ApiResponse;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserLike;
import com.rando.springboot.randoJavaBackend.service.JwtService;
import com.rando.springboot.randoJavaBackend.service.UserLikeService;
import com.rando.springboot.randoJavaBackend.service.UserPickedService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user_picked")
public class UserPickedController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserPickedService userPickedService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserLikeRepository userLikeRepository;

    @Autowired
    private UserLikeService userLikeService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getPickedUpUser(
            HttpServletRequest request
    ){
        try{
            User user = jwtService.tokenGetUser(request);
            List<UserDTO> users = userPickedService.getPickedUpUser(user);
            return ResponseEntity.ok(users);
        }catch (Exception e){
            log.info(e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> PickUpUser(
            @RequestParam String likedUserId,
            @RequestParam String isLike,
            HttpServletRequest request
    ) {
        boolean likeStatus;
        if(isLike.equals("True")){
             likeStatus = true;
        }else{
             likeStatus = false;
        }
        try{

            User user = jwtService.tokenGetUser(request);
            Optional<User> optionalUser = userRepository.findById(Integer.valueOf(likedUserId));
            if (optionalUser.isPresent()) {
                User otherSideUser = optionalUser.get();
                // Do something with the user
                if (userLikeRepository.countByUserAndLikedUserAndLikeStatus(user, otherSideUser, likeStatus) == 0) {
                    UserLike userLike = new UserLike();
                    userLike.setUser(user);
                    userLike.setLikedUser(otherSideUser);
                    userLike.setLikeStatus(likeStatus);
                    userLikeService.saveUserLike(userLike);
                }
                int count1 = matchRepository.countByUser1AndUser2(user, otherSideUser);
                int count2 = matchRepository.countByUser2AndUser1(user, otherSideUser);
                if (count1 + count2 != 0){
                    return new ResponseEntity<>(new ApiResponse<>(null, "is matched"), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ApiResponse<>(null, "not matched yet"), HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);


        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }
}
