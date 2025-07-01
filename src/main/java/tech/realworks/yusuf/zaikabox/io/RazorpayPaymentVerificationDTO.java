package tech.realworks.yusuf.zaikabox.io;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayPaymentVerificationDTO {
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private String orderId;
}

