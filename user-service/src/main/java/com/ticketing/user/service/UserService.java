package com.ticketing.user.service;

import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.exception.ErrorCode;
import com.ticketing.user.domain.User;
import com.ticketing.user.dto.LoginRequest;
import com.ticketing.user.dto.SignupRequest;
import com.ticketing.user.dto.TokenResponse;
import com.ticketing.user.dto.UserResponse;
import com.ticketing.user.repository.UserRepository;
import com.ticketing.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getPhone()
        );
        return new UserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.createToken(
                user.getId(), user.getEmail(), user.getRole().name());

        return new TokenResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationMs() / 1000,
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserResponse(user);
    }
}
