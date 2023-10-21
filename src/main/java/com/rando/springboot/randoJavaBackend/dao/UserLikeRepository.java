package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {



    // Check if a UserLike relationship exists given specific conditions
    boolean existsByUserAndLikedUserAndLikeStatus(User user, User likedUser, boolean isLike);
    int countByLikedUser(User user);
    List<UserLike> findByUserAndLikeStatusIsNotNull(User user);


    int countByUserAndLikedUserAndLikeStatus(User user, User otherSideUser, boolean likeStatus);
}

