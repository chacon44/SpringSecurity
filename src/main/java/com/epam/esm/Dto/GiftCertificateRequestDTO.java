package com.epam.esm.Dto;

import java.util.List;

public record GiftCertificateRequestDTO(
        String name,
        String description,
        Double price,
        Long duration,
        List<Long> tagIds) {
}
