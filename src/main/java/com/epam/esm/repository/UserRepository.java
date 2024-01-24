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
  String ORDER_DESCENDANT = "GROUP BY o.user.id ORDER BY SUM(o.price) DESC";
  String SELECT_HIGHEST_VALUE = " LIMIT 1";
  String FILTER ="(" + SELECT_USER_IDS_FROM_ORDERS + ORDER_DESCENDANT + SELECT_HIGHEST_VALUE + ")";

  //sum of all orders
  String query = FETCH_ALL_USER_FIELDS + FETCH_ORDERS + GET_USER_WITH_APPLIED_FILTER + FILTER;

  @Query(value = query)
  Optional<User> findUserWithHighestOrdersCost();



  String getMostUsedTag = "  SELECT t.name, count(t.name) \n" + //select tag names and count them
                          "  FROM users u \n" + //from user u
                          "  JOIN orders o ON u.id = o.user_id \n" + //join orders on user
                          "  JOIN certificates c ON  o.certificate_id = c.id \n" + //join the certificates that match given id
                          "  JOIN certificate_tags ct ON c.id = ct.certificate_id \n" + //join certificate_tags where it matches given id
                          "  JOIN tag t ON ct.tag_id = t.id \n" + //join tags
                          "  WHERE u.id = :userId \n" + //filter that gets only user with provided id
                          "  GROUP BY t.name \n" + //group result by tag name
                          "  ORDER BY count DESC LIMIT 1\n" + //order by count, descendant order
                          "  "; //gets first element

  @Query(value = getMostUsedTag)
  Optional<String> getMostUsedTag();
}
