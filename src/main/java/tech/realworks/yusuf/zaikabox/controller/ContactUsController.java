package tech.realworks.yusuf.zaikabox.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.io.ContactUsRequest;
import tech.realworks.yusuf.zaikabox.service.ContactUsEmailService;

@RestController
@RequiredArgsConstructor
public class ContactUsController {

    private final ContactUsEmailService contactUsEmailService;

    @PostMapping("/contact-us")
    @ResponseStatus(HttpStatus.OK)
    public void setContactUsEmailService(@RequestBody ContactUsRequest request) {
        try {
            contactUsEmailService.sendContactUsEmail(request.getEmail(), request.getSubject(), request.getMessage());
        } catch (Exception e) {
           throw new RuntimeException("Failed to send email", e);
        }
    }
}
