package service;

import static com.epam.esm.exceptions.Messages.CERTIFICATE_ALREADY_EXISTS;
import static com.epam.esm.exceptions.Messages.CERTIFICATE_WITH_ID_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.esm.dto.CertificateResponseDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.service.CertificateService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {

    @InjectMocks
    private CertificateService certificateService;

    @Mock
    CertificateRepository certificateRepository;

    @Mock
    TagRepository tagRepository;

    public static final Long CERTIFICATE_ID = 3L;
    private static final String NAME = "Test Certificate";
    private static final String EXISTING_NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final double PRICE = 10.1;
    private static final long DURATION = 20L;
    private static final List<Long> TAG_IDS = Arrays.asList(1L, 2L, 3L);
    private static final List<Tag> tags = TAG_IDS.stream().map(id -> {
    Tag tag = new Tag();
    tag.setId(id);
    return tag;
  }).collect(Collectors.toList());
    @Mock
    GiftCertificate giftCertificate;

  @Test
  public void testSaveGiftCertificate_Success() {

    when(giftCertificate.getName()).thenReturn(NAME);
    when(giftCertificate.getDescription()).thenReturn(DESCRIPTION);
    when(giftCertificate.getPrice()).thenReturn(PRICE);
    when(giftCertificate.getDuration()).thenReturn(DURATION);
    when(giftCertificate.getTags()).thenReturn(tags);

    when(tagRepository.findAllById(TAG_IDS)).thenReturn(tags);
    when(certificateRepository.findByName(anyString())).thenReturn(Optional.empty());
    when(certificateRepository.save(any())).thenReturn(giftCertificate);

    // When
    CertificateResponseDTO result = certificateService.saveGiftCertificate(giftCertificate, TAG_IDS);

    // Then
    assertEquals(NAME, result.name());
    assertEquals(DESCRIPTION, result.description());
    assertEquals(PRICE, result.price());
    assertEquals(DURATION, result.duration());
    assertEquals(TAG_IDS, result.tagIds());
  }

  @Test
  public void testSaveGiftCertificate_AlreadyExists() {

    List<Long> tagIds = TAG_IDS;

    when(giftCertificate.getName()).thenReturn(NAME);
    when(giftCertificate.getDescription()).thenReturn(DESCRIPTION);
    when(giftCertificate.getPrice()).thenReturn(PRICE);
    when(giftCertificate.getDuration()).thenReturn(DURATION);

    List<Tag> tags = tagIds.stream().map(id -> {
      Tag tag = new Tag();
      tag.setId(id);
      return tag;
    }).collect(Collectors.toList());

    when(tagRepository.findAllById(tagIds)).thenReturn(tags);

    GiftCertificate existingCertificate = new GiftCertificate();
    existingCertificate.setId(1L);
    existingCertificate.setName(EXISTING_NAME);

    when(certificateRepository.findByName(anyString())).thenReturn(Optional.of(existingCertificate));

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () ->
        certificateService.saveGiftCertificate(giftCertificate, TAG_IDS));

    assertEquals(CERTIFICATE_ALREADY_EXISTS.formatted(1L), exception.getMessage());
  }

  @Test
  public void testSaveGiftCertificate_DatabaseError() {
    // Given
    List<Long> tagIds = TAG_IDS;

    // Mocks
    when(giftCertificate.getName()).thenReturn(NAME);
    when(giftCertificate.getDescription()).thenReturn(DESCRIPTION);
    List<Tag> tags = tagIds.stream().map(id -> {
      Tag tag = new Tag();
      tag.setId(id);
      return tag;
    }).collect(Collectors.toList());

    when(tagRepository.findAllById(tagIds)).thenReturn(tags);
    when(certificateRepository.findByName(anyString())).thenThrow(new DataAccessException("Test Exception") {});

    // When / Then
    Exception exception = assertThrows(CustomizedException.class, () ->
        certificateService.saveGiftCertificate(giftCertificate, tagIds));
    assertEquals("Database error while saving GiftCertificate", exception.getMessage());
  }

  @Test
  public void testGetGiftCertificate_Success() {
    // Given
    Long id = 1L;

    // Mocks
    when(giftCertificate.getName()).thenReturn(NAME);
    when(certificateRepository.findById(id)).thenReturn(Optional.of(giftCertificate));

    // When
    CertificateResponseDTO result = certificateService.getGiftCertificate(id);

    // Then
    assertEquals(NAME, result.name());
  }

  @Test
  public void testGetGiftCertificate_NonExistingId() {
    // Given
    Long invalidId = 200L;

    // Mocks
    when(certificateRepository.findById(invalidId)).thenReturn(Optional.empty());

    // When / Then
    Exception exception = assertThrows(CustomizedException.class, () ->
        certificateService.getGiftCertificate(invalidId));

    assertEquals(CERTIFICATE_WITH_ID_NOT_FOUND.formatted(invalidId), exception.getMessage());
  }

  @Test
  public void testGetGiftCertificate_DatabaseError() {

    when(certificateRepository.findById(CERTIFICATE_ID)).thenThrow(new DataAccessException("Test Exception") {});

    // When / Then
    Exception exception = assertThrows(CustomizedException.class, () ->
        certificateService.getGiftCertificate(CERTIFICATE_ID));

    assertEquals("Database error while getting gift certificate with id: " + CERTIFICATE_ID, exception.getMessage());
  }

  @Test
  public void testGetFilteredCertificates_Success() {
    // Given
    List<String> tagNames = Arrays.asList("tag1", "tag2");
    String searchWord = "text";
    Pageable pageable = PageRequest.of(0, 5);

    // Mocks
    when(giftCertificate.getName()).thenReturn(NAME);

    GiftCertificate anotherGiftCertificate = mock(GiftCertificate.class);
    when(anotherGiftCertificate.getName()).thenReturn("Another Certificate");

    List<GiftCertificate> mockCertificates = Arrays.asList(giftCertificate, anotherGiftCertificate);

    Page<GiftCertificate> certificatesPage = new PageImpl<>(mockCertificates);
    when(certificateRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(certificatesPage);

    // When
    Page<CertificateResponseDTO> results = certificateService.getFilteredCertificates(tagNames, searchWord, pageable);

    // Then
    assertEquals(2, results.getNumberOfElements());
    assertEquals(NAME, results.getContent().get(0).name());
    assertEquals("Another Certificate", results.getContent().get(1).name());
  }
  @Test
  public void testGetFilteredCertificates_DatabaseError() {
    // Given
    List<String> tagNames = Arrays.asList("tag1", "tag2");
    String searchWord = "text";
    Pageable pageable = PageRequest.of(0, 5);

    // Mocks
    when(certificateRepository.findAll(any(Specification.class), eq(pageable)))
        .thenThrow(new DataAccessException("Test Exception") {});

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () ->
        certificateService.getFilteredCertificates(tagNames, searchWord, pageable));

    assertEquals("Database error while getting filtered certificates", exception.getMessage());
  }

  @Test
  public void testUpdateGiftCertificate_Success(){

    //Mock
    when(giftCertificate.getName()).thenReturn(NAME);
    when(giftCertificate.getDescription()).thenReturn(DESCRIPTION);
    when(giftCertificate.getPrice()).thenReturn(PRICE);
    when(giftCertificate.getDuration()).thenReturn(DURATION);
    when(giftCertificate.getTags()).thenReturn(tags);
    when(certificateRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.of(giftCertificate));
    when(certificateRepository.save(any())).thenReturn(giftCertificate);

    //When
    CertificateResponseDTO result = certificateService.updateGiftCertificate(CERTIFICATE_ID, giftCertificate, TAG_IDS);

    //Then
    assertEquals(NAME, result.name());
    assertEquals(DESCRIPTION, result.description());
    assertEquals(PRICE, result.price());
    assertEquals(DURATION, result.duration());
    assertEquals(TAG_IDS, result.tagIds());
  }
  @Test
  public void testUpdateGiftCertificate_NonExistingId(){
    Long invalidId = 200L;

    // Mocks
    when(certificateRepository.findById(invalidId)).thenReturn(Optional.empty());

    // When / Then
    Exception exception = assertThrows(CustomizedException.class, () ->
        certificateService.updateGiftCertificate(invalidId, giftCertificate, TAG_IDS));
    assertEquals("Gift certificate not found with id: " + invalidId, exception.getMessage());
  }

  @Test
  public void testUpdateGiftCertificate_DatabaseError(){

    // Mocks
    when(certificateRepository.findById(CERTIFICATE_ID)).thenThrow(new DataAccessException("test exception") {});

    // When / Then
    Exception exception = assertThrows(CustomizedException.class, () ->
        certificateService.updateGiftCertificate(CERTIFICATE_ID, giftCertificate, TAG_IDS));
    assertEquals("Database error during update certificate with id " + CERTIFICATE_ID, exception.getMessage());
  }
}
