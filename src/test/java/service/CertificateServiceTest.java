package service;

import static com.epam.esm.exceptions.Codes.CERTIFICATE_FOUND;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.exceptions.Codes;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificatesRepository;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.service.GiftCertificateService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {

    @InjectMocks
    private GiftCertificateService giftCertificateService;

    @Mock
    CertificatesRepository certificatesRepository;

    @Mock
    TagRepository tagRepository;

    List<Long> tagIdsList = List.of(1L, 2L, 3L);

    List<Tag> tagList = tagIdsList.stream().map(id -> {
        Tag tag = new Tag();
        tag.setId(id);
        return tag;
    }).collect(Collectors.toList());

    private static final String EXISTING_NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final double PRICE = 10.1;
    private static final long DURATION = 20L;

    @Mock
    GiftCertificate giftCertificate;



    @Test
    public void saveGiftCertificate_correctlySaved() {
        GiftCertificate giftCertificateToSave = new GiftCertificate("name", "description", 10.1, 20L);
        List<Long> tagIdsList = Arrays.asList(1L, 2L, 3L);

        Mockito.when(tagRepository.findAllById(tagIdsList)).thenReturn(tagList);
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToSave.getName()))
            .thenReturn(Optional.empty());

        Mockito.when(certificatesRepository.save(any(GiftCertificate.class))).thenReturn(giftCertificateToSave);

        ResponseEntity<?> actual = giftCertificateService.saveGiftCertificate(giftCertificateToSave, tagIdsList);

        Mockito.verify(certificatesRepository).save(any(GiftCertificate.class));

        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        assertEquals(giftCertificateToSave, actual.getBody());
    }

    @Test
    public void saveGiftCertificate_alreadyExisting() {
        // Given
        GiftCertificate existingCertificate = new GiftCertificate(EXISTING_NAME, DESCRIPTION, PRICE, DURATION);
        Mockito.when(tagRepository.findAllById(tagIdsList)).thenReturn(tagList);
        Mockito.when(certificatesRepository.findCertificateByName(existingCertificate.getName()))
            .thenReturn(Optional.of(existingCertificate));

        // When
        ResponseEntity<?> response = giftCertificateService.saveGiftCertificate(existingCertificate, tagIdsList);

        // Then
        Optional<GiftCertificate> foundCert = certificatesRepository.findCertificateByName(existingCertificate.getName());
        if (foundCert.isPresent()) {
            Long certificateId = foundCert.get().getId();
            ErrorDTO expectedError = new ErrorDTO(CERTIFICATE_ALREADY_EXISTS.formatted(certificateId), CERTIFICATE_FOUND);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(response.getBody()).isEqualTo(expectedError);
        }
    }

    @Test
    public void getGiftCertificate_certificateExist() {

        Mockito.when(certificatesRepository.existsById(giftCertificate.getId())).thenReturn(true);
        Mockito.when(certificatesRepository.findById(giftCertificate.getId())).thenReturn(
            Optional.ofNullable(giftCertificate));
        ResponseEntity<?> actual = giftCertificateService.getGiftCertificateById(giftCertificate.getId());

        Mockito.verify(certificatesRepository).existsById(Mockito.eq(giftCertificate.getId()));
        Mockito.verify(certificatesRepository).findById(Mockito.eq(giftCertificate.getId()));

        assertEquals(HttpStatus.FOUND, actual.getStatusCode());
        assertEquals(Optional.ofNullable(giftCertificate), actual.getBody());
    }

    @Test
    public void getGiftCertificate_certificateNotExist() {

        String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificate.getId());

        ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_NOT_FOUND);
        ResponseEntity<ErrorDTO> expected = ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        Mockito.when(certificatesRepository.existsById(giftCertificate.getId())).thenReturn(false);
        ResponseEntity<?> actual = giftCertificateService.getGiftCertificateById(giftCertificate.getId());
        Mockito.verify(certificatesRepository).existsById(Mockito.eq(giftCertificate.getId()));

        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getBody(), actual.getBody());
    }

    @Test
    public void deleteGiftCertificate_certificateExists() {
        Long giftCertificateId = giftCertificate.getId();
        Mockito.when(certificatesRepository.existsById(giftCertificateId)).thenReturn(true);
        ResponseEntity<?> actual = giftCertificateService.deleteGiftCertificate(giftCertificateId);
        Mockito.verify(certificatesRepository).existsById(Mockito.eq(giftCertificateId));
        Mockito.verify(certificatesRepository).deleteById(Mockito.eq(giftCertificateId));
        assertEquals(HttpStatus.FOUND, actual.getStatusCode());
    }

    @Test
    public void deleteGiftCertificate_certificateNotExist() {
        Long giftCertificateId = 10000L;
        String message = CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId);
        ErrorDTO errorResponse = new ErrorDTO(message, Codes.CERTIFICATE_NOT_FOUND);
        ResponseEntity<ErrorDTO> expected = ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse);
        Mockito.when(certificatesRepository.existsById(giftCertificateId)).thenReturn(false);
        ResponseEntity<?> actual = giftCertificateService.deleteGiftCertificate(giftCertificateId);
        Mockito.verify(certificatesRepository).existsById(Mockito.eq(giftCertificateId));
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getBody(), actual.getBody());
    }
    @Test
    public void updateGiftCertificate_parametersValid_certificateUpdated() {
        // Arrange
        Long id = 1L;
        GiftCertificate giftCertificateToUpdate = new GiftCertificate("name", "description", 10.50, 20L);
        giftCertificateToUpdate.setId(id);

        Mockito.when(certificatesRepository.findById(id)).thenReturn(
            Optional.ofNullable(giftCertificate));
        Mockito.when(tagRepository.findAllById(tagIdsList)).thenReturn(tagList);
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToUpdate.getName())).thenReturn(Optional.empty());
        Mockito.when(certificatesRepository.save(any(GiftCertificate.class))).thenReturn(giftCertificateToUpdate);

        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(id, giftCertificateToUpdate, tagIdsList);

        // Assert
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(giftCertificateToUpdate, actual.getBody());
    }

    @Test
    public void updateGiftCertificate_parametersNotValid() {
        // Arrange
        Long id = giftCertificate.getId();
        giftCertificate.setName(null);

        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(id, giftCertificate, tagIdsList);

        // Assert
        assertInstanceOf(ErrorDTO.class, actual.getBody());
    }

    @Test
    public void updateGiftCertificate_certificateAlreadyExists() {
        // Arrange
        Long certificate_id = 20L;



        GiftCertificate giftCertificateToSave = new GiftCertificate("newName", "description", 10.1, 20L);
        GiftCertificate existingCertificate = new GiftCertificate("newName", "description", 10.1, 20L);
        Long existingCertificate_id = 21L;
        existingCertificate.setId(existingCertificate_id);

        Mockito.when(tagRepository.findAllById(tagIdsList)).thenReturn(tagList);
        Mockito.when(certificatesRepository.findById(certificate_id)).thenReturn(Optional.of(giftCertificateToSave));
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToSave.getName())).thenReturn(Optional.of(existingCertificate));
        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(certificate_id, giftCertificateToSave, tagIdsList);

        // Assert
        assertEquals(HttpStatus.FOUND, actual.getStatusCode());
    }

    @Test
    public void updateGiftCertificate_idNotAssociatedToAnyCertificate() {
        // Arrange
        Long certificateId = 999L;
        GiftCertificate giftCertificateToUpdate = new GiftCertificate("name", "description", 10.1, 20L);
        giftCertificateToUpdate.setId(certificateId);

        Mockito.when(tagRepository.findAllById(tagIdsList)).thenReturn(tagList);
        Mockito.when(certificatesRepository.findById(certificateId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(certificateId, giftCertificateToUpdate, tagIdsList);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void updateGiftCertificate_someTagsDoNotExist() {
        // Arrange
        Long id = giftCertificate.getId();
        List<Long> tagIdsList = Arrays.asList(999L, 1000L);

        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(id, giftCertificate, tagIdsList);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

}
