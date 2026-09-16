package com.styletry.backend.dto.request;

import lombok.Data;

@Data
public class EsewaPaymentRequest {
    private Long orderId;
    private Double amount;
    private String successUrl;
    private String failureUrl;
}
