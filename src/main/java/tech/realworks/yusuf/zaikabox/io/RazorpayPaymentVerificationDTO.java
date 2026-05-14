package tech.realworks.yusuf.zaikabox.io;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayPaymentVerificationDTO {
    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;
    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
    @NotBlank(message = "Order ID is required")
    private String orderId;
}
