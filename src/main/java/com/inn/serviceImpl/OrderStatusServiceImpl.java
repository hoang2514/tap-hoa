package com.inn.serviceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inn.POJO.Bill;
import com.inn.POJO.OrderStatus;
import com.inn.dao.BillDao;
import com.inn.service.OrderStatusService;

@Service
public class OrderStatusServiceImpl implements OrderStatusService {

    @Autowired
    private BillDao billDao;

    @Override
    public void updateOrderStatus(Integer orderId, OrderStatus newStatus) throws Exception {
        Bill bill = billDao.findById(orderId).orElseThrow(() -> new Exception("Order not found"));
        OrderStatus currentStatus = bill.getStatus();

        if (newStatus == OrderStatus.PREPARING_SHIPMENT && currentStatus == OrderStatus.AWAITING_PAYMENT) {
        } else if (newStatus == OrderStatus.CANCELLED && currentStatus == OrderStatus.AWAITING_PAYMENT) {
        } else if (newStatus == OrderStatus.AWAITING_PAYMENT && currentStatus == OrderStatus.CONFIRMING) {
        } else if (newStatus == OrderStatus.CANCELLED && currentStatus == OrderStatus.CONFIRMING) {
        } else if (newStatus == OrderStatus.PREPARING_SHIPMENT && currentStatus == OrderStatus.CONFIRMING) {
        } else {
            List<OrderStatus> allowedFrom = Arrays.asList(OrderStatus.PREPARING_SHIPMENT, OrderStatus.SHIPPING);
            if (!allowedFrom.contains(currentStatus) && !newStatus.equals(OrderStatus.CANCELLED)) {
                throw new Exception("Đơn hàng không thể chuyển trạng thái");
            }
        }

        // Chuyển đổi giữa các trạng thái đơn hàng
        if (newStatus == OrderStatus.SHIPPING && currentStatus != OrderStatus.PREPARING_SHIPMENT) {
            throw new Exception("Đơn hàng không thể chuyển trạng thái");
        }
        if (newStatus == OrderStatus.DELIVERED && currentStatus != OrderStatus.SHIPPING) {
            throw new Exception("Đơn hàng không thể chuyển trạng thái");
        }

        bill.setStatus(newStatus);
        bill.setUpdatedDate(LocalDateTime.now());
        billDao.save(bill);
    }
}