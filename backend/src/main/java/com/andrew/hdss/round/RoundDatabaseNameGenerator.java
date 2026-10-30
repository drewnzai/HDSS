package com.andrew.hdss.round;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class RoundDatabaseNameGenerator {

    private final String prefix;

    public RoundDatabaseNameGenerator(
            @Value("${hdss.round.database-prefix:hdss_round}") String prefix) {

        if (prefix == null || !prefix.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid round database prefix");
        }

        this.prefix = prefix;
    }

    public String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8);

        return prefix + "_" + date + "_" + suffix;
    }
}