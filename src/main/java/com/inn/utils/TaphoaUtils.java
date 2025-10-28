package com.inn.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TaphoaUtils {
    private TaphoaUtils() {}

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body("{\"message\":\"" + responseMessage + "\"}");
    }
}
