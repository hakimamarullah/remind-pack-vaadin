package com.starline.base.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Formatter {

    public static String DD_MMM_YYYY = "dd MMM yyyy";

    private Formatter() {
    }

    public static String formatDate(LocalDate date, String pattern) {
        return Optional.ofNullable(date)
                .map(d -> d.format(DateTimeFormatter.ofPattern(pattern)))
                .orElse("");
    }
}
