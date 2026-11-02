package com.example.modeldemo;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class HumanDto {

    private String name;
    private long age;
    private double height;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime birthday;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }
}
