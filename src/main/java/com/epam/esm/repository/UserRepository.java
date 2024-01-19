package com.epam.esm.repository;

import com.epam.esm.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  String FETCH_ALL_USER_FIELDS = "SELECT u FROM User u ";
  String FETCH_ORDERS = "LEFT JOIN FETCH u.orders ";
  String GET_USER_WITH_APPLIED_FILTER = "WHERE u.id = ";

  String SELECT_USER_IDS_FROM_ORDERS = "SELECT o.user.id FROM Order o ";
  String ORDER_DESCENDANT = "ORDER BY o.price DESC ";
  String SELECT_HIGHEST_VALUE = "LIMIT 1";
  String FILTER ="(" + SELECT_USER_IDS_FROM_ORDERS + ORDER_DESCENDANT + SELECT_HIGHEST_VALUE + ")";

  //sum of all orders
  String query = FETCH_ALL_USER_FIELDS + FETCH_ORDERS + GET_USER_WITH_APPLIED_FILTER + FILTER;
  @Query(query)
  Optional<User> findUserWithHighestOrdersCost();
}
