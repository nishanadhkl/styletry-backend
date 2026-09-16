package com.styletry.backend.controller;

import com.styletry.backend.dto.request.EsewaPaymentRequest;
import com.styletry.backend.dto.response.EsewaPaymentResponse;
import com.styletry.backend.service.EsewaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final EsewaPaymentService esewaPaymentService;

    @PostMapping("/esewa/initiate")
    public ResponseEntity<EsewaPaymentResponse> initiateEsewaPayment(@RequestBody EsewaPaymentRequest request) {
        return ResponseEntity.ok(esewaPaymentService.createPayment(request));
    }
}
