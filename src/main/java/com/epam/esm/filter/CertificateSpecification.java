package com.epam.esm.filter;

import static org.springframework.util.StringUtils.hasText;

import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class CertificateSpecification implements Specification<GiftCertificate> {
  private final List<String> tagNames;
  private final String searchWord;

  public CertificateSpecification(List<String> tagNames, String searchWord) {
    this.tagNames = tagNames;
    this.searchWord = searchWord;
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<GiftCertificate> root,
      @NonNull CriteriaQuery<?> query,
      @NonNull CriteriaBuilder criteriaBuilder) {

    List<Predicate> predicates = new ArrayList<>();

    if (this.tagNames != null && !this.tagNames.isEmpty()) {
      List<Predicate> tagPredicates = new ArrayList<>();
      for (String singleTagName : tagNames) {
        Join<GiftCertificate, Tag> join = root.join("tags");
        tagPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(join.get("name")), "%" + singleTagName.toLowerCase().trim() + "%"));
      }
      predicates.add(criteriaBuilder.and(tagPredicates.toArray(new Predicate[0])));
    }

    if (hasText(this.searchWord)) {
      predicates.add(criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + this.searchWord.toLowerCase() + "%"),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + this.searchWord.toLowerCase() + "%")
      ));
    }

    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }
}
