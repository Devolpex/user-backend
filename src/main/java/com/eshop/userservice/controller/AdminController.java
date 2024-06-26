package com.eshop.userservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.eshop.userservice.enums.Role;
import com.eshop.userservice.model.Admin;
import com.eshop.userservice.model.User;
import com.eshop.userservice.request.admin.AdminCreateRequest;
import com.eshop.userservice.request.admin.AdminUpdateRequest;
import com.eshop.userservice.response.admin.AdminCreateResponse;
import com.eshop.userservice.response.admin.AdminDeleteResponse;
import com.eshop.userservice.response.admin.AdminPageResponse;
import com.eshop.userservice.response.admin.AdminUpdateResponse;
import com.eshop.userservice.service.AdminService;
import com.eshop.userservice.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/users-api/admins")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    private final UserService userService;
    private final AdminService adminService;
    // private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AdminCreateResponse> postAdmin(@RequestBody @Valid AdminCreateRequest request,
            BindingResult bindingResult) {

        List<String> errors = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            errors = bindingResult.getAllErrors().stream().map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
        }
        if (userService.emailExists(request.getEmail())) {
            errors.add("Email already exists");
        }
        if (!userService.confirmPassword(request.getPassword(), request.getConfirm_password())) {
            errors.add("Password confirmation does not match");
        }
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AdminCreateResponse.builder().errors(errors).build());
        }
        User user = User.builder()
            .firstname(userService.firstLetterToUpperCase(request.getFirstname()))
            .lastname(userService.firstLetterToUpperCase(request.getLastname()))
            .email(request.getEmail().toUpperCase())
            .phone(request.getPhone())
            .password(userService.bcryptPassword(request.getPassword()))
            .role(Role.ADMIN)
            .created_at(new Date())
            .build();

        adminService.saveAdmin(user);

        return ResponseEntity.ok(AdminCreateResponse.builder()
                .success("Admin created successfully")
                .redirectTo("/admins")
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Admin admin = adminService.findAdminById(id);
        return ResponseEntity.ok(admin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUpdateResponse> putAdmin(@PathVariable Long id,
            @RequestBody @Valid AdminUpdateRequest request, BindingResult bindingResult) {
        List<String> errors = new ArrayList<>();

        Admin admin = adminService.findAdminById(id);
        if (admin == null) {
            // errors.add("Admin not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AdminUpdateResponse.builder()
                    .errors(Collections.singletonList("Admin not found")).build());
        } else {
            if (bindingResult.hasErrors()) {
                errors = bindingResult.getAllErrors().stream().map(error -> error.getDefaultMessage())
                        .collect(Collectors.toList());
            }
            if (request.getPassword() != null) {
                if (!userService.confirmPassword(request.getPassword(), request.getConfirm_password())) {
                    errors.add("Password confirmation does not match");
                } else {
                    request.setPassword(userService.bcryptPassword(request.getPassword()));
                }
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AdminUpdateResponse.builder().errors(errors).build());
            }

            User user = admin.getUser();
            user.setFirstname(userService.firstLetterToUpperCase(request.getFirstname()));
            user.setLastname(userService.firstLetterToUpperCase(request.getLastname()));
            user.setEmail(request.getEmail().toLowerCase());
            user.setPhone(request.getPhone());
            if (request.getPassword() != null) {
                user.setPassword(request.getPassword());
            }
            admin.setUser(user);
            adminService.updateAdmin(id, admin);
        }
        return ResponseEntity.ok(AdminUpdateResponse.builder()
        .success("Admin updated successfully")
        .redirectTo("/admins")
        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AdminDeleteResponse> deleteAdmin(@PathVariable Long id) {
        Admin admin = adminService.findAdminById(id);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AdminDeleteResponse.builder()
                    .errors(Collections.singletonList("Admin not found")).build());
        }
        if (adminService.deleteAdmin(id) == false) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AdminDeleteResponse.builder()
                    .errors(Collections.singletonList("Failed to delete admin")).build());
        }
        return ResponseEntity.ok(AdminDeleteResponse.builder()
                .success("Admin deleted successfully")
                .redirectTo("/admins")
                .build());
    }

    @GetMapping
    public ResponseEntity<AdminPageResponse> getAdminsByPagination(@RequestParam(defaultValue = "1") int page){
        int size = 5;
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Admin> adminPage = adminService.getAdminsByPagination(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(AdminPageResponse.builder()
            .admins(adminPage.getContent())
            .currentPage(adminPage.getNumber() + 1)
            .totalPages(adminPage.getTotalPages())
            .build());
    }

}
