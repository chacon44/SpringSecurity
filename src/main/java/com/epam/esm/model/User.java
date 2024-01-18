package com.epam.esm.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the user entity class mapped to "users" table in the database.
 * This entity is part of a one-to-many relationship with the Order entity.
 * Contains ID, name and a list of orders placed by the user.
 * ID is autogenerated and serves as the primary key.
 * Name is not nullable, can be set externally.
 * Use a builder pattern for object creation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
@Table(name = "users")
public class User {

  /**
   * Represents a unique identifier of the User.
   * It is autogenerated and set by the database.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  /**
   * Represents the user's name.
   * This field is not nullable, means every user has to have a name.
   * It can be modified externally.
   */
  @Setter
  @Column(name = "name")
  private String name;


  /**
   * Set of orders that has been placed by the user.
   * The relationship to Order is marked as one-to-many.
   * Fetch type is LAZY, which means the orders are fetched on-demand.
   * All persist operations like save() or update() will be cascaded to each order in the set.
   */
  @JsonManagedReference

  /**
   * fetch type is lazy. That way, I can fetch (retrieve data) of user of id 1, and then after selecting
   * this user 1, then I fetch orders only if explictly do it, like saying user.getorders().
   * If fetch type were eager, it would retrieve all orders of that user
   * at the moment that user is fetched. So, in case there are thousands of orders, I would fetch thousands
   * of orders for nothing, when I only need 1 or 2.
   */
  @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  private Set<Order> orders = new HashSet<>();
}
