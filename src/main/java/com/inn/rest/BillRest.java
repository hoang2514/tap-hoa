package com.inn.rest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/bill")
public interface BillRest {
    
    @PostMapping(path="/generateReport")
    ResponseEntity<String> generateReport(@RequestBody Map<String, Object> requestMap);
}
