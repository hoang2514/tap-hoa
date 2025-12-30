package com.inn.service;

import com.inn.POJO.Bill;
import com.inn.POJO.OrderStatus;

public interface OrderStatusService {
    void updateOrderStatus(Integer orderId, OrderStatus newStatus) throws Exception;
}