package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
            signupRequest.getEmail(),
            encodedPassword,
            userRole
        );
        User savedUser = userRepository.save(newUser);

        return new SignupResponse(createJwtToken(savedUser));
    }

    public SigninResponse signin(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
            () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        return new SigninResponse(createJwtToken(user));
    }

    private String createJwtToken(User user) {
        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
    }
}
