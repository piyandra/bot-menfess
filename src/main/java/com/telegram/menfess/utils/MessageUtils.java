package com.telegram.menfess.utils;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

@Component
public class MessageUtils {

    private final Predicate<String> hasValidLength =
            msg -> msg.replaceAll("#\\w+", "").trim().split("\\s+").length >= 3;

    private final Predicate<String> hasHashtag =
            msg -> msg.contains("#");

    private final Predicate<String> isValidMessage =
            hasValidLength.and(hasHashtag);

    public Optional<String> process(String message) {
        return Optional.ofNullable(message)
                .filter(isValidMessage)
                .map(this::convertMessage);
    }

    private String convertMessage(String message) {
        return message;
    }
}
