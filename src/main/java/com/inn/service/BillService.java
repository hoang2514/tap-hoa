package com.inn.service;

import jakarta.servlet.http.HttpServletRequest;

public interface BillService {
    String createOrder(int amount, String orderInfo, String baseUrl);
    int orderReturn(HttpServletRequest request);
}
