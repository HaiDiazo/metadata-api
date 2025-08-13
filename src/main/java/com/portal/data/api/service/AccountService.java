package com.portal.data.api.service;

import com.portal.data.api.dto.model.Account;
import com.portal.data.api.dto.requests.AccountRequest;
import com.portal.data.api.dto.response.AccountResponse;
import com.portal.data.api.repository.MongoAccountRepository;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final MongoAccountRepository mongoAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AccountService(MongoAccountRepository mongoAccountRepository) {
        this.mongoAccountRepository = mongoAccountRepository;
    }

    public Optional<Account> findByUsername(String username) {
        return mongoAccountRepository.findByUsername(username);
    }

    public List<AccountResponse> getAllAccounts() {
        return mongoAccountRepository.findAll().stream()
                .map(map -> new AccountResponse(
                        map.getId(),
                        map.getName(),
                        map.getUsername(),
                        map.getRole()
                ))
                .toList();
    }

    public Account createAccount(AccountRequest accountRequest) {
        if (mongoAccountRepository.existsByUsername(accountRequest.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        Account account = new Account();
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        ObjectId objectId = new ObjectId(id);
        account.setId(objectId.toHexString());
        account.setName(accountRequest.getName());
        account.setUsername(accountRequest.getUsername());
        account.setPassword(passwordEncoder.encode(accountRequest.getPassword()));
        account.setRole(accountRequest.getRole());

        return mongoAccountRepository.insert(account);
    }

    public Account updateAccount(AccountRequest accountRequest, String id) {

        Account account = mongoAccountRepository.findById(id).orElseThrow();

        account.setRole(accountRequest.getRole());
        account.setUsername(accountRequest.getUsername());
        account.setName(accountRequest.getName());
        account.setPassword(passwordEncoder.encode(accountRequest.getPassword()));

        return mongoAccountRepository.save(account);
    }

    public String deleteAccount(String id) {

        Account account = mongoAccountRepository.findById(id).orElseThrow();
        mongoAccountRepository.deleteById(id);
        return account.getUsername();
    }
}
