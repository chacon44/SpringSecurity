package com.epam.esm.repository;

import com.epam.esm.model.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<Tag> findTagByName(String name);
  List<Tag> findAllTagByTagIdList(List<Long> ids);
}
