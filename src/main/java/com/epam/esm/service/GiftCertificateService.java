package com.epam.esm.service;

import static com.epam.esm.exceptions.Codes.CERTIFICATE_BAD_REQUEST;
import static com.epam.esm.exceptions.Codes.CERTIFICATE_FOUND;
import static com.epam.esm.exceptions.Codes.CERTIFICATE_NOT_FOUND;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificatesRepository;
import com.epam.esm.repository.TagRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GiftCertificateService {

    private final CertificatesRepository certificatesRepository;
    private final TagRepository tagRepository;

    @Autowired
    public GiftCertificateService(CertificatesRepository certificatesRepository, TagRepository tagRepository) {
        this.certificatesRepository = certificatesRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * The saveGiftCertificate method is used to save a new gift certificate in the database.
     * This method also validates the request and ensures that each gift certificate is unique.
     *
     * @param giftCertificate The gift certificate object to be saved. It should contain all the required fields.
     * @param tagIdsList      The list of tag IDs associated with the gift certificate.
     * @return If the gift certificate is saved successfully, it returns the saved certificate
     * with a HttpStatus of CREATED (201).
     * <p>
     * If the gift certificate could not be saved, it returns
     * an ErrorDTO object with a message and a HttpStatus of BAD_REQUEST (400).
     * <p>
     * If a gift certificate with the same name already exists in the database,
     * it returns an ErrorDTO object with a message and a HttpStatus of FOUND (302).
     */
    public ResponseEntity<?> saveGiftCertificate(@NonNull GiftCertificate giftCertificate, List<Long> tagIdsList) {

        Optional<ResponseEntity<ErrorDTO>> requestValidationMessage = validateCertificateRequest(giftCertificate, tagIdsList);
        if (requestValidationMessage.isPresent()) return requestValidationMessage.get();

        tagIdsList = tagIdsList.stream().distinct().collect(Collectors.toList());
        Optional<GiftCertificate> tryToFindCertificate = certificatesRepository.findCertificateByName(giftCertificate.getName());

        List<Tag> tags = tagRepository.findAllById(tagIdsList);
        List<Tag> uniqueTags = new ArrayList<>(new HashSet<>(tags));
        giftCertificate.setTags(uniqueTags);

        if (tryToFindCertificate.isEmpty()) {
            GiftCertificate savedGiftCertificate = certificatesRepository.save(giftCertificate);
                return new ResponseEntity<>(savedGiftCertificate, HttpStatus.CREATED);

        } else {
            GiftCertificate foundCertificate = tryToFindCertificate.get();
            Long idFound = foundCertificate.getId();
            ErrorDTO errorResponse = new ErrorDTO(CERTIFICATE_ALREADY_EXISTS.formatted(idFound), CERTIFICATE_FOUND);
            return ResponseEntity.status(HttpStatus.FOUND).body(errorResponse);
        }
    }

    /**
     * @param giftCertificateId id of the certificate
     * @return if the certificate exists, returns found with giftCertificate
     * if not, returns not found and error
     */
    public ResponseEntity<?> getGiftCertificateById(@NonNull Long giftCertificateId) {

        if (certificatesRepository.existsById(giftCertificateId)) {
            Optional<GiftCertificate> giftCertificate = certificatesRepository.findById(giftCertificateId);
            return ResponseEntity.status(HttpStatus.FOUND).body(giftCertificate);
        } else {
            String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId);

            ErrorDTO errorResponse = new ErrorDTO(message, CERTIFICATE_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    public ResponseEntity<List<GiftCertificate>> getFilteredCertificates(
        String tagName, String searchWord, String nameOrder, String createDateOrder) {

        Specification<GiftCertificate> spec = new CertificateSpecification(tagName, searchWord);
        Sort sort = constructSort(nameOrder, createDateOrder);

        List<GiftCertificate> certificates = certificatesRepository.findAll(spec, sort);

        return ResponseEntity.status(HttpStatus.OK).body(certificates);
    }
    private Sort constructSort(String nameOrder, String createDateOrder) {
        Sort sortBy = Sort.unsorted();

        if ("ASC".equalsIgnoreCase(nameOrder)) {
            sortBy = sortBy.and(Sort.by("name").ascending());
        } else if ("DESC".equalsIgnoreCase(nameOrder)) {
            sortBy = sortBy.and(Sort.by("name").descending());
        }

        if ("ASC".equalsIgnoreCase(createDateOrder)) {
            sortBy = sortBy.and(Sort.by("createDate").ascending());
        } else if ("DESC".equalsIgnoreCase(createDateOrder)) {
            sortBy = sortBy.and(Sort.by("createDate").descending());
        }

        return sortBy;
    }

    /**
     * @param giftCertificateId id of certificate to be deleted
     * @return if deleted, returns FOUND
     * if not deleted, returns NO CONTENT, with a body that contains message and code
     */
    public ResponseEntity<?> deleteGiftCertificate(Long giftCertificateId) {

        if(certificatesRepository.existsById(giftCertificateId)){
            certificatesRepository.deleteById(giftCertificateId);
            return ResponseEntity.status(HttpStatus.FOUND).body(null);
        }
        else
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
            new ErrorDTO(
                CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId),
                CERTIFICATE_NOT_FOUND));
    }

    public ResponseEntity<?> updateGiftCertificate(@NonNull Long id, GiftCertificate newGiftCertificate, List<Long> newTagIdsList ) {

        // Check if there's an issue with the request and return an error if any
        Optional<ResponseEntity<ErrorDTO>> requestValidationMessage = validateCertificateRequest(newGiftCertificate, newTagIdsList);
        if (requestValidationMessage.isPresent()) {
            return requestValidationMessage.get();
        }

        return certificatesRepository.findById(id).map(existingCertificate -> {
            if (isNewCertificateNameTaken(newGiftCertificate.getName(), id)) {
                Optional<GiftCertificate> optionalCertificate = certificatesRepository.findCertificateByName(newGiftCertificate.getName());
                if (optionalCertificate.isPresent()) {
                    Long foundId = optionalCertificate.get().getId();
                    return ResponseEntity.status(HttpStatus.FOUND)
                        .body(new ErrorDTO(CERTIFICATE_ALREADY_EXISTS.formatted(foundId), CERTIFICATE_FOUND));
                }
            }

            // Retrieve tags with given IDs from the database
            List<Tag> uniqueTags = tagRepository.findAllByIdIn(newTagIdsList);

            newGiftCertificate.setTags(uniqueTags);

            // Copy fields from updated certificate to the existing one
            BeanUtils.copyProperties(newGiftCertificate, existingCertificate, "id", "createDate");
            existingCertificate.setTags(newGiftCertificate.getTags());

            GiftCertificate savedCertificate = certificatesRepository.save(existingCertificate);

            return ResponseEntity.status(HttpStatus.OK).body(savedCertificate);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorDTO(
                CERTIFICATE_WITH_ID_NOT_FOUND.formatted(id),
                CERTIFICATE_NOT_FOUND)));
    }

    private boolean isNewCertificateNameTaken(String name, Long id) {
        Optional<GiftCertificate> certificateWithName = certificatesRepository.findCertificateByName(name);
        return certificateWithName.isPresent() && !certificateWithName.get().getId().equals(id);
    }

    public Optional<ResponseEntity<ErrorDTO>> validateCertificateRequest(GiftCertificate giftCertificate, List<Long> tagIds) {

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

    public Optional<String> validateRequest(GiftCertificate giftCertificate) {

        List<String> errors = new ArrayList<>();

        if (giftCertificate.getName().isEmpty()) {
            errors.add("Name is required");
        }

        if (giftCertificate.getDescription().isEmpty()) {
            errors.add("Description is required");
        }

        if (giftCertificate.getPrice() != null) {
            double price = giftCertificate.getPrice();
            if (Double.isNaN(price) || Double.isInfinite(price)) {
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

