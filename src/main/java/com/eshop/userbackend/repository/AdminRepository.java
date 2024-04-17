package com.eshop.userbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eshop.userbackend.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long>{
    Page<Admin> findAll(Pageable pageable);


}
