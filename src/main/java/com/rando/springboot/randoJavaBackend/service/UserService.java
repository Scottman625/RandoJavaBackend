package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.ChatroomMessageRepository;
import com.rando.springboot.randoJavaBackend.dao.MatchRepository;
import com.rando.springboot.randoJavaBackend.dao.UserLikeRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.dto.UserDTO;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private S3Service s3Service; // The S3Service we defined previously

    @Autowired
    private UserLikeRepository userLikeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChatroomMessageRepository messageRepository;

    public int getLikesCount(User user) {
        return userLikeRepository.countByLikedUser(user);
    }

    public List<UserDTO> getMatchNotChattedUser(User user){
        List<UserMatch> matches = matchRepository.findAllByUser(user);
        List<UserMatch> matchesWithoutMessages = matches.stream()
                .filter(match -> messageRepository.countByMatch(match) == 0)
                .collect(Collectors.toList());

        List<User> users = userRepository.findUsersByMatchesWithoutMessagesAndExcludeCurrentUser(matchesWithoutMessages, user.getId());
        List<UserDTO> userDTOS = convertToUserDTOs(users);

        return userDTOS;
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

    public List<UserDTO> convertToUserDTOs(List<User> users){
        List<UserDTO> UserDTOs = new ArrayList<>();

        for (User user: users){
            UserDTO dto = new UserDTO(user);

            BeanUtils.copyProperties(user, dto);
            dto.setName(user.getUsername());
            dto.setImage(s3Service.getPresignedUrl(user.getImage()));
            dto.setAge(getAge(user));
            dto.setCareer(user.getCareer());
            dto.setPhone(user.getPhone());
            dto.setGender(user.getGender());
            dto.setAboutMe(user.getAboutMe());
            UserDTOs.add(dto);
        }
        return UserDTOs;
    }


    private static final Random random = new Random();

    public static Date generateRandomDate(int startYear, int endYear) {
        int dayOfYear = random.nextInt(365) + 1;
        int year = startYear + random.nextInt(endYear - startYear + 1);
        LocalDate localDate = LocalDate.ofYearDay(year, dayOfYear);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    public void setRandomBirthDatesForAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            Date randomDate = generateRandomDate(1950, 2020);  // 假設生日範圍是1950年到2020年
            user.setBirthDate(randomDate);
            userRepository.save(user);
        }
    }


}


