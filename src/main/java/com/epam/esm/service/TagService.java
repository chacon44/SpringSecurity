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
import jakarta.transaction.Transactional;
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

    /**
     * Saves a tag with the provided name.
     *
     * @param tagName The name for the new tag. Should not be null or empty.
     * @return The saved tag as a TagResponseDTO.
     * @throws CustomizedException if tagName is null or empty, if tag already exists, or if there is a database error during saving.
     */
    @Transactional
    public TagResponseDTO saveTag(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            throw new CustomizedException(NOT_VALID_TAG_REQUEST, ErrorCode.TAG_BAD_REQUEST);
        }

        try {
        tagRepository.findByName(tagName)
            .ifPresent(tag -> {
                throw new CustomizedException(TAG_ALREADY_EXISTS.formatted(tag.getId()), ErrorCode.TAG_ALREADY_EXISTS);
            });

        Tag tag = new Tag();
        tag.setName(tagName);


            Tag savedTag = tagRepository.save(tag);
            return convertTagToTagReturnDTO(savedTag);
        }catch (DataAccessException ex){
            throw new CustomizedException(TAG_CANNOT_BE_SAVED, ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }

    /**
     * Retrieves the most used tag by user with highest cost of orders.
     *
     * @return The most used tag as a TagResponseDTO, or null if no tags exist.
     * @throws CustomizedException if there is a database error during fetch.
     */
    public TagResponseDTO getMostUsedTag(){
        try {
            Optional<Long> tagId = tagRepository.findMostUsedTagOfUserWithHighestTotalCostOfOrders();
            if(tagId.isPresent()) {
                Tag tag = tagRepository.getReferenceById(tagId.get());
                return convertTagToTagReturnDTO(tag);
            }
            else return null;
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error while fetching tag", ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }

    /**
     * Retrieves all tags, paged according to the provided Pageable.
     *
     * @param pageable Details for the paging of the results.
     * @return A page of tag results as TagResponseDTOs.
     * @throws CustomizedException if there is a database error during fetch.
     */
    public Page<TagResponseDTO> getAllTags(Pageable pageable) {
        try {
            return tagRepository.findAll(pageable)
                .map(this::convertTagToTagReturnDTO);

        } catch (DataAccessException ex) {
            throw new CustomizedException("Failed to fetch tags from the database", ErrorCode.TAG_DATABASE_ERROR, ex);
        }
    }

    /**
     * Deletes the tag with the provided id.
     *
     * @param tagId The id of the tag to delete.
     * @throws CustomizedException if tagId does not exist, or if there is a database error during deletion.
     */
    @Transactional
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
