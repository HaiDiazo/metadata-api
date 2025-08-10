package com.portal.data.api.repository;

import com.portal.data.api.dto.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoAccountRepository extends MongoRepository<Account, String> {

    boolean existsByUsername(String username);
}
