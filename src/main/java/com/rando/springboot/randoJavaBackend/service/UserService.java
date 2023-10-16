package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.UserLikeRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Calendar;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private UserLikeRepository userLikeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public int getLikesCount(User user) {
        return userLikeRepository.countByLikedUser(user);
    }

    public Integer getAge(User user) {
        Date birthDate = user.getBirthDate();
        if (birthDate == null) {
            return null;
        }

        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);

        Calendar todayCal = Calendar.getInstance();

        int age = todayCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
        if (todayCal.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
                (todayCal.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) && todayCal.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }


    public String getConstellation(User user) {
        Date birthDate = user.getBirthDate();
        if (birthDate == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDate);

        int month = calendar.get(Calendar.MONTH) + 1;  // Calendar.MONTH is zero-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "雙魚座";
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "牡羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 20)) return "雙子座";
        if ((month == 6 && day >= 21) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "獅子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "處女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 22)) return "天秤座";
        if ((month == 10 && day >= 23) || (month == 11 && day <= 21)) return "天蠍座";
        if ((month == 11 && day >= 22) || (month == 12 && day <= 21)) return "射手座";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座";

        return null;
    }

    public void updateUser() {
        for (int i = 0; i < 400; i++) {
            String string = String.valueOf(i);
            String phone = "0910000000";
            phone = phone.substring(0, phone.length() - string.length()) + string;
            Optional<User> userOptional = userRepository.findByPhone(phone);

//
            User user = userOptional.get();;
            user.setPhone(phone);
            user.setPassword("$2a$10$MjWKtfFMTmKLBcUkABgDVOoyMtVRq9D/fsY6ogLrh.WxqiB.6wk6y");

            userRepository.save(user);
        }
    }

//    public void createUser() {
//        for (int i = 200; i < 400; i++) {
//            String string = String.valueOf(i);
//            String phone = "0910000000";
//            phone = phone.substring(0, phone.length() - string.length()) + string;
//
//            User user = new User();
//            user.setPhone(phone);
//            user.setPassword("admin");
//            user.setUsername("Test" + string);
//            user.setGender(User.Gender.FEMALE);
//            user.setSearchGender(User.Gender.MALE);
//            user.setImage("https://rando-app-bucket.s3.amazonaws.com/images/53931d0a-269a-11ee-a7ec-767e3dc7197d.jpeg?AWSAccessKeyId=AKIA4N73ISGH4N5BIHUI&Signature=V0FqP7Fe685JzB8Ffh5kmf59sKI%3D&Expires=1689821590");
//
//            userRepository.save(user);
//        }
//    }


    public User validateUser(String phone, String password) {
        Optional<User> userOptional = userRepository.findByPhone(phone);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) { // Use encoder to compare passwords
                return user;
            }
        }
        return null;
    }

    public User createUser(String phone, String password) {
        User user = new User();
        user.setPhone(phone);
        String hashedPassword = passwordEncoder.encode(password); // Encrypting the password using bcrypt
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public User findByPhoneAndPassword(String phone, String password) {
        return userRepository.findByPhoneAndPassword(phone, password);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


}


