package com.epam.esm.service;

import static com.epam.esm.exceptions.Codes.CERTIFICATE_BAD_REQUEST;
import static com.epam.esm.exceptions.Codes.CERTIFICATE_FOUND;
import static com.epam.esm.exceptions.Codes.CERTIFICATE_NOT_FOUND;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.domain.Sort.unsorted;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.exceptions.CertificateNotFoundException;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.utils.CertificateSpecification;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<?> saveGiftCertificate(@NonNull GiftCertificate giftCertificate, List<Long> tagIdsList) {

        Optional<ResponseEntity<ErrorDTO>> requestValidationMessage = validateCertificateRequest(giftCertificate, tagIdsList);
        if (requestValidationMessage.isPresent()) return requestValidationMessage.get();

        Optional<GiftCertificate> tryToFindCertificate = certificateRepository.findByName(giftCertificate.getName());

        List<Tag> tags = tagRepository.findAllByIdIn(tagIdsList);
        giftCertificate.setTags(tags);

        if (tryToFindCertificate.isEmpty()) {
            GiftCertificate savedGiftCertificate = certificateRepository.save(giftCertificate);
                return new ResponseEntity<>(savedGiftCertificate, HttpStatus.CREATED);

        } else {
            GiftCertificate foundCertificate = tryToFindCertificate.get();
            Long idFound = foundCertificate.getId();
            ErrorDTO errorResponse = new ErrorDTO(CERTIFICATE_ALREADY_EXISTS.formatted(idFound), CERTIFICATE_FOUND);
            return ResponseEntity.status(HttpStatus.FOUND).body(errorResponse);
        }
    }

    public ResponseEntity<?> getGiftCertificate(@NonNull Long giftCertificateId) {

        if (certificateRepository.existsById(giftCertificateId)) {
            Optional<GiftCertificate> giftCertificate = certificateRepository.findById(giftCertificateId);
            return ResponseEntity.status(HttpStatus.FOUND).body(giftCertificate);
        } else {
            String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId);

            ErrorDTO errorResponse = new ErrorDTO(message, CERTIFICATE_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    public ResponseEntity<List<GiftCertificate>> getFilteredCertificates(
        List<String> tagNames, String searchWord, String nameOrder, String createDateOrder) {

        Specification<GiftCertificate> spec = new CertificateSpecification(tagNames, searchWord);
        Sort sort = constructSort(nameOrder, createDateOrder);

        List<GiftCertificate> certificates = certificateRepository.findAll(spec, sort);

        return ResponseEntity.status(HttpStatus.OK).body(certificates);
    }
    private Sort constructSort(String nameOrder, String createDateOrder) {
        Sort sortBy = unsorted();

        if ("ASC".equalsIgnoreCase(nameOrder)) {
            sortBy = sortBy.and(by("name").ascending());
        } else if ("DESC".equalsIgnoreCase(nameOrder)) {
            sortBy = sortBy.and(by("name").descending());
        }

        if ("ASC".equalsIgnoreCase(createDateOrder)) {
            sortBy = sortBy.and(by("createDate").ascending());
        } else if ("DESC".equalsIgnoreCase(createDateOrder)) {
            sortBy = sortBy.and(by("createDate").descending());
        }

        return sortBy;
    }

    public ResponseEntity<?> deleteGiftCertificate(Long giftCertificateId) {

        if(certificateRepository.existsById(giftCertificateId)){
            certificateRepository.deleteById(giftCertificateId);
            return ResponseEntity.status(HttpStatus.FOUND).body(null);
        }
        else
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
            new ErrorDTO(
                CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId),
                CERTIFICATE_NOT_FOUND));
    }

    @Transactional
    public GiftCertificate updateGiftCertificate(@NonNull Long certificateId, GiftCertificate updates, List<Long> newTagIdsList) {
        GiftCertificate existingCertificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new CertificateNotFoundException(certificateId));

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
            existingCertificate.getTags().clear(); // remove old tags
            existingCertificate.getTags().addAll(uniqueTags); // add new tags
        }

        return certificateRepository.save(existingCertificate);
    }

    private Optional<ResponseEntity<ErrorDTO>> validateCertificateRequest(GiftCertificate giftCertificate, List<Long> tagIds) {

        Optional<String> validationMessage = validateRequest(giftCertificate);
        if (validationMessage.isPresent()) {
            ErrorDTO errorResponse = new ErrorDTO(validationMessage.get(), CERTIFICATE_BAD_REQUEST);
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
        }

        List<Tag> existingTags = tagRepository.findAllById(tagIds);

        if(existingTags.size() != tagIds.size()){
            ErrorDTO errorResponse = new ErrorDTO("non existing tags", CERTIFICATE_BAD_REQUEST);
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
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
}

