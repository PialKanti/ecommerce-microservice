package com.example.ecommerce.commons.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiEndpoints {
    private static final String API_VERSION = "/api/v1";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Auth {
        public static final String BASE_AUTH = API_VERSION + "/auth";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class User {
        public static final String BASE_USERS = API_VERSION + "/users";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Admin {
        public static final String BASE_ADMIN             = API_VERSION + "/admin";
        public static final String BASE_ADMIN_ROLES       = BASE_ADMIN + "/roles";
        public static final String BASE_ADMIN_PERMISSIONS = BASE_ADMIN + "/permissions";
        public static final String BASE_ADMIN_USERS       = BASE_ADMIN + "/users";
        public static final String BASE_ADMIN_PRODUCTS    = BASE_ADMIN + "/products";
        public static final String BASE_ADMIN_CATEGORIES  = BASE_ADMIN + "/categories";
        public static final String BASE_ADMIN_INVENTORY   = BASE_ADMIN + "/inventory";
        public static final String BASE_ADMIN_ORDERS      = BASE_ADMIN + "/orders";
        public static final String BASE_ADMIN_PAYMENTS    = BASE_ADMIN + "/payments";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Product {
        public static final String BASE_PRODUCTS = API_VERSION + "/products";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Category {
        public static final String BASE_CATEGORIES = API_VERSION + "/categories";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Inventory {
        public static final String BASE_INVENTORY = API_VERSION + "/inventory";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cart {
        public static final String BASE_CART = API_VERSION + "/cart";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Order {
        public static final String BASE_ORDERS = API_VERSION + "/orders";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Payment {
        public static final String BASE_PAYMENTS        = API_VERSION + "/payments";
        public static final String BASE_PAYMENT_WEBHOOK = BASE_PAYMENTS + "/webhook";
    }
}
