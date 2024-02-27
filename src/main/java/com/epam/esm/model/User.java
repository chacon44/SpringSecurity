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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

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
@Entity
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
@Audited
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Setter
  @Column(name = "name")
  private String name;

  @Column(name = "create_date", updatable = false)
  private String createDate;

  @Column(name = "last_update_date")
  private String lastUpdateDate;

  @Setter
  @Column(name = "username")
  private String username;

  @Setter
  @Column(name = "email")
  private String email;

  @Setter
  //encrypted String using BCryptPasswordEncoder
  @Column(name = "password")
  private String password;

  @Column(name = "NotCryptedPassword")
  private String notCryptedPassword;

  @Setter
  @Column(name = "enable")
  private Boolean enable;

  @PrePersist
  protected void onCreate() {
    if (this.createDate == null) {
      this.createDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .format(LocalDateTime.now(ZoneOffset.UTC));
    }
    this.lastUpdateDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .format(LocalDateTime.now(ZoneOffset.UTC));
  }

  @PreUpdate
  protected void onUpdate() {
    this.lastUpdateDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .format(LocalDateTime.now(ZoneOffset.UTC));
  }

  @JsonManagedReference
  @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  private Set<Order> orders = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();
}
