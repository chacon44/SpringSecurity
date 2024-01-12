package com.epam.esm.dto;

import java.util.List;

public record GiftCertificateRequestDTO(
        String name,
        String description,
        Double price,
        Long duration,
        List<Long> tagIds) {
}
