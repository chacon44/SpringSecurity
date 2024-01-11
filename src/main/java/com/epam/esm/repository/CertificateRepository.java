package com.epam.esm.repository;

import com.epam.esm.model.GiftCertificate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<GiftCertificate, Long> ,
    JpaSpecificationExecutor<GiftCertificate> {

  Optional<GiftCertificate> findByName(String certificateName);
}
