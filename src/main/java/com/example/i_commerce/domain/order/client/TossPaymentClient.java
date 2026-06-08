package com.example.i_commerce.domain.order.client;

import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.global.exception.AppException;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Profile("!k6")
@RequiredArgsConstructor
public class TossPaymentClient implements PaymentClient {

    private final WebClient tossWebClient;

    @Value("${toss.secretKey}")
    private String SECRET_KEY;
    private String encodedKey;

    @PostConstruct
    public void init() {
        this.encodedKey = Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());
    }

    @Override
    public Map<String, Object> requestConfirm(PaymentConfirmRequest dto) {
        String url = "/confirm";

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", dto.tossOrderId());
        params.put("amount", dto.amount());
        params.put("paymentKey", dto.paymentKey());

        return executePost(url, params);
    }

    @Override
    public Map<String, Object> requestCanceled(PaymentCancelRequest dto) {
        String url = "/" + dto.paymentKey() + "/cancel";

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", dto.cancelReason());
        params.put("cancelAmount", dto.cancelAmount());

        try{
            return executePost(url, params);
        } catch (WebClientRequestException e) {
            throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new AppException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
            }
            if (e.getStatusCode().is5xxServerError()) {
                throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
            }
            throw new AppException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    @Override
    public Map checkPaymentStatus(String paymentKey) {
        String url = "/" + paymentKey;

        try {
            return tossWebClient.get()
                    .uri(url)
                    .headers(httpHeaders -> {
                        httpHeaders.set("Authorization", "Basic " + encodedKey);
                        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (WebClientRequestException e) {
            throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is5xxServerError()) {
                throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
            }
            if (e.getStatusCode().is4xxClientError()) {
                throw new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND);
            }
            throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }

    private Map executePost(String url, Map<String, Object> params) {
        return tossWebClient.post()
                .uri(url)
                .headers(httpHeaders -> {
                    httpHeaders.set("Authorization", "Basic " + encodedKey);
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

    }
}
