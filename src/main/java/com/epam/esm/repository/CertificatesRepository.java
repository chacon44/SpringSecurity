package com.epam.esm.repository;

import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificatesRepository extends JpaRepository<GiftCertificate, Long> {

  Optional<GiftCertificate> findCertificateByName(String certificateName);
  List<GiftCertificate> findByNameContainsOrDescriptionContains(String name, String description);

  @Query(value = "SELECT gc FROM GiftCertificate gc JOIN gc.tags t WHERE t.name = :tagName")
  List<GiftCertificate> findByTagName(@Param("tagName") String tagName);

  Optional<GiftCertificate> findByNameAndDescriptionAndPriceAndDuration(String name, String description, Double price, Long duration);
  @Query("SELECT g.tags FROM GiftCertificate g WHERE g.id = :giftCertificateId")
  List<Tag> findTagsByCertificateId(@Param("giftCertificateId") Long giftCertificateId);
}
