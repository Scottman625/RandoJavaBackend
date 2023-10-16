package com.rando.springboot.randoJavaBackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interest")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interest")
    private String interest;

    public String toString() {
        return interest;
    }

    public Interest(){}

    // getters, setters, toString(), etc.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }
}

