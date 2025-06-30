package tech.realworks.yusuf.zaikabox.service.userservice;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.Role;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        // If role is null, default to CUSTOMER role
        Role role = user.getRole() != null ? user.getRole() : Role.CUSTOMER;

        return new User(user.getEmail(), user.getPassword(), role.getAuthorities());
    }
}
