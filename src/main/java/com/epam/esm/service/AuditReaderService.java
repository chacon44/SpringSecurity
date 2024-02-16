package com.epam.esm.service;

import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditReaderService {

  @Autowired
  private EntityManager entityManager;

  public AuditReader getReader() {
    return AuditReaderFactory.get(entityManager);
  }

}
