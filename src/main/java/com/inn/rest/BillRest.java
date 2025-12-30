package com.inn.rest;

import com.inn.POJO.Bill;
import com.inn.POJO.OrderStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/bill")
public interface BillRest {

    @PostMapping(path="/generateReport")
    ResponseEntity<?> generateReport(@RequestBody Map<String, Object> requestMap);

    @GetMapping(path="/getBills")
    ResponseEntity<List<Bill>> getBills();

    @PostMapping(path = "/getPdf")
    ResponseEntity<byte[]> getPdf(@RequestBody Map<String, Object> requestMap);

    @PostMapping(path="/delete/{id}")
    ResponseEntity<String> deleteBill(@PathVariable Integer id);

    @GetMapping(path="/getUserBills")
    ResponseEntity<List<Bill>> getUserBills();

    @PostMapping(path = "/cancelOrder/{id}")
    ResponseEntity<String> cancelOrder(@PathVariable Integer id);

    @PostMapping(path = "/updateOrderStatus/{id}")
    ResponseEntity<String> updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, Object> requestMap);

    @GetMapping(path="/status/{uuid}")
    ResponseEntity<?> getBillStatus(@PathVariable String uuid);
}
