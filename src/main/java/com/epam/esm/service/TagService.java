package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.NOT_VALID_TAG_REQUEST;
import static com.epam.esm.exceptions.Messages.TAG_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.TAG_CANNOT_BE_SAVED;
import static com.epam.esm.exceptions.Messages.TAG_ID_NOT_FOUND;

import com.epam.esm.dto.TagResponseDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.TagRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }


    public TagResponseDTO saveTag(String tagName) {

        if (tagName == null || tagName.isEmpty()) {
            throw new CustomizedException(NOT_VALID_TAG_REQUEST, ErrorCode.TAG_BAD_REQUEST);
        }

        tagRepository.findByName(tagName)
            .ifPresent(tag -> {
                throw new CustomizedException(TAG_ALREADY_EXISTS.formatted(tag.getId()), ErrorCode.TAG_BAD_REQUEST);
            });

        Tag tag = new Tag();
        tag.setName(tagName);

        try {
            Tag savedTag = tagRepository.save(tag);
            return convertTagToTagReturnDTO(savedTag);
        }catch (DataAccessException ex){
            throw new CustomizedException(TAG_CANNOT_BE_SAVED, ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }

    public Page<TagResponseDTO> getAllTags(Pageable pageable) {
        try {
            return tagRepository.findAll(pageable)
                .map(this::convertTagToTagReturnDTO);

        } catch (DataAccessException ex) {
            throw new CustomizedException("Failed to fetch tags from the database", ErrorCode.TAG_DATABASE_ERROR, ex);
        }
        catch (Exception ex) {
            throw new CustomizedException("Unexpected error occurred", ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }

    public TagResponseDTO getMostPopularTag(){
        try {
            Optional<Long> tagId = tagRepository.findMostUsedTagOfUserWithHighestTotalCostOfOrders();
            if(tagId.isPresent()) {
                Tag tag = tagRepository.getReferenceById(tagId.get());
                return convertTagToTagReturnDTO(tag);
            }
            else return null;
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error during getting tag", ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }


    public void deleteTag(long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new CustomizedException(TAG_ID_NOT_FOUND.formatted(tagId), ErrorCode.TAG_NOT_FOUND);
        }
        try {
            tagRepository.deleteById(tagId);
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error during deleting tag with id " + tagId, ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }

    private TagResponseDTO convertTagToTagReturnDTO(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getName());
    }
}
