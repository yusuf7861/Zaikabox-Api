package tech.realworks.yusuf.zaikabox.controller;

import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;
import tech.realworks.yusuf.zaikabox.io.RazorpayPaymentVerificationDTO;
import tech.realworks.yusuf.zaikabox.service.BillingService;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing")
public class PaymentController {

    private final BillingService billingService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate Payment", description = "Creates a Razorpay order and returns the details.")
    public ResponseEntity<OrderResponse> initiatePayment(@RequestBody OrderRequest orderRequest) throws RazorpayException {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createOrder(orderRequest));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Payment", description = "Verifies a Razorpay payment signature.")
    public ResponseEntity<OrderResponse> verifyPayment(@RequestBody RazorpayPaymentVerificationDTO dto) {
        return ResponseEntity.ok(billingService.verifyPayment(dto));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay Webhook", description = "Handles Razorpay webhook events.")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload, @RequestHeader("X-Razorpay-Signature") String signature) {
        billingService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
