package com.rando.springboot.randoJavaBackend.dto;

import com.rando.springboot.randoJavaBackend.entity.User;

public class UserDTO {
    private Integer id;
    private Integer age;
    private String other_side_image_url;
    private String imageUrl;

    public UserDTO(User user){
        this.id = user.getId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getOther_side_image_url() {
        return other_side_image_url;
    }

    public void setOther_side_image_url(String other_side_image_url) {
        this.other_side_image_url = other_side_image_url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
