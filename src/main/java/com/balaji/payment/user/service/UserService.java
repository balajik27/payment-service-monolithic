
package com.balaji.payment.user.service;

import com.balaji.payment.user.dto.request.UserLoginRequest;
import com.balaji.payment.user.dto.request.UserRegisterRequest;
import com.balaji.payment.user.dto.response.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRegisterRequest request);

    UserResponse loginUser(UserLoginRequest request);

}
