package com.inn.dao;

import com.inn.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

// Interface to communicate with database through String Data JPA
// Extends JpaRepository provides methods like findAll(), findById(), save(), deleteById(), ...
public interface UserDao extends JpaRepository<User, Integer> {
    // Custom method
    User findByEmail(@Param("email") String email);
}
