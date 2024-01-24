package com.epam.esm.dto;

import java.time.LocalDateTime;

public record OrderResponseDTO(Long orderId, UserDTO user, CertificateDTO certificate, Double purchaseCost, LocalDateTime purchaseDate) {}

