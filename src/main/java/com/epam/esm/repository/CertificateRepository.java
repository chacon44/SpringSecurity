package com.epam.esm.repository;

import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<GiftCertificate, Long> {
  @NonNull
  Page<GiftCertificate> findAll(@NonNull Specification<GiftCertificate> spec,@NonNull Pageable pageable);
  Optional<GiftCertificate> findByName(String certificateName);
  List<GiftCertificate> findAllByTagsContaining(Tag tag);

}
