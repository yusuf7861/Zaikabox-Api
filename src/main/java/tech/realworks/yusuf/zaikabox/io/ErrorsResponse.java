package tech.realworks.yusuf.zaikabox.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ErrorsResponse {
    private String message;
    private HttpStatus status;
}
