package service;

import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.exceptions.Codes;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificatesRepository;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.service.GiftCertificateService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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


    @Mock
    GiftCertificate giftCertificate;

    @Mock
    List<GiftCertificate> listOfCertificates;

    @Mock
    List<Tag> tagList;

    private final
    List<Long> tagIdsList = List.of(1L, 3L);

    @Test
    public void saveGiftCertificate_correctlySaved() {
        GiftCertificate giftCertificateToSave = new GiftCertificate("name", "description", 10.1, 20L);
        List<Long> tagIdsList = Arrays.asList(1L, 2L, 3L);

        Mockito.when(tagRepository.existsById(anyLong())).thenReturn(true);
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToSave.getName()))
            .thenReturn(Optional.empty());

        List<Tag> tags = new ArrayList<>();
        Mockito.when(tagRepository.findAllById(tagIdsList)).thenReturn(tags);

        Mockito.when(certificatesRepository.save(any(GiftCertificate.class))).thenReturn(giftCertificateToSave);

        ResponseEntity<?> actual = giftCertificateService.saveGiftCertificate(giftCertificateToSave, tagIdsList);

        Mockito.verify(certificatesRepository).save(any(GiftCertificate.class));

        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        assertEquals(giftCertificateToSave, actual.getBody());
    }

    @Test
    public void saveGiftCertificate_cannotBeSaved() {
        GiftCertificate giftCertificateToSave = new GiftCertificate("name", "description", 10.1, 20L);

        Mockito.when(tagRepository.existsById(anyLong())).thenReturn(true);
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToSave.getName()))
            .thenReturn(Optional.of(giftCertificateToSave));

        ResponseEntity<?> actual = giftCertificateService.saveGiftCertificate(giftCertificateToSave, tagIdsList);

        assertEquals(HttpStatus.FOUND, actual.getStatusCode());
      assertInstanceOf(ErrorDTO.class, actual.getBody());
    }

    @Test
    public void saveGiftCertificate_alreadyExisting() {
        GiftCertificate giftCertificateToSave = new GiftCertificate("name", "description", 10.1, 20L);

        Mockito.when(tagRepository.existsById(anyLong())).thenReturn(true);
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToSave.getName()))
            .thenReturn(Optional.empty());

        Mockito.when(certificatesRepository.save(any(GiftCertificate.class))).thenReturn(giftCertificateToSave);

        ResponseEntity<?> actual = giftCertificateService.saveGiftCertificate(giftCertificateToSave, tagIdsList);

        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        assertEquals(giftCertificateToSave, actual.getBody());
    }

    @Test
    public void getGiftCertificate_certificateExist() {

        Mockito.when(certificatesRepository.existsById(giftCertificate.getId())).thenReturn(true);
        Mockito.when(certificatesRepository.getReferenceById(giftCertificate.getId())).thenReturn(giftCertificate);
        ResponseEntity<?> actual = giftCertificateService.getGiftCertificateById(giftCertificate.getId());

        Mockito.verify(certificatesRepository).existsById(Mockito.eq(giftCertificate.getId()));
        Mockito.verify(certificatesRepository).getReferenceById(Mockito.eq(giftCertificate.getId()));

        assertEquals(HttpStatus.FOUND, actual.getStatusCode());
        assertEquals(giftCertificate, actual.getBody());
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
    public void getFilteredCertificates_correct() {
        // Arrange

        Mockito.when(certificatesRepository.findByTagName(anyString())).thenReturn(listOfCertificates);
        Mockito.when(certificatesRepository.findByNameContainsOrDescriptionContains(anyString(),anyString())).thenReturn(listOfCertificates);

        // Act
        ResponseEntity<?> actual = giftCertificateService.getFilteredCertificates("tag","name","ASC","DESC");

        // Assert
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(listOfCertificates, actual.getBody());
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
        Mockito.when(certificatesRepository.existsById(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(tagRepository.existsById(ArgumentMatchers.anyLong())).thenReturn(true);
        Mockito.when(certificatesRepository.findCertificateByName(giftCertificateToUpdate.getName())).thenReturn(Optional.empty());
        Mockito.when(certificatesRepository.save(any(GiftCertificate.class))).thenReturn(giftCertificateToUpdate);

        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(id, giftCertificateToUpdate, List.of(1L, 2L));

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
        GiftCertificate giftCertificateToSave = new GiftCertificate("name", "description", 10.1, 20L);


        GiftCertificate giftCertificate1 = new GiftCertificate("name", "description", 10.1, 20L);
        Long certificate_id = 20L;
        giftCertificate1.setId(certificate_id);

        List<Long> tagIdsList = List.of(1L, 2L, 3L);
        List<Tag> tags = tagIdsList.stream().map(id -> {
            Tag tag = new Tag();
            tag.setId(id);
            return tag;
        }).collect(Collectors.toList());

        Mockito.when(certificatesRepository
            .findByNameAndDescriptionAndPriceAndDuration(giftCertificateToSave.getName(), giftCertificateToSave.getDescription(),
                giftCertificateToSave.getPrice(), giftCertificateToSave.getDuration())).thenReturn(
            Optional.of(giftCertificate1));
        Mockito.when(certificatesRepository.findTagsByCertificateId(certificate_id)).thenReturn(tags);

        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(certificate_id, giftCertificateToSave, tagIdsList);

        // Assert
        assertEquals(HttpStatus.FOUND, actual.getStatusCode());
        assertTrue(giftCertificateService.checkIfGiftCertificateExists(giftCertificate1, tagIdsList));
    }

    @Test
    public void updateGiftCertificate_idNotAssociatedToAnyCertificate() {
        // Arrange
        Long id = 999L;
        GiftCertificate giftCertificateToUpdate = new GiftCertificate("name", "description", 10.1, 20L);

        Mockito.when(tagRepository.existsById(ArgumentMatchers.anyLong())).thenReturn(true);

        // Act
        ResponseEntity<?> actual = giftCertificateService.updateGiftCertificate(id, giftCertificateToUpdate, tagIdsList);

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
