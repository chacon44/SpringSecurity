package com.epam.esm.dto;

import java.time.LocalDateTime;

public record OrderDTO(Long orderId, UserDTO user, CertificateDTO certificate, Double purchaseCost, LocalDateTime purchaseDate) {}

