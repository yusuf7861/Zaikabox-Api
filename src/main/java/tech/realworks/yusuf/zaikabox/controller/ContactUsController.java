package tech.realworks.yusuf.zaikabox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.io.ContactUsRequest;
import tech.realworks.yusuf.zaikabox.service.ContactUsEmailService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Contact-Us Controller", description = "APIs for handling contact us messages")
public class ContactUsController {

    private final ContactUsEmailService contactUsEmailService;

    @Operation(summary = "Submit contact message", description = "Sends a contact request message via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to send message",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/contact-us")
    @ResponseStatus(HttpStatus.OK)
    public void setContactUsEmailService(@Parameter(description = "Contact request details") @RequestBody ContactUsRequest request) {
        try {
            contactUsEmailService.sendContactUsEmail(request.getEmail(), request.getSubject(), request.getMessage());
        } catch (Exception e) {
           throw new RuntimeException("Failed to send email", e);
        }
    }
}
