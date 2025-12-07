package com.inn.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.inn.POJO.Bill;

public interface BillService {
    String createOrder(int amount, String orderInfo, String baseUrl);
    int orderReturn(HttpServletRequest request);
    ResponseEntity<String> generateReport(Map<String, Object> requestMap);

    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    ResponseEntity<String> deleteBill(@PathVariable Integer id);
}
