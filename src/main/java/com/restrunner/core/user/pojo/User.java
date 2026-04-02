package com.restrunner.core.user.pojo;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private  String userID;
    private String email;
    private String token;
}
