package com.thoughtworks.rslist.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @NotNull
    @Size(max = 8)
    private String name;
    @NotNull
    private String gender;
    @NotNull
    @Min(18)
    @Max(100)
    private Integer age;
    @Email
    private String email;
    @Pattern(regexp = "1\\d{10}")
    private String phone;
    private Integer votes;

    public User(String name, String gender, Integer age, String email, String phone) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonIgnore
    public Integer getVotes() {
        return votes;
    }

    @JsonProperty
    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
