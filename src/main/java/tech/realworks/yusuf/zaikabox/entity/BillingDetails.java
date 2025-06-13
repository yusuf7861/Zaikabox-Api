package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingDetails {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String zip;
    private String locality;
    private String landmark;
    private String country;
    private String state;
}
