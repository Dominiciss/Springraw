package com.dominicis.springraw.Entity;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class User {
    private Integer id;
    private String username;
    private String password;
    // Creation date
    private Date date;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(Integer id, String username, String password, Date date) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
