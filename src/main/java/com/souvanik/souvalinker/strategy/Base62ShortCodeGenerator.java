package com.souvanik.souvalinker.strategy;

import com.souvanik.souvalinker.constants.AppConstants;
import org.springframework.stereotype.Component;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
public class Base62ShortCodeGenerator implements ShortCodeGenerator {

    @Override
    public String generate(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid id"
            );
        }

        String alphabet = AppConstants.BASE62_ALPHABET;

        StringBuilder shortCode = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % 62);
            shortCode.append(alphabet.charAt(remainder));

            id = id / 62;
        }

        return shortCode.reverse().toString();
    }
}