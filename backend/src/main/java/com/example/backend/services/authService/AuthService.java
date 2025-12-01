package com.example.backend.services.authService;

import com.example.backend.dtos.authDTO.LoginDTO;
import com.example.backend.dtos.authDTO.UserDTO;
import com.example.backend.entities.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.InternalServerErrorException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtAuthService;

    public AuthService(PasswordEncoder encoder, UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtAuthService) {
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.userMapper = new UserMapper();
        this.authenticationManager = authenticationManager;
        this.jwtAuthService = jwtAuthService;
    }

    public void SignUp(UserDTO newuser) {
        Optional<User> existingUser = userRepository.findByUsername(newuser.getUsername());

        if (existingUser.isPresent()) {
            throw new BadRequestException("Username is already exists");
        }

        try {
            newuser.setPassword(encoder.encode(newuser.getPassword()));
            User user = userMapper.signupToUser(newuser);
            userRepository.save(user);

        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred", e);
        }
    }

    public String Login(LoginDTO loginDTO) {

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User does not exist"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtAuthService.generateAuthToken(user);

        return token;
    }

}
