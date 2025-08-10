package com.portal.data.api.controller;

import com.portal.data.api.dto.model.Account;
import com.portal.data.api.dto.requests.AccountRequest;
import com.portal.data.api.dto.response.AccountResponse;
import com.portal.data.api.dto.response.ResponseApi;
import com.portal.data.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@Tag(name = "User Management")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/get-all")
    @Operation(summary = "Get all account")
    public ResponseEntity<?> getAccountAll() {
        long start = System.currentTimeMillis();

        List<AccountResponse> results = accountService.getAllAccounts();
        ResponseApi<List<AccountResponse>> response = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.OK.name(),
                results
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @Operation(summary = "Create Account")
    public ResponseEntity<?> createAccount(
        @RequestBody @Valid AccountRequest account
    ) {
        long start = System.currentTimeMillis();

        Account results = accountService.createAccount(account);
        ResponseApi<Account> response = new ResponseApi<>(
                HttpStatus.CREATED.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.CREATED.name(),
                results
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update Account")
    public ResponseEntity<?> updateAccount(
        @PathVariable String id,
        @RequestBody @Valid AccountRequest accountRequest
    ) {
        long start = System.currentTimeMillis();

        Account result = accountService.updateAccount(accountRequest, id);

        ResponseApi<Account> response = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.OK.name(),
                result
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Account")
    public ResponseEntity<?> deleteAccount(
        @PathVariable String id
    ) {
        long start = System.currentTimeMillis();

        String username = accountService.deleteAccount(id);

        ResponseApi<String> response = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.OK.name(),
                "deleted account {" + id + "} with username: " + username
        );
        return ResponseEntity.ok(response);
    }
}
