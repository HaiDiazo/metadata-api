package com.portal.data.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
@AllArgsConstructor
public class AccountResponse {
    private String id;
    private String name;
    private String username;
    private String role;
}
