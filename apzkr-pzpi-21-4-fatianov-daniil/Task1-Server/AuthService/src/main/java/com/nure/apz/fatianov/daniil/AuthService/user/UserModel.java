package com.nure.apz.fatianov.daniil.AuthService.user;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    private Integer id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private ZonedDateTime birthday;
    private String gender;
    private ZonedDateTime creationDate;

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", birthday=" + birthday +
                ", gender='" + gender + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }

    public static UserModel toModel(User user) {
        return new UserModel(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getBirthday(),
                user.getGender(),
                user.getCreationDate()
        );
    }
}
