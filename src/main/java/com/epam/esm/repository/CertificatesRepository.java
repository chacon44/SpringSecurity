package com.epam.esm.repository;

import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificatesRepository extends JpaRepository<GiftCertificate, Long> ,
    JpaSpecificationExecutor<GiftCertificate> {

  Optional<GiftCertificate> findCertificateByName(String certificateName);

  String GET_TAGS_BY_CERTIFICATE_ID = "SELECT t.* FROM tag t INNER JOIN " +
      "gift_certificate_tag gct ON t.tag_id = gct.tag_id " +
      "WHERE gct.certificate_id = :giftCertificateId";

  @Query(value = GET_TAGS_BY_CERTIFICATE_ID, nativeQuery = true)
  List<Tag> findTagsByCertificateId(@Param("giftCertificateId") Long giftCertificateId);
}
