package com.epam.esm.dto;

import java.util.List;

public record CertificateDTO(Long certificateId, String name, String description, Double price, Long duration, List<Long> tagIds) {}
