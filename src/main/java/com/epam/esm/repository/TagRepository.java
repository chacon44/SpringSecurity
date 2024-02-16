package com.epam.esm.repository;

import com.epam.esm.model.Tag;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, Long>{
  Optional<Tag> findByName(String name);
  @NonNull
  Page<Tag> findAll(@NonNull Pageable pageable);

  String SELECT_TAG_IDS = "SELECT ct.tag_id FROM certificates cert";
  //fetches id of tags associated to each certificate

  String JOIN_CERTIFICATES_WITH_TAGS = "JOIN gift_certificate_tag ct ON cert.certificate_id = ct.certificate_id";
  //performs inner join with certificates_tag table to get a list of tags for each certificate

  String CERTIFICATE_CONDITION = "WHERE cert.certificate_id IN ";
  //filter previous list by the next condition

  String ORDER_ID_SUBQUERY = "(SELECT o.certificate_id FROM orders o WHERE o.id IN ";
  //get certificate ids from the orders that meet the next condition

  String ORDER_IDS = "(SELECT ord.id FROM orders ord";
  //get every order ids from orders list

  String GROUP_BY_USER_ORDER = "GROUP BY ord.user_id,ord.id ORDER BY SUM(ord.price) DESC))";
  //group them by user and order, ordering them by the sum of the purchasing costs in descendant order

  String GROUP_AND_SORT_BY_TAG_COUNT = "GROUP BY ct.tag_id ORDER BY COUNT(ct.tag_id) DESC LIMIT 1";
  //now I have a list of tags after applying the filter to certificates. Then, I group them by tag_id
  //order them in descendant order and get the first element with limit 1. That returns a tag id

  String FINAL_QUERY = SELECT_TAG_IDS + " " + JOIN_CERTIFICATES_WITH_TAGS + " " +
      CERTIFICATE_CONDITION + ORDER_ID_SUBQUERY + ORDER_IDS + " " +
      GROUP_BY_USER_ORDER + " " + GROUP_AND_SORT_BY_TAG_COUNT;

  @Query(value= FINAL_QUERY, nativeQuery = true)
  Optional<Long> findMostUsedTagOfUserWithHighestTotalCostOfOrders();
}
