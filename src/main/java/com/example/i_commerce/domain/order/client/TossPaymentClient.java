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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final RestTemplate restTemplate;

    @Value("${toss.secretKey}")
    private String SECRET_KEY;
    private String encodedKey;

    private static final String BASE_URL = "https://api.tosspayments.com/v1/payments";

    @PostConstruct
    public void init() {
        this.encodedKey = Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());
    }

    @Retry(name = "tossConfirmRetry", fallbackMethod = "checkPaymentStatus")
    public Map<String, Object> requestConfirm(PaymentConfirmRequest dto) {
        String url =  BASE_URL + "/confirm";

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", dto.tossOrderId());
        params.put("amount", dto.amount());
        params.put("paymentKey", dto.paymentKey());

        try{
            return executePost(url, params);
        } catch (HttpStatusCodeException e) {
            throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }

    public Map<String, Object> requestCanceled(PaymentCancelRequest dto) {
        String url = BASE_URL+ "/" + dto.paymentKey() + "/cancel";

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", dto.cancelReason());
        params.put("cancelAmount", dto.cancelAmount());

        try{
            return executePost(url, params);
        } catch (ResourceAccessException e) {
            throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
        }catch (HttpStatusCodeException e) {
            throw new AppException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    public Map<String, Object> checkPaymentStatus(PaymentConfirmRequest dto, ResourceAccessException e) {
        String url = BASE_URL + "/payments/" + dto.paymentKey();

        HttpHeaders headers = getCommonHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();

        } catch (ResourceAccessException ex) {
            throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
        } catch (HttpStatusCodeException ex) {
            throw new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    private Map<String, Object> executePost(String url, Map<String, Object> params) {
        HttpHeaders headers = getCommonHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }

    private HttpHeaders getCommonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
