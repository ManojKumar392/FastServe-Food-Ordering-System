package com.manoj.fastserve.Repository.spec;

import com.manoj.fastserve.Entity.MenuItem;
import org.springframework.data.jpa.domain.Specification;

public class MenuItemSpecification {

    public static Specification<MenuItem> hasName(String name) {

        return (root, query, cb) ->
                name == null ? null :
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"
                        );
    }

    public static Specification<MenuItem> minPrice(Double minPrice) {

        return (root, query, cb) ->
                minPrice == null ? null :
                        cb.greaterThanOrEqualTo(
                                root.get("price"),
                                minPrice
                        );
    }

    public static Specification<MenuItem> maxPrice(Double maxPrice) {

        return (root, query, cb) ->
                maxPrice == null ? null :
                        cb.lessThanOrEqualTo(
                                root.get("price"),
                                maxPrice
                        );
    }

    public static Specification<MenuItem> restaurantId(Long restaurantId) {

        return (root, query, cb) ->
                restaurantId == null ? null :
                        cb.equal(
                                root.get("restaurant").get("id"),
                                restaurantId
                        );
    }
}