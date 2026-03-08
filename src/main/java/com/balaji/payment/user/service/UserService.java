
package com.balaji.payment.user.service;

import java.util.UUID;

import com.balaji.payment.user.dto.request.UserLoginRequest;
import com.balaji.payment.user.dto.request.UserRegisterRequest;
import com.balaji.payment.user.dto.response.UserLoginResponse;
import com.balaji.payment.user.dto.response.UserRegisterResponse;
import com.balaji.payment.user.entity.UserEntity;

public interface UserService {

    UserRegisterResponse registerUser(UserRegisterRequest request);

    UserLoginResponse loginUser(UserLoginRequest request);

    UserEntity fetchUserById(UUID userId);

}
