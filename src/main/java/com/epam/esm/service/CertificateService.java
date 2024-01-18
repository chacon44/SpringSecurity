package com.epam.esm.service;

import static com.epam.esm.exceptions.ErrorCode.CERTIFICATE_BAD_REQUEST;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

import com.epam.esm.dto.CertificateReturnDTO;
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

    public CertificateReturnDTO saveGiftCertificate(@NonNull GiftCertificate giftCertificate, List<Long> tagIdsList) {
        try {
            Optional<ErrorDTO> requestValidationMessage = validateCertificateRequest(giftCertificate, tagIdsList);
            if (requestValidationMessage.isPresent())
                throw new CustomizedException(requestValidationMessage.get().errorMessage(), CERTIFICATE_BAD_REQUEST);

            Optional<GiftCertificate> tryToFindCertificate = certificateRepository.findByName(giftCertificate.getName());

            if (tryToFindCertificate.isEmpty()) {
                List<Tag> tags = tagRepository.findAllByIdIn(tagIdsList);
                giftCertificate.setTags(tags);
                GiftCertificate savedGiftCertificate = certificateRepository.save(giftCertificate);

                return convertToCertificateDTO(savedGiftCertificate);
            } else {
                GiftCertificate foundCertificate = tryToFindCertificate.get();
                Long idFound = foundCertificate.getId();

                throw new CustomizedException(CERTIFICATE_ALREADY_EXISTS.formatted(idFound), ErrorCode.CERTIFICATE_ALREADY_FOUND);
            }
        } catch (DataAccessException ex) {
            throw new CustomizedException("Database error while saving GiftCertificate", ErrorCode.CERTIFICATE_DATABASE_ERROR, ex);
        }
    }

    public CertificateReturnDTO getGiftCertificate(@NonNull Long giftCertificateId) {
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

    public Page<CertificateReturnDTO> getFilteredCertificates(
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
    @Transactional
    public CertificateReturnDTO updateGiftCertificate(@NonNull Long certificateId, GiftCertificate updates, List<Long> newTagIdsList) {

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
                List<Tag> uniqueTags = tagRepository.findAllByIdIn(newTagIdsList);
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

    public CertificateReturnDTO convertToCertificateDTO(GiftCertificate certificate) {

        List<Long> tagIds = certificate.getTags().stream()
            .map(Tag::getId)
            .collect(Collectors.toList());


        return new CertificateReturnDTO(certificate.getId(),
            certificate.getName(),
            certificate.getDescription(),
            certificate.getPrice(),
            certificate.getDuration(),
            tagIds);
    }
}

