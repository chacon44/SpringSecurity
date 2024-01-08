package com.epam.esm.validators;

import com.epam.esm.model.GiftCertificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificateValidator {
    /**
     *
     * @param giftCertificate cannot be empty or null
     *                        can contain empty or null values
     * @return
     */
    public static Optional<String> validateRequest(GiftCertificate giftCertificate) {

        List<String> errors = new ArrayList<>();

        if (giftCertificate.getName() == null || giftCertificate.getName().isEmpty()) {
            errors.add("Name is required");
        }

        if (giftCertificate.getDescription() == null || giftCertificate.getDescription().isEmpty()) {
            errors.add("Description is required");
        }

        try {
            double price = Double.parseDouble(giftCertificate.getPrice().toString());
            if (Double.isNaN(price) || Double.isInfinite(price)) {
                errors.add("Price must be a finite number");
            } else if (price < 0) {
                errors.add("Price must be non-negative");
            }
        } catch (NumberFormatException e) {
            errors.add("Price must be a valid number");
        }

        try {
            long duration = Long.parseLong(giftCertificate.getDuration().toString());
            if (duration < 0) {
                errors.add("Duration must be non-negative");
            }
        } catch (NumberFormatException e) {
            errors.add("Duration must be a valid number");
        }

        if (errors.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(String.join(", ", errors));
        }
    }
}
