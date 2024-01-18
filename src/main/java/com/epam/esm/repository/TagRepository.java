package com.epam.esm.repository;

import com.epam.esm.model.Tag;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long>{
  Optional<Tag> findByName(String name);
  @NonNull
  Page<Tag> findAll(@NonNull Pageable pageable);
  List<Tag> findAllByIdIn(List<Long> ids);
}
