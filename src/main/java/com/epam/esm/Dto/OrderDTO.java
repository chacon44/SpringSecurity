package com.epam.esm.Dto;

import java.time.LocalDateTime;

public record OrderDTO(Long orderId, UserDTO user, CertificateDTO certificate, Double price, LocalDateTime purchaseDate) {}

