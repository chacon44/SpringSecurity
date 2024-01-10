package com.epam.esm.exceptions;

public class CertificateNotFoundException extends RuntimeException {
  public CertificateNotFoundException(Long id) {
    super("GiftCertificate with ID " + id + " not found.");
  }
}
