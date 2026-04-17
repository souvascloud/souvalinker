package com.souvanik.souvalinker.config;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public final class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private DataSourceContextHolder() {
    }

    public static void setPrimary() {
        CONTEXT.set("PRIMARY");
    }


    public static void setReplica() {
        CONTEXT.set("REPLICA");
    }

    public static String getCurrentDataSource() {
        return CONTEXT.get();
    }


    public static void clear() {
        CONTEXT.remove();
    }
}