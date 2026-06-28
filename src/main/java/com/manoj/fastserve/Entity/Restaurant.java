package com.manoj.fastserve.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(
        indexes = {
                @Index(
                        name = "idx_restaurant_name",
                        columnList = "name"
                ),
                @Index(
                        name = "idx_restaurant_location",
                        columnList = "location"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Restaurant location is required")
    private String location;

    @OneToMany(mappedBy = "restaurant")
    @JsonManagedReference
    private List<MenuItem> menuItems;
}

