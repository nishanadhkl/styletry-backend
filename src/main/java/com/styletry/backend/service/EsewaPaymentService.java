package com.styletry.backend.service;

import com.styletry.backend.dto.request.EsewaPaymentRequest;
import com.styletry.backend.dto.response.EsewaPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EsewaPaymentService {

    private static final String SIGNED_FIELDS = "total_amount,transaction_uuid,product_code";

    @Value("${esewa.payment-url:https://rc-epay.esewa.com.np/api/epay/main/v2/form}")
    private String paymentUrl;

    @Value("${esewa.product-code:EPAYTEST}")
    private String productCode;

    @Value("${esewa.secret-key:8gBm/:&EnhH.1/q}")
    private String secretKey;

    public EsewaPaymentResponse createPayment(EsewaPaymentRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        String amount = formatAmount(request.getAmount());
        String transactionUuid = "ORD-" + request.getOrderId() + "-" + System.currentTimeMillis();
        String signatureMessage = "total_amount=" + amount
                + ",transaction_uuid=" + transactionUuid
                + ",product_code=" + productCode;

        EsewaPaymentResponse response = new EsewaPaymentResponse();
        response.setPaymentUrl(paymentUrl);
        response.setAmount(amount);
        response.setTaxAmount("0");
        response.setTotalAmount(amount);
        response.setTransactionUuid(transactionUuid);
        response.setProductCode(productCode);
        response.setProductServiceCharge("0");
        response.setProductDeliveryCharge("0");
        response.setSuccessUrl(request.getSuccessUrl());
        response.setFailureUrl(request.getFailureUrl());
        response.setSignedFieldNames(SIGNED_FIELDS);
        response.setSignature(sign(signatureMessage));
        return response;
    }

    private String formatAmount(Double amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat formatter = new DecimalFormat("0.##", symbols);
        return formatter.format(amount);
    }

    private String sign(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to create eSewa payment signature");
        }
    }
}
