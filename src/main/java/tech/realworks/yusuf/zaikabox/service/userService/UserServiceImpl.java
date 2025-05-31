package tech.realworks.yusuf.zaikabox.service.userService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.io.user.UserRequest;
import tech.realworks.yusuf.zaikabox.io.user.UserResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;
    private final CartRepository cartRepository;

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        UserEntity newUser = convertToEntity(userRequest);
        newUser = userRepository.save(newUser);
        return convertToResponse(newUser);
    }

    @Override
    public String findByUserId() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository
                .findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + loggedInUserEmail + " not found"));
        return loggedInUser.getId();
    }

    @Override
    public UserResponse getUserProfile() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository
                .findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + loggedInUserEmail + " not found"));
        return convertToResponse(loggedInUser);
    }

    @Override
    public void deleteUser() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository
                .findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + loggedInUserEmail + " not found"));

        // Delete the user's cart first
        cartRepository.deleteByUserId(loggedInUser.getId());

        // Then delete the user
        userRepository.delete(loggedInUser);
    }

    private UserEntity convertToEntity(UserRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();
    }

    private UserResponse convertToResponse(UserEntity registeredUser) {
        return UserResponse.builder()
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .email(registeredUser.getEmail())
                .build();
    }
}
