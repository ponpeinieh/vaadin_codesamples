package com.example.application.data.repository;


import com.example.application.data.entity.User;

public interface UserDao {

    public User findByUserName(String userName);
    
    public void save(User user);
    
}
