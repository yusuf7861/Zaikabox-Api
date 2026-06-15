package tech.realworks.yusuf.zaikabox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;
import tech.realworks.yusuf.zaikabox.io.RazorpayPaymentVerificationDTO;
import tech.realworks.yusuf.zaikabox.service.BillingService;
import tech.realworks.yusuf.zaikabox.service.IdempotencyService;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing")
public class PaymentController {

    private final BillingService billingService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate Payment", description = "Creates a Razorpay order and returns the details.")
    public ResponseEntity<OrderResponse> initiatePayment(
            @Valid @RequestBody OrderRequest orderRequest,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        OrderResponse response = idempotencyService.execute(idempotencyKey, "payment-initiate", () -> billingService.createOrder(orderRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Payment", description = "Verifies a Razorpay payment signature.")
    public ResponseEntity<OrderResponse> verifyPayment(
            @Valid @RequestBody RazorpayPaymentVerificationDTO dto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        OrderResponse response = idempotencyService.execute(idempotencyKey, "payment-verify", () -> billingService.verifyPayment(dto));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay Webhook", description = "Handles Razorpay webhook events.")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload, @RequestHeader("X-Razorpay-Signature") String signature) {
        billingService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
