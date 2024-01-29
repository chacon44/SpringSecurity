package com.epam.esm.service;

import static com.epam.esm.exceptions.ErrorCode.CERTIFICATE_BAD_REQUEST;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

import com.epam.esm.dto.CertificateResponseDTO;
import com.epam.esm.dto.errors.ErrorDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.filter.CertificateSpecification;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.TagRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final TagRepository tagRepository;

    @Autowired
    public CertificateService(CertificateRepository certificateRepository, TagRepository tagRepository) {
        this.certificateRepository = certificateRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Adds a new certificate in the database.
     *
     * @param giftCertificate The certificate to save. Must not be null.
     * @param tagIdsList      A list of the ids of tags to be associated with the certificate.
     * @return The saved certificate in the form of a CertificateResponseDTO.
     * @throws CustomizedException If certificate or any of the tagIds in tagIdsList are not valid,
     *                             certificate already exists,
     *                             or if there is a database error during saving.
     */
    public CertificateResponseDTO saveGiftCertificate(@NonNull GiftCertificate giftCertificate, List<Long> tagIdsList) {
        try {
            Optional<ErrorDTO> requestValidationMessage = validateCertificateRequest(giftCertificate, tagIdsList);
            if (requestValidationMessage.isPresent())
                throw new CustomizedException(requestValidationMessage.get().errorMessage(), CERTIFICATE_BAD_REQUEST);

            Optional<GiftCertificate> tryToFindCertificate = certificateRepository.findByName(giftCertificate.getName());

            if (tryToFindCertificate.isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(tagIdsList);
                giftCertificate.setTags(tags);
                GiftCertificate savedGiftCertificate = certificateRepository.save(giftCertificate);

                return convertToCertificateDTO(savedGiftCertificate);
            } else {
                GiftCertificate foundCertificate = tryToFindCertificate.get();
                Long idFound = foundCertificate.getId();

                throw new CustomizedException(CERTIFICATE_ALREADY_EXISTS.formatted(idFound), ErrorCode.CERTIFICATE_ALREADY_EXISTS);
            }
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error while saving GiftCertificate", ErrorCode.CERTIFICATE_DATABASE_ERROR, ex);
        }
    }

    /**
     * Retrieve GiftCertificate with the given id.
     *
     * @param giftCertificateId The id of the certificate to retrieve. Must not be null.
     * @return The certificate in the form of a CertificateResponseDTO.
     * @throws CustomizedException If giftCertificateId is null, does not exist, or if there is a database error during retrieval.
     */
    public CertificateResponseDTO getGiftCertificate(@NonNull Long giftCertificateId) {
        try {
            Optional<GiftCertificate> giftCertificate = certificateRepository.findById(giftCertificateId);

            if (giftCertificate.isPresent()) {
                return convertToCertificateDTO(giftCertificate.get());
            } else {
                String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId);
                throw new CustomizedException(message, ErrorCode.CERTIFICATE_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error while getting gift certificate with id: " + giftCertificateId,
                ErrorCode.CERTIFICATE_DATABASE_ERROR, ex);
        }
    }

    /**
     * Retrieves a page of certificates filtered by tagNames and a searchWord.
     *
     * @param tagNames   The tags that the certificates should have.
     * @param searchWord The word to filter certificates by.
     * @param pageable   The details of the page to retrieve.
     * @return A page of filtered certificates in the form of CertificateResponseDTOs.
     * @throws CustomizedException If there is a database error during retrieval.
     */
    public Page<CertificateResponseDTO> getFilteredCertificates(
        List<String> tagNames,
        String searchWord,
        Pageable pageable) {

        try {
            Specification<GiftCertificate> spec = new CertificateSpecification(tagNames, searchWord);
            Page<GiftCertificate> certificatesPage = certificateRepository.findAll(spec, pageable);
            return certificatesPage.map(this::convertToCertificateDTO);
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error while getting filtered certificates", ErrorCode.CERTIFICATE_DATABASE_ERROR, ex);
        }
    }

    /**
     * Deletes the certificate identified by certificateId.
     *
     * @param certificateId The id of the certificate to delete.
     * @throws CustomizedException If certificateId does not exist, or if there is a database error during deletion.
     */
    @Transactional
    public void deleteGiftCertificate(Long certificateId) {

        if(!certificateRepository.existsById(certificateId)){
            throw new CustomizedException(CERTIFICATE_WITH_ID_NOT_FOUND.formatted(certificateId), ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        try {
            certificateRepository.deleteById(certificateId);
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error during deleting certificate with id " + certificateId, ErrorCode.CERTIFICATE_DATABASE_ERROR, ex);
        }
    }
    /**
     * Updates the given fields for the certificate identified by certificateId.
     *
     * @param certificateId The id of the certificate to update. Must not be null.
     * @param updates       The certificate object that contains updated fields. Fields that are null will not be updated.
     * @param newTagIdsList A list of the ids of new tags to be associated with the certificate.
     * @return              The updated certificate in the form of a CertificateResponseDTO.
     *
     * @throws CustomizedException If certificateId is null, does not exist, or if there is a database error during update.
     */
    @Transactional
    public CertificateResponseDTO updateGiftCertificate(@NonNull Long certificateId, GiftCertificate updates, List<Long> newTagIdsList) {

        try {
            Optional<GiftCertificate> optCertificate = certificateRepository.findById(
                certificateId);

            if (optCertificate.isEmpty()) {
                throw new CustomizedException(
                    "Gift certificate not found with id: " + certificateId,
                    ErrorCode.CERTIFICATE_NOT_FOUND);
            }

            GiftCertificate existingCertificate = optCertificate.get();

            if (updates.getName() != null) {
                existingCertificate.setName(updates.getName());
            }
            if (updates.getDescription() != null) {
                existingCertificate.setDescription(updates.getDescription());
            }
            if (updates.getPrice() != null) {
                existingCertificate.setPrice(updates.getPrice());
            }
            if (updates.getDuration() != null) {
                existingCertificate.setDuration(updates.getDuration());
            }
            if (newTagIdsList != null) {
                List<Tag> uniqueTags = tagRepository.findAllById(newTagIdsList);
                existingCertificate.setTags(uniqueTags);
            }

            GiftCertificate updatedCertificate = certificateRepository.save(existingCertificate);

            return convertToCertificateDTO(updatedCertificate);
        } catch (DataAccessException ex){
            throw new CustomizedException("Database error during update certificate with id " + certificateId, ErrorCode.CERTIFICATE_DATABASE_ERROR, ex);

        }
    }

    private Optional<ErrorDTO> validateCertificateRequest(GiftCertificate giftCertificate, List<Long> tagIds) {

        Optional<String> validationMessage = validateRequest(giftCertificate);
        if (validationMessage.isPresent()) {
            ErrorDTO errorResponse = new ErrorDTO(validationMessage.get(), CERTIFICATE_BAD_REQUEST.getErrorCode());
            return Optional.of(errorResponse);
        }

        List<Tag> existingTags = tagRepository.findAllById(tagIds);

        if(existingTags.size() != tagIds.size()){
            ErrorDTO errorResponse = new ErrorDTO("non existing tags", CERTIFICATE_BAD_REQUEST.getErrorCode());
            return Optional.of(errorResponse);
        }

        return Optional.empty();
    }

    private Optional<String> validateRequest(GiftCertificate giftCertificate) {

        List<String> errors = new ArrayList<>();

        if(giftCertificate.getName() == null || giftCertificate.getName().isEmpty()) {
            errors.add("Name is required");
        }

        if (giftCertificate.getDescription() == null || giftCertificate.getDescription().isEmpty()) {
            errors.add("Description is required");
        }

        if (giftCertificate.getPrice() != null) {
            double price = giftCertificate.getPrice();
            if (isNaN(price) || isInfinite(price)) {
                errors.add("Price must be a finite number");
            } else if (price < 0) {
                errors.add("Price must be non-negative");
            }
        } else {
            errors.add("Price must be a valid number");
        }

        if (giftCertificate.getDuration() != null) {
            long duration = giftCertificate.getDuration();
            if (duration < 0) {
                errors.add("Duration must be non-negative");
            }
        } else {
            errors.add("Duration must be a valid number");
        }

        if (errors.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(String.join(", ", errors));
        }
    }

    public CertificateResponseDTO convertToCertificateDTO(GiftCertificate certificate) {

        List<Long> tagIds = certificate.getTags().stream()
            .map(Tag::getId)
            .collect(Collectors.toList());


        return new CertificateResponseDTO(certificate.getId(),
            certificate.getName(),
            certificate.getDescription(),
            certificate.getPrice(),
            certificate.getDuration(),
            tagIds);
    }
}

