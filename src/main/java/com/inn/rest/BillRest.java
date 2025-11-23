package com.inn.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/bill")
public interface BillRest {
    @GetMapping("")
    String showPaymentPage();

    @PostMapping("/submitOrder")
    String submitOrder(@RequestParam("amount") int orderTotal,
                       @RequestParam("orderInfo") String orderInfo,
                       HttpServletRequest request);

    @GetMapping("/vnpay-payment")
    String vnpayReturn(HttpServletRequest request, Model model);
}
