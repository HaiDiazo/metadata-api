package com.portal.data.api.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequest {
    private String name;
    private String username;
    private String password;
    private String role;
}
