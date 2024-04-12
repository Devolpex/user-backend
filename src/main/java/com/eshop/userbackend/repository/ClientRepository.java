package com.eshop.userbackend.repository;

import com.eshop.userbackend.enums.Auth;
import com.eshop.userbackend.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client,Long> {

    @Query("SELECT c.auth FROM Client c WHERE c.user.email = :email")
    Auth findAuthByEmail(@Param("email")String email);
    Client findByUserEmail(String email);
}
