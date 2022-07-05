package com.example.dynamicroutingdatasource.model;

public class RoutingDatabaseContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String db) {
        CONTEXT.set(db);
    }

    public static String getClientDatabase() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
