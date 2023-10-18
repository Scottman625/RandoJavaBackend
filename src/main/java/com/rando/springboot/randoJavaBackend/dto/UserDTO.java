package com.rando.springboot.randoJavaBackend.dto;

import com.rando.springboot.randoJavaBackend.entity.User;

public class UserDTO {
    private Long id;
    private Integer age;
    private String other_side_image_url;
    private String image;
    private String name;

    public UserDTO(User user){
        this.id = user.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
