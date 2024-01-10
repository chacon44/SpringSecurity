package com.epam.esm.controller;

import static com.epam.esm.exceptions.Codes.CERTIFICATE_NOT_FOUND;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.Dto.GiftCertificateRequestDTO;
import com.epam.esm.exceptions.CertificateNotFoundException;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.service.GiftCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CertificatesController {

    @Autowired
    private GiftCertificateService giftCertificateService;

    /**
     * Creates a new gift certificate.
     *
     * @param requestDTO The data transfer object containing the details of the gift certificate to be created.
     * @return ResponseEntity<?> A response entity representing the result of the creation operation.
     *          A ResponseEntity object that contains either the saved gift certificate,
     *              an error message indicating the gift certificate could not be saved,
     *              or an error message indicating the gift certificate already exists.
     * <p>
     *               If the gift certificate is saved successfully, it returns the saved certificate
     *               with a HttpStatus of CREATED (201)
     *               If the gift certificate could not be saved for any reason, it returns
     *               an ErrorDTO object with a message and a HttpStatus of BAD_REQUEST (400).
     * <p>
     *               If a gift certificate with the same name already exists in the database,
     *               it returns an ErrorDTO object with a message and a HttpStatus of FOUND (302).
     *
     * @PostMapping This annotation maps HTTP POST requests onto this method.
     * @value "/certificate" The path where this method is mapped.
     * @consumes {"application/json"} Specifies that this method only processes requests where the Content-Type header is application/json.
     * @produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @PostMapping(value = "/certificate", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<?> postCertificate(@RequestBody GiftCertificateRequestDTO requestDTO) {
        GiftCertificate giftCertificate = new GiftCertificate(
                requestDTO.name(),
                requestDTO.description(),
                requestDTO.price(),
                requestDTO.duration()
        );
        return giftCertificateService.saveGiftCertificate(giftCertificate, requestDTO.tagIds());
    }

    /**
     * Retrieves a gift certificate by its ID.
     *
     * @param id The unique identifier of the gift certificate to be retrieved.
     * @return ResponseEntity<?> A response entity containing the gift certificate with the given ID.
     * if certificate exists, returns Response Entity with giftCertificate
     * if not, returns not found and error
     * @GetMapping This annotation maps HTTP GET requests onto this method.
     * @value "/certificate/{id}" The path where this method is mapped. It includes a path variable 'id'.
     * @consumes {"application/json"} Specifies that this method only processes requests where the Content-Type header is application/json.
     * @produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @GetMapping(value = "/certificate/{id}", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<?> getCertificate(@PathVariable long id) {
        return giftCertificateService.getGiftCertificateById(id);
    }

    /**
     * Retrieves gift certificates based on various filter criteria.
     *
     * @param tagName The name of the tag to filter the gift certificates.
     * @param searchWord The word to search in the gift certificates.
     * @param nameOrder The order in which to sort the gift certificates by name.
     * @param createDateOrder The order in which to sort the gift certificates by creation date.
     * @return ResponseEntity<?> A response entity containing the filtered and sorted gift certificates.
     *
     * @GetMapping This annotation maps HTTP GET requests onto this method.
     * @value "/certificate/" The path where this method is mapped.
     * produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @GetMapping(value = "/certificate/", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<?> getFilteredCertificates(
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String searchWord,
            @RequestParam(required = false) String nameOrder,
            @RequestParam(required = false) String createDateOrder) {

        return giftCertificateService.getFilteredCertificates(
                tagName,
                searchWord,
                nameOrder,
                createDateOrder);
    }

    /**
     * Deletes a gift certificate by its ID.
     *
     * @param id The unique identifier of the gift certificate to be deleted.
     * @return ResponseEntity<?> A response entity representing the result of the deletion operation.
     * If deleted, returns FOUND
     * if not deleted, returns NO CONTENT with a body-containing message and code
     * <p>
     * DeleteMapping This annotation maps HTTP DELETE requests onto this method.
     * Value "/certificate/{id}" The path where this method is mapped.
     * It includes a path variable 'id'.
     * Consumes {"application/json"} Specifies that this method only processes requests where the Content-Type header is application/json.
     * Produces {"application/json"} Specifies that this method returns data in application/json format.
     */
    @DeleteMapping(value = "/certificate/{id}", consumes = {"application/json"}, produces = {"application/json"})
    ResponseEntity<?> deleteCertificate(@PathVariable long id) {
        return giftCertificateService.deleteGiftCertificate(id);
    }


//    @PutMapping(value = "/certificate/{id}", consumes = {"application/json"}, produces = {"application/json"})
//    ResponseEntity<?> updateCertificate(@PathVariable long id, @RequestBody GiftCertificateRequestDTO requestDTO) {
//
//        return giftCertificateService.updateGiftCertificate(id, new GiftCertificate(
//                requestDTO.name(),
//                requestDTO.description(),
//                requestDTO.price(),
//                requestDTO.duration()
//        ), requestDTO.tagIds());
//    }

    @ExceptionHandler(CertificateNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleCertificateNotFoundException(
        CertificateNotFoundException e) {
        ErrorDTO errorResponse = new ErrorDTO(e.getMessage(), CERTIFICATE_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @PatchMapping("/certificate/{id}")
    public ResponseEntity<GiftCertificate> updateCertificate(@PathVariable Long id, @RequestBody GiftCertificateRequestDTO requestDTO){
        GiftCertificate updatedCertificate = giftCertificateService.updateGiftCertificate(id, new GiftCertificate(
                requestDTO.name(),
                requestDTO.description(),
                requestDTO.price(),
                requestDTO.duration()), requestDTO.tagIds());
        return ResponseEntity.ok(updatedCertificate);
    }
    /* Format of POST
            "name" : "name",
            "description" : "description",
            "price" : 1.0,
            "duration" : 1,
            "tagIds" : [1,2]
     */
}
