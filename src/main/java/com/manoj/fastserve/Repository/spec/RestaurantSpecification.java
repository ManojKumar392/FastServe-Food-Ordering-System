package com.manoj.fastserve.Repository.spec;

import com.manoj.fastserve.Entity.Restaurant;
import org.springframework.data.jpa.domain.Specification;

public class RestaurantSpecification {

    public static Specification<Restaurant> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    public static Specification<Restaurant> hasLocation(String location) {
        return (root, query, cb) ->
                location == null ? null :
                        cb.like(cb.lower(root.get("location")),
                                "%" + location.toLowerCase() + "%");
    }

    public static Specification<Restaurant> notDeleted() {

        return (root, query, cb) ->
                cb.isFalse(root.get("isDeleted"));
    }
}