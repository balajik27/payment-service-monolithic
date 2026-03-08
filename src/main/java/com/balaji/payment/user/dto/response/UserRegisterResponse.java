package com.balaji.payment.user.dto.response;

import com.balaji.payment.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponse {

    private UUID id;
    private String name;
    private String email;
    private UserStatus status;

}
