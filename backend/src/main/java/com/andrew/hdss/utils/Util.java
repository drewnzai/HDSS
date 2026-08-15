package com.andrew.hdss.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.SecureRandom;

public class Util {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    public static Pageable getPageable(PaginationRequest paginationRequest) {
        return PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                paginationRequest.getDirection(),
                paginationRequest.getSortField()
        );
    }
}
