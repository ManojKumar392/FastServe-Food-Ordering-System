package com.manoj.fastserve.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotBlank(message = "Name is required")
    private String name;

    @Setter
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @Setter
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Setter
    @NotBlank(message = "Address is required")
    private String address;

    @Setter
    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {
    }

}