package com.eshop.userbackend.dto.user;

import lombok.*;

import java.util.Date;
@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class UserDTO {
    private long id;
    private String lastname;
    private String firstname;
    private String email;
    private String phone;
    private String image;
    private Date created_at;
}



