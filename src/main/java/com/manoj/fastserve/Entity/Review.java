package com.manoj.fastserve.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Min(1)
    @Max(5)
    private Integer rating;

    @Setter
    @NotBlank
    private String comment;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    public Review() {
    }

}