package com.inn.serviceImpl;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inn.POJO.OrderStatus;
import com.inn.service.BillService;
import com.inn.service.OrderStatusService;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private BillService billService;

    @RabbitListener(queues = "order.queue")
    @Transactional
    public void processOrderMessage(Map<String, Object> message) {
        try {
            Integer orderId = (Integer) message.get("orderId");
            String action = (String) message.get("action");
            String productDetails = (String) message.get("productDetails");

            log.info("Processing order {} with action {}", orderId, action);

            switch (action) {
                case "process_order":
                    // Process order: validate and deduct stock, set status
                    try {
                        String paymentMethod = (String) message.get("paymentMethod");
                        boolean stockAvailable = billService.validateStock(orderId, productDetails);
                        if (stockAvailable) {
                            billService.deductStockForOrder(orderId);
                            if ("CASH".equals(paymentMethod)) {
                                orderStatusService.updateOrderStatus(orderId, OrderStatus.PREPARING_SHIPMENT);
                            } else {
                                orderStatusService.updateOrderStatus(orderId, OrderStatus.AWAITING_PAYMENT);
                            }
                        } else {
                            orderStatusService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
                        }
                    } catch (Exception ex) {
                        log.error("Error processing order {}", orderId, ex);
                        orderStatusService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
                    }
                    break;
                default:
                    log.warn("Unknown action: {}", action);
            }
        } catch (Exception e) {
            log.error("Error processing order message", e);
        }
    }
}