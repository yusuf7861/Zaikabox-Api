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
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("Root Admin found, Skipping the creation part of Root Admin.");
            return;
        }

        log.info("Root Admin not found. Creating a new one");
        String password = generateStrongPassword();

        UserEntity rootAdmin = new UserEntity();
        rootAdmin.setEmail("yjamalk@zohomail.in");
        rootAdmin.setPassword(passwordEncoder.encode(password));
        rootAdmin.setRole(Role.ADMIN);

        userRepository.save(rootAdmin);

        try {
            sendContactUsEmail(password);
        } catch (Exception e) {
            log.error("Failed to send email notification for Root Admin creation", e);
        }

        log.info("Root Admin created successfully");
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

    public static String generateStrongPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@$!%*?&";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        String all = upper + lower + digits + special;
        for (int i = 4; i < 10; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // shuffle
        return password.chars()
                .mapToObj(c -> (char) c)
                .sorted((a, b) -> random.nextInt(3) - 1)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

}
