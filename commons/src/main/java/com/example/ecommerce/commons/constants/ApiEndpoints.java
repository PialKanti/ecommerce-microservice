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
    public static class Admin {
        public static final String BASE_ADMIN = API_VERSION + "/admin";
        public static final String BASE_ADMIN_ROLES = BASE_ADMIN + "/roles";
        public static final String BASE_ADMIN_PERMISSIONS = BASE_ADMIN + "/permissions";
        public static final String BASE_ADMIN_USERS = BASE_ADMIN + "/users";
    }
}
