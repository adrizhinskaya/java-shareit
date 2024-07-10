package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ColoredCRUDLogger {
    private static final String RESET = "\u001B[0m";
    private static final String POST_COLOR = "\u001B[33m";
    private static final String PATCH_COLOR = "\u001B[35m";
    private static final String GET_COLOR = "\u001B[32m";
    private static final String DELETE_COLOR = "\u001b[31m";

    public static void logPost(String url, String message) {
        log.info(POST_COLOR + "POST {}: {}" + RESET, url, message);
    }

    public static void logPatch(String url, String message) {
        log.info(PATCH_COLOR + "PATCH {}: {}" + RESET, url, message);
    }

    public static void logGet(String url, String message) {
        log.info(GET_COLOR + "GET {}: {}" + RESET, url, message);
    }

    public static void logDelete(String url, String message) {
        log.info(DELETE_COLOR + "DELETE {}: {}" + RESET, url, message);
    }

    public static void logPostComplete(String url, String message) {
        log.info(POST_COLOR + "completion POST {}: {}" + RESET, url, message);
    }

    public static void logPatchComplete(String url, String message) {
        log.info(PATCH_COLOR + "completion PATCH {}: {}" + RESET, url, message);
    }

    public static void logGetComplete(String url, String message) {
        log.info(GET_COLOR + "completion GET {}: {}" + RESET, url, message);
    }

    public static void logDeleteComplete(String url, String message) {
        log.info(DELETE_COLOR + "completion DELETE {}: {}" + RESET, url, message);
    }
}
