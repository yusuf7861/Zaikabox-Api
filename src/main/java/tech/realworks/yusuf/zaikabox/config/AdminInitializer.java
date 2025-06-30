package tech.realworks.yusuf.zaikabox.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tech.realworks.yusuf.zaikabox.entity.Role;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;

import java.security.SecureRandom;

@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.existsByRole(Role.ADMIN);

        if (adminExists) {
            log.info("Root Admin found, Skipping the creation part of Root Admin.");
        }

        if (!adminExists) {
            log.info("Root Admin not found. Creating a new one");
            String password = generateRandomPassword(8);
            sendContactUsEmail(password);

            UserEntity rootAdmin = new UserEntity();
            rootAdmin.setEmail("yjamalk@zohomail.in");
            rootAdmin.setPassword(passwordEncoder.encode(password));
            rootAdmin.setRole(Role.ADMIN);
            userRepository.save(rootAdmin);
            log.info("Root Admin created successfully");
        }
    }

    public void sendContactUsEmail(String password) {
        SimpleMailMessage simpleMailSender = new SimpleMailMessage();
        simpleMailSender.setFrom("yjamal12feb@gmail.com");
        simpleMailSender.setTo("yjamalk@zohomail.in");
        simpleMailSender.setSubject("Zaikabox - Root Admin Created");
        String mailBody = "Project Zaikabox root admin has been created.\n\n"
                + "Email: yjamalk@zohomail.in\n"
                + "Temporary Password: " + password + "\n\n"
                + "Please log in and change your password immediately.";
        simpleMailSender.setText(mailBody);

        javaMailSender.send(simpleMailSender);
    }

    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
