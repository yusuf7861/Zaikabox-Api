package tech.realworks.yusuf.zaikabox.io;

import lombok.Data;

@Data
public class ContactUsRequest {
    private String name;
    private String email;
    private String subject;
    private String message;
}
