package com.epam.esm.repository;

import com.epam.esm.model.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByName(String name);

  @Query("SELECT t FROM Tag t JOIN FETCH t.giftCertificates WHERE t.name = :name")
  Optional<Tag> findWithNameAndFetchCertificatesEagerly(@Param("name") String name);
}
