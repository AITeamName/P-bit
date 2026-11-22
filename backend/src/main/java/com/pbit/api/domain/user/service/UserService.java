package com.pbit.api.domain.user.service;

import com.pbit.api.domain.user.dto.response.UserMeResponse;
import com.pbit.api.domain.user.entity.User;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserMeResponse getUserMe(UUID userId) {

        User user = userRepository.findById(userId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(()-> new BusinessException(ErrorCode.TOKEN_INVALID));
        return UserMeResponse.from(user);
    }
}
