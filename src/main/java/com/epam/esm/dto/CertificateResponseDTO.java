package com.epam.esm.dto;

import java.util.List;

public record CertificateResponseDTO(Long certificateId, String name, String description, Double price, Long duration, List<Long> tagIds) {}

