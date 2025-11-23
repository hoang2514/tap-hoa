package com.inn.restImpl;

import com.inn.rest.BillRest;
import com.inn.service.BillService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class BillRestImpl implements BillRest {

    @Autowired
    private BillService billService;

    @Override
    public String showPaymentPage() {
        return "index"; // Trả về tên của file HTML (không có đuôi .html)
    }

    @Override
    public String submitOrder(int orderTotal, String orderInfo, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = billService.createOrder(orderTotal, orderInfo, baseUrl);
        return "redirect:" + vnpayUrl;
    }

    @Override
    public String vnpayReturn(HttpServletRequest request, Model model) {
        int paymentStatus = billService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
    }
}
