package tech.realworks.yusuf.zaikabox.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactUsEmailService {

    private final JavaMailSender javaMailSender;

    public void sendContactUsEmail(String email, String subject, String message) {
        SimpleMailMessage simpleMailSender = new SimpleMailMessage();
        simpleMailSender.setFrom("yjamal12feb@gmail.com");
        simpleMailSender.setTo("yjamalk@zohomail.in");
        simpleMailSender.setReplyTo(email);
        simpleMailSender.setSubject(subject);
        simpleMailSender.setText(message);

        javaMailSender.send(simpleMailSender);
    }
}
