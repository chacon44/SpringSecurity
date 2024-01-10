package com.epam.esm.service;

import static com.epam.esm.exceptions.Codes.CERTIFICATE_BAD_REQUEST;
import static com.epam.esm.exceptions.Codes.CERTIFICATE_FOUND;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.exceptions.Codes;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificatesRepository;
import com.epam.esm.repository.TagRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @return if certificate exists, returns found with giftcertificate
     * if not, returns not found and error
     */
    public ResponseEntity<?> getGiftCertificateById(@NonNull Long giftCertificateId) {

        if (certificatesRepository.existsById(giftCertificateId)) {
            Optional<GiftCertificate> giftCertificate = certificatesRepository.findById(giftCertificateId);
            return ResponseEntity.status(HttpStatus.FOUND).body(giftCertificate);
        } else {
            String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId);

            ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    public List<GiftCertificate> searchCertificatesByKeyword(String keyword) {
      return certificatesRepository.findByNameContainsOrDescriptionContains(keyword, keyword);
    }

    public List<GiftCertificate> getCertificatesByTagId(String tagName) {

        Tag tag = tagRepository.findByName(tagName).get();
        Long tagId = tag.getId();
        return certificatesRepository.findCertificateByTagId(tagId);
    }
    public ResponseEntity<?> getFilteredCertificates(String tagName, String searchWord, String nameOrder, String createDateOrder) {

        List<GiftCertificate> list = filterCertificates(tagName, searchWord, nameOrder, createDateOrder);

        if (list != null) {
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            String message = "Problem with list";

            ErrorDTO errorResponse = new ErrorDTO(message, 1000);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    public List<GiftCertificate> filterCertificates(String tagName, String searchWord, String nameOrder, String createDateOrder) {
        List<GiftCertificate> certificatesByTagName = getCertificatesByTagId(tagName);

      return certificatesByTagName.stream()
            .filter(certificate ->
                certificate.getName().contains(searchWord) ||
                    certificate.getDescription().contains(searchWord)
            )
            .sorted(
                Comparator.comparing(GiftCertificate::getName,
                        "DESC".equalsIgnoreCase(nameOrder) ? Comparator.reverseOrder() : Comparator.naturalOrder())
                    .thenComparing(GiftCertificate::getCreateDate,
                        "DESC".equalsIgnoreCase(createDateOrder) ? Comparator.reverseOrder() : Comparator.naturalOrder())
            )
            .collect(Collectors.toList());
    }

    /**
     * @param giftCertificateId id of certificate to be deleted
     * @return if deleted, returns FOUND
     * if not deleted, returns NO CONTENT, with body containing message and code
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
                Codes.CERTIFICATE_NOT_FOUND));
    }

    /**
     * @param id              cannot be null
     * @param giftCertificate cannot be empty
     *                        can contain not valid parameters
     *                        <p>
     *                        new values of certificate to be assigned to the id
     * @param tagIdsList      can be empty
     * @return Updated correctly: ResponseEntity.status(HttpStatus.OK).body(responseDTO)
     * Parameters not valid: requestValidationMessage.get() when parameters not valid
     * Already existing identical certificate: ResponseEntity.status(HttpStatus.FOUND).body(errorResponse)
     * Id not associated to any certificate: ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
     * Some of tags doesn't exists: ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
     */
    public ResponseEntity<?> updateGiftCertificate(@NonNull Long id, GiftCertificate giftCertificate, List<Long> tagIdsList) {

        if(checkIfGiftCertificateExists(giftCertificate,tagIdsList)) {
            ErrorDTO errorResponse = new ErrorDTO("already existing", CERTIFICATE_FOUND);
            return ResponseEntity.status(HttpStatus.FOUND).body(errorResponse);
        }
        Optional<ResponseEntity<ErrorDTO>> requestValidationMessage = validateCertificateRequest(giftCertificate, tagIdsList);
        if (requestValidationMessage.isPresent())
            return requestValidationMessage.get();

        tagIdsList = tagIdsList.stream().distinct().collect(Collectors.toList());

        Optional<GiftCertificate> existingCertificateOptional = certificatesRepository.findById(id);
        if (!certificatesRepository.existsById(id)) {
            String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(id);
            ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Optional<GiftCertificate> certificateWithName = certificatesRepository.findCertificateByName(giftCertificate.getName());
        if (certificateWithName.isPresent() && !certificateWithName.get().getId().equals(id)) {
            String message = CERTIFICATE_ALREADY_EXISTS.formatted(certificateWithName.get().getId());
            ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_FOUND);
            return ResponseEntity.status(HttpStatus.FOUND).body(errorResponse);
        }

        // Retrieve tags by ids, and set them to giftCertificate
        List<Tag> tags = tagRepository.findAllById(tagIdsList);
        List<Tag> uniqueTags = new ArrayList<>(new HashSet<>(tags));
        giftCertificate.setTags(uniqueTags);

        // Copy properties from the passed giftCertificate to the existing one
        GiftCertificate existingCertificate = existingCertificateOptional.get();
        BeanUtils.copyProperties(giftCertificate, existingCertificate, "id", "createDate");
        existingCertificate.setTags(giftCertificate.getTags());

        GiftCertificate savedCertificate = certificatesRepository.save(existingCertificate);

        return ResponseEntity.status(HttpStatus.OK).body(savedCertificate);
    }

    public Optional<ResponseEntity<ErrorDTO>> validateCertificateRequest(GiftCertificate giftCertificate, List<Long> tagIds) {

        Optional<String> validationMessage = validateRequest(giftCertificate);

        for (Long tagId : tagIds)
            if (!tagRepository.existsById(tagId)) {
                ErrorDTO errorResponse = new ErrorDTO("non existing tags", CERTIFICATE_BAD_REQUEST);
                return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
            }

        if (validationMessage.isPresent()) {
            ErrorDTO errorResponse = new ErrorDTO(validationMessage.get(), CERTIFICATE_BAD_REQUEST);
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
        }
        return Optional.empty();
    }

    public boolean checkIfGiftCertificateExists(GiftCertificate giftCertificate, List<Long> newTagIds) {
        Optional<GiftCertificate> existingGiftCertificateOptional = certificatesRepository
            .findByNameAndDescriptionAndPriceAndDuration(giftCertificate.getName(), giftCertificate.getDescription(),
                giftCertificate.getPrice(), giftCertificate.getDuration());

        if(existingGiftCertificateOptional.isEmpty()) {
            return false;
        }

        GiftCertificate existingGiftCertificate = existingGiftCertificateOptional.get();
        List<Tag> existingTags = certificatesRepository.findTagsByCertificateId(existingGiftCertificate.getId());
        List<Long> existingTagIds = existingTags.stream().map(Tag::getId).toList();

        return new HashSet<>(existingTagIds).equals(new HashSet<>(newTagIds));
    }

    public Optional<String> validateRequest(GiftCertificate giftCertificate) {

        List<String> errors = new ArrayList<>();

        if (giftCertificate.getName() == null || giftCertificate.getName().isEmpty()) {
            errors.add("Name is required");
        }

        if (giftCertificate.getDescription() == null || giftCertificate.getDescription().isEmpty()) {
            errors.add("Description is required");
        }

        try {
            double price = Double.parseDouble(giftCertificate.getPrice().toString());
            if (Double.isNaN(price) || Double.isInfinite(price)) {
                errors.add("Price must be a finite number");
            } else if (price < 0) {
                errors.add("Price must be non-negative");
            }
        } catch (NumberFormatException e) {
            errors.add("Price must be a valid number");
        }

        try {
            long duration = Long.parseLong(giftCertificate.getDuration().toString());
            if (duration < 0) {
                errors.add("Duration must be non-negative");
            }
        } catch (NumberFormatException e) {
            errors.add("Duration must be a valid number");
        }

        if (errors.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(String.join(", ", errors));
        }
    }
}

