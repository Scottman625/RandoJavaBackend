package com.rando.springboot.randoJavaBackend.entity;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.rando.springboot.randoJavaBackend.dao.UserLikeRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="phone",unique = true, length = 10)
    private String phone;

    @Column(name="password")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name="is_active")
    private Boolean isActive = true;

    @Column(name="is_staff")
    private Boolean isStaff = false;

    @Column(name="name")
    private String username;


    @Temporal(TemporalType.DATE)
    @Column(name="birth_date")
    private Date birthDate;

    @Column(name="career")
    private String career;

    @Lob
    @Column(name="about_me")
    private String aboutMe;

    @Column(name="image")
    private String image;


    public enum Gender {
        MALE, FEMALE, ALL
    }

    @Enumerated(EnumType.STRING)
    @Column(name="gender")
    private Gender gender = Gender.MALE;

    @Enumerated(EnumType.STRING)
    @Column(name="search_gender")
    private Gender searchGender = Gender.FEMALE;

    @Column(name="email")
    private String email;

    @Column(name="address")
    private String address;

    @Column(name="line_id",unique = true)
    private String lineId;

    @Column(name="apple_id",unique = true)
    private String appleId;

    @Lob
    @Column(name="background_image")
    private Byte[] backgroundImage;

    // ... Getters, Setters, and other utility methods like age(), constellation() ...


    public User() {
    }

    public User(String phone, String name, String email) {
        this.phone = phone;
        this.username = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getStaff() {
        return isStaff;
    }

    public void setStaff(Boolean staff) {
        isStaff = staff;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getSearchGender() {
        return searchGender;
    }

    public void setSearchGender(Gender searchGender) {
        this.searchGender = searchGender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getAppleId() {
        return appleId;
    }

    public void setAppleId(String appleId) {
        this.appleId = appleId;
    }

    public Byte[] getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Byte[] backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
}
