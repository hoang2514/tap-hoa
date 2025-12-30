package com.inn.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.inn.POJO.Bill;
import com.inn.POJO.OrderStatus;

public interface BillService {
    String createOrder(int amount, String orderInfo, String baseUrl);
    int orderReturn(HttpServletRequest request);

    void handlePaymentResult(int paymentStatus, String uuid);
    ResponseEntity<?> generateReport(Map<String, Object> requestMap);

    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    ResponseEntity<String> deleteBill(@PathVariable Integer id);

    ResponseEntity<List<Bill>> getUserBills();

    ResponseEntity<String> cancelOrder(@PathVariable Integer id);

    ResponseEntity<String> updateOrderStatus(@PathVariable Integer id, OrderStatus newStatus);

    void deductStock(String productDetailsJson);

    void addStockBack(String productDetailsJson);

    ResponseEntity<?> getBillStatus(String uuid);

    boolean validateStock(Integer orderId, String productDetails);

    void deductStockForOrder(Integer orderId);
}
