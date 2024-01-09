package com.epam.esm.service;

import static com.epam.esm.exceptions.Codes.TAG_BAD_REQUEST;
import static com.epam.esm.exceptions.Codes.TAG_NOT_FOUND;
import static com.epam.esm.exceptions.Messages.TAG_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.TAG_ID_NOT_FOUND;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.TagRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }


    public ResponseEntity<?> saveTag(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Tag name is required", TAG_BAD_REQUEST));
        }

        Optional<Tag> possibleTag = tagRepository.findByName(tagName);
        if (possibleTag.isPresent()) {
            String message = TAG_ALREADY_EXISTS.formatted(possibleTag.get().getId());
            return ResponseEntity.badRequest().body(new ErrorDTO(message, TAG_BAD_REQUEST));
        }

        Tag tag = new Tag();
        tag.setName(tagName);
        Tag savedTag = tagRepository.save(tag);
        return ResponseEntity.status(CREATED).body(savedTag);
    }

    /**
     *
     * @param tagId unique tag id
     * @return if tag is retrieved, returns tag
     * if not, returns not found
     */
    public ResponseEntity<?> getTag(long tagId) {
        if (tagRepository.existsById(tagId)) {
            Optional<Tag> tag = tagRepository.findById(tagId);
            return ResponseEntity.status(FOUND).body(tag);
        } else {
            String message = TAG_ID_NOT_FOUND.formatted(tagId);

            ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     *
     * @param tagId unique tag id
     * @return
     * if the tag has been deleted, returns found
     * if not, returns not found
     */
    public ResponseEntity<?> deleteTag(long tagId) {

        if (tagRepository.existsById(tagId)) {
            tagRepository.deleteById(tagId);
            return ResponseEntity.status(FOUND).body(null);
        }

        String message = TAG_ID_NOT_FOUND.formatted(tagId);
        ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
    }
}
