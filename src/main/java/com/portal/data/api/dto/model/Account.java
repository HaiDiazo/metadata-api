package com.portal.data.api.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portal-metadata-account")
public class Account {
    @Id
    private String id;
    private String name;
    private String username;
    private String password;
    private String role;
}
