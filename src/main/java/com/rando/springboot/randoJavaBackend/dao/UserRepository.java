package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhone(String phone);
    User findByUsername(String username);
    User findById(int id);
    User findByPhoneAndPassword(String phone,String password);
    @Query("SELECT DISTINCT u FROM User u WHERE " +
            "(u IN (SELECT m.user1 FROM UserMatch m WHERE m IN :matchesWithoutMessages) OR " +
            " u IN (SELECT m.user2 FROM UserMatch m WHERE m IN :matchesWithoutMessages)) " +
            "AND u.id <> :currentUserId")
    List<User> findUsersByMatchesWithoutMessagesAndExcludeCurrentUser(
            @Param("matchesWithoutMessages") List<UserMatch> matchesWithoutMessages,
            @Param("currentUserId") Long currentUserId
    );

    @Query("SELECT u From User u WHERE u.id <> :currentUserId and u.gender <> :currentUserGender")
    List<User> findByIdNotAndGenderNot(@Param("currentUserId") Long currentUserId, @Param("currentUserGender") User.Gender currentUserGender);


    List<User> findByIdNotAndGenderNotAndIdNotIn(long id, User.Gender gender, List<Long> pickedUsersIdList);

    @Query("SELECT u FROM User u WHERE u <> ?1 AND u.id IN ?2")
    User findFirstNotAndIn(User user, List<Long> userIds);
}
