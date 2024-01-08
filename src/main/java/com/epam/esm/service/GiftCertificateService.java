package esm.service;

import static com.epam.esm.exceptions.Codes.*;
import static com.epam.esm.exceptions.Messages.*;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.exceptions.Codes;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.repository.GiftCertificateTagRepository;
import com.epam.esm.validators.CertificateValidator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@SuppressWarnings("rawtypes")
@Slf4j
@Service
public class GiftCertificateService {

    private final GiftCertificateTagRepository giftCertificateTagRepository;

    @Autowired
    public GiftCertificateService(GiftCertificateTagRepository giftCertificateTagRepository) {
        this.giftCertificateTagRepository = giftCertificateTagRepository;
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
        GiftCertificate tryToFindCertificate = giftCertificateTagRepository.getGiftCertificateByName(giftCertificate.getName());

        GiftCertificate saveGiftCertificate = giftCertificateTagRepository.saveGiftCertificate(giftCertificate, tagIdsList);

        if (tryToFindCertificate == null) {
            if (saveGiftCertificate != null) {
                GiftCertificate response = giftCertificateTagRepository.getGiftCertificateByName(giftCertificate.getName());
                return new ResponseEntity<>(response, HttpStatus.CREATED);

            } else {
                ErrorDTO errorResponse = new ErrorDTO(CERTIFICATE_COULD_NOT_BE_SAVED, 500);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

        } else {
            Long idFound = tryToFindCertificate.getId();
            ErrorDTO errorResponse = new ErrorDTO(CERTIFICATE_ALREADY_EXISTS.formatted(idFound), CERTIFICATE_FOUND);
            return ResponseEntity.status(HttpStatus.FOUND).body(errorResponse);
        }
    }

    /**
     * @param giftCertificateId id of the certificate
     * @return if certificate exists, returns found with giftcertificate
     * if not, returns not found and error
     */
    public ResponseEntity getGiftCertificateById(@NonNull Long giftCertificateId) {

        GiftCertificate giftCertificate = giftCertificateTagRepository.getGiftCertificateById(giftCertificateId);

        if (giftCertificate != null) {
            return ResponseEntity.status(HttpStatus.FOUND).body(giftCertificate);
        } else {
            String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId);

            ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    /**
     * @param tagName         The name of the tag to filter the gift certificates.
     * @param searchWord      The word to search in the gift certificates.
     * @param nameOrder       The order in which to sort the gift certificates by name.
     * @param createDateOrder The order in which to sort the gift certificates by creation date.
     * @return can return the list and OK if list is not null, otherwise will return not found with message.
     */
    public ResponseEntity<?> getFilteredCertificates(String tagName, String searchWord, String nameOrder, String createDateOrder) {

        List<GiftCertificate> list = giftCertificateTagRepository.filterCertificates(tagName, searchWord, nameOrder, createDateOrder);

        if (list != null) {
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            String message = "Problem with list";

            ErrorDTO errorResponse = new ErrorDTO(message, 1000);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * @param giftCertificateId id of certificate to be deleted
     * @return if deleted, returns FOUND
     * if not deleted, returns NO CONTENT, with body containing message and code
     */
    public ResponseEntity<?> deleteGiftCertificate(Long giftCertificateId) {

        boolean certificateSuccessfullyDeleted = giftCertificateTagRepository.deleteGiftCertificate(giftCertificateId);

        return certificateSuccessfullyDeleted ?
                ResponseEntity.status(HttpStatus.FOUND).body(null) :
                ResponseEntity.status(HttpStatus.NO_CONTENT).body(
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

        Optional<ResponseEntity<ErrorDTO>> requestValidationMessage = validateCertificateRequest(giftCertificate, tagIdsList);
        if (requestValidationMessage.isPresent())
            return requestValidationMessage.get();

        tagIdsList = tagIdsList.stream().distinct().collect(Collectors.toList());

        if (giftCertificateTagRepository.getGiftCertificateById(id) == null) {
            String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(id);
            ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        if (!validateUpdate(id, giftCertificate, tagIdsList)) {
            GiftCertificate certificate = giftCertificateTagRepository.getGiftCertificateByName(giftCertificate.getName());

            String message = CERTIFICATE_ALREADY_EXISTS.formatted(certificate.getId());
            ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_FOUND);
            return ResponseEntity.status(HttpStatus.FOUND).body(errorResponse);
        }

        GiftCertificate responseDTO = giftCertificateTagRepository.updateGiftCertificate(id, giftCertificate, tagIdsList);
        if (responseDTO == null) {

            String message = "There are non existing tags";
            ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    private Optional<ResponseEntity<ErrorDTO>> validateCertificateRequest(GiftCertificate giftCertificate, List<Long> tagIds) {
        Optional<String> validationMessage = CertificateValidator.validateRequest(giftCertificate);
        for (Long tagId : tagIds)
            if (giftCertificateTagRepository.getTagById(tagId) == null) {
                ErrorDTO errorResponse = new ErrorDTO("non existing tags", CERTIFICATE_BAD_REQUEST);
                return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
            }

        if (validationMessage.isPresent()) {
            ErrorDTO errorResponse = new ErrorDTO(validationMessage.get(), CERTIFICATE_BAD_REQUEST);
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
        }
        return Optional.empty();
    }

    private boolean validateUpdate(Long id, GiftCertificate giftCertificate, List<Long> tagIdsList) {

        GiftCertificate certificate = giftCertificateTagRepository.getGiftCertificateById(id);

        List<Long> tagIdList = giftCertificateTagRepository.tagIdListByCertificateId(id);
        boolean nameExist = Objects.equals(certificate.getName(), giftCertificate.getName());
        boolean descriptionExist = Objects.equals(certificate.getDescription(), giftCertificate.getDescription());
        boolean priceExist = Objects.equals(certificate.getPrice(), giftCertificate.getPrice());
        boolean durationExist = Objects.equals(certificate.getDuration(), giftCertificate.getDuration());
        boolean tagsExist = tagIdList.equals(tagIdsList);

        return !nameExist || !descriptionExist || !priceExist || !durationExist || !tagsExist;
    }
}

