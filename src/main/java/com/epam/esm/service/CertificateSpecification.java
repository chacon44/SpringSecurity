package com.epam.esm.service;
import com.epam.esm.model.GiftCertificate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

// A new CertificateSpecification class to create a dynamic query
public class CertificateSpecification implements Specification<GiftCertificate> {
  private final String tagName;
  private final String searchWord;
  public CertificateSpecification(String tagName, String searchWord) {
    this.tagName = tagName;
    this.searchWord = searchWord;
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<GiftCertificate> root,
      @NonNull CriteriaQuery<?> query,
      @NonNull CriteriaBuilder criteriaBuilder) {

    List<Predicate> predicates = new ArrayList<>();

    if (StringUtils.hasText(this.tagName)) {
      // Assuming that 'tags' is a collection in the `GiftCertificate` entity
      predicates.add(criteriaBuilder.equal(root.join("tags").get("name"), this.tagName));
    }

    if (StringUtils.hasText(this.searchWord)) {
      predicates.add(criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + this.searchWord.toLowerCase() + "%"),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + this.searchWord.toLowerCase() + "%")
      ));
    }

    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }
}
