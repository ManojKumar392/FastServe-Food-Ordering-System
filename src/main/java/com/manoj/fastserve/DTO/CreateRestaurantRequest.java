package com.manoj.fastserve.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateRestaurantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String location;

}