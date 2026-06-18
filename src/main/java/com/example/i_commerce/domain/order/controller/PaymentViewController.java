package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.PaymentService;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final OrderService orderService;

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/checkout")
    public String checkoutPage(
            @AuthenticationPrincipal CustomUserPrincipal member,
            @RequestParam Long orderId,
            Model model) {

        PaymentDetailResponse info = paymentService.getPaymentDetails(member.getId(), orderId);

        model.addAttribute("orderId", info.tossOrderId());
        model.addAttribute("customerKey", info.customerKey());
        model.addAttribute("amount", info.amount());
        model.addAttribute("orderName", info.orderName());
        model.addAttribute("customerName", info.customerName());
        model.addAttribute("customerMobilePhone", info.customerMobilePhone());

        return "checkout";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/success")
    public String successPage(
            @AuthenticationPrincipal CustomUserPrincipal member,
            @RequestParam String paymentType,
            @RequestParam String paymentKey,
            @RequestParam String orderId, // 토스 결제 완료 후 리다이렉트 시 전달되는 orderId는 토스에서 생성한 고유 주문 번호입니다. 실제 주문 번호와는 다를 수 있습니다.
            @RequestParam Long amount,
            Model model) {

        orderService.validateOrderOwner(orderId, member.getId());

        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);

        return "success";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/fail")
    public String failPage(
            @RequestParam String code,
            @RequestParam String message,
            Model model) {
        return "fail";
    }

}
