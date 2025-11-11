package com.inn.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inn.wrapper.UserWrapper;

import java.util.List;
// import java.util.Map;

@RequestMapping(path = "/user")
public interface UserRest {

    @GetMapping(path = "/get")
    public ResponseEntity<List<UserWrapper>> getAllUser();
}
