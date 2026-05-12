package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.service.PaymentService;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam Long paymentId, Model model) {

        PaymentDetailResponse info = paymentService.getPaymentDetails(paymentId);

        model.addAttribute("orderId", info.tossOrderId());
        model.addAttribute("customerKey", info.customerKey());
        model.addAttribute("amount", info.amount());
        model.addAttribute("orderName", info.orderName());
        model.addAttribute("customerName", info.customerName());
        model.addAttribute("customerMobilePhone", info.customerMobilePhone());
        return "checkout";
    }

    @GetMapping("/success")
    public String successPage(
            @RequestParam String paymentType,
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {

        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);

        return "success";
    }

    @GetMapping("/fail")
    public String failPage(
            @RequestParam String code,
            @RequestParam String message,
            Model model) {
        return "fail";
    }

}
