package com.epam.esm.repository;

import com.epam.esm.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = (" +
      "SELECT o.user.id FROM Order o GROUP BY o.user.id ORDER BY SUM(o.price) DESC LIMIT 1)")
  Optional<User> findUserWithHighestOrdersCost();
}
