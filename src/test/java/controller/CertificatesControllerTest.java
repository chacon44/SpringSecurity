package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.esm.controller.CertificatesController;
import com.epam.esm.dto.CertificateRequestDTO;
import com.epam.esm.dto.CertificateResponseDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.CertificateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class CertificatesControllerTest {

    @InjectMocks
    private CertificatesController certificatesController;

    @Mock
    private CertificateService certificateService;

    @Mock
    private AuditReaderService auditReaderService;

    @Mock
    AuditReader auditReader;

    @Mock
    AuditQuery auditQuery;

    @Mock
    AuditQueryCreator auditQueryCreator;

    private MockMvc mockMvc;

  @BeforeEach
  public void setup(){
    this.mockMvc = MockMvcBuilders.standaloneSetup(certificatesController).build();
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testPostCertificate() throws Exception {
    // Given
    List<Long> tagIds = List.of(1L, 2L);
    CertificateRequestDTO requestDTO = new CertificateRequestDTO("name", "description", 100.0, 5L, tagIds);
    CertificateResponseDTO responseDTO = new CertificateResponseDTO(1L, "name", "description", 100.0, 5L, tagIds);

    // Mocks
    when(certificateService.saveGiftCertificate(any(GiftCertificate.class), anyList())).thenReturn(responseDTO);

    // When & Then
    mockMvc.perform(post("/api/certificate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(requestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.certificateId").value(1L))
        .andExpect(jsonPath("$.name").value("name"))
        .andExpect(jsonPath("$.description").value("description"))
        .andExpect(jsonPath("$.price").value(100.0))
        .andExpect(jsonPath("$.duration").value(5L))
        .andExpect(jsonPath("$.tagIds[0]").value(1L))
        .andExpect(jsonPath("$.tagIds[1]").value(2L));
  }

  @Test
  public void testGetCertificate() throws Exception {
    // Given
    List<Long> tagIds = List.of(1L, 2L);
    CertificateResponseDTO responseDTO = new CertificateResponseDTO(1L, "name", "description", 100.0, 5L, tagIds);

    // Mocks
    when(certificateService.getGiftCertificate(anyLong())).thenReturn(responseDTO);

    // When & Then
    mockMvc.perform(get("/api/certificate/" + 1L)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.certificateId").value(1L))
        .andExpect(jsonPath("$.name").value("name"))
        .andExpect(jsonPath("$.description").value("description"))
        .andExpect(jsonPath("$.price").value(100.0))
        .andExpect(jsonPath("$.duration").value(5L))
        .andExpect(jsonPath("$.tagIds[0]").value(1L))
        .andExpect(jsonPath("$.tagIds[1]").value(2L));
  }

  @Test
  public void testGetFilteredCertificates() throws Exception {
    // Given
    List<String> tagNames = List.of("tag1", "tag2");
    String searchWord = "search";
    int page = 0;
    int size = 10;
    String sort = "id";

    List<Long> tagIds = List.of(1L, 2L);
    CertificateResponseDTO responseDTO = new CertificateResponseDTO(1L, "name", "description", 100.0, 5L, tagIds);

    List<CertificateResponseDTO> certificates = new ArrayList<>();
    certificates.add(responseDTO);

    Page<CertificateResponseDTO> certificateDTOPage = new PageImpl<>(certificates);

    // Mocks
    when(certificateService.getFilteredCertificates(any(), any(), any())).thenReturn(certificateDTOPage);

    // When & Then
    mockMvc.perform(get("/api/certificate")
            .param("tagName", tagNames.toArray(new String[0]))
            .param("searchWord", searchWord)
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size))
            .param("sort", sort)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].certificateId").value(1L))
        .andExpect(jsonPath("$.content[0].name").value("name"))
        .andExpect(jsonPath("$.content[0].description").value("description"))
        .andExpect(jsonPath("$.content[0].price").value(100.0))
        .andExpect(jsonPath("$.content[0].duration").value(5L))
        .andExpect(jsonPath("$.content[0].tagIds[0]").value(1L))
        .andExpect(jsonPath("$.content[0].tagIds[1]").value(2L));
  }

  @Test
  public void testDeleteCertificate() throws Exception {
    // Given
    long id = 1L;

    doNothing().when(certificateService).deleteGiftCertificate(anyLong());

    // When & Then
    mockMvc.perform(delete("/api/certificate/" + id)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void testUpdateCertificate() throws Exception {
    // Given
    List<Long> tagIds = List.of(1L, 2L);
    CertificateRequestDTO requestDTO = new CertificateRequestDTO("name", "description", 100.0, 5L, tagIds);
    CertificateResponseDTO responseDTO = new CertificateResponseDTO(1L, "name", "description", 100.0, 5L, tagIds);

    // Mock
    when(certificateService.updateGiftCertificate(anyLong(), any(GiftCertificate.class), anyList())).thenReturn(responseDTO);

    // When & Then
    mockMvc.perform(patch("/api/certificate/" + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(requestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.certificateId").value(1L))
        .andExpect(jsonPath("$.name").value("name"))
        .andExpect(jsonPath("$.description").value("description"))
        .andExpect(jsonPath("$.price").value(100.0))
        .andExpect(jsonPath("$.duration").value(5L))
        .andExpect(jsonPath("$.tagIds[0]").value(1L))
        .andExpect(jsonPath("$.tagIds[1]").value(2L));
  }

  @Test
  public void testGetCertificateRevisions() throws Exception {
    // Given
    long id = 1L;

    GiftCertificate sampleCertificate = new GiftCertificate();
    sampleCertificate.setId(1L);
    sampleCertificate.setName("Sample");
    sampleCertificate.setDescription("Sample Description");
    sampleCertificate.setPrice(100.0);
    sampleCertificate.setDuration(5L);

    // Mocks
    when(auditReaderService.getReader()).thenReturn(auditReader);
    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(GiftCertificate.class, true, true))
        .thenReturn(auditQuery);
    when(auditReader.getRevisions(GiftCertificate.class, id)).thenReturn(Arrays.asList(1, 2));
    when(auditReader.find(GiftCertificate.class, id, 1)).thenReturn(sampleCertificate);
    when(auditReader.find(GiftCertificate.class, id, 2)).thenReturn(sampleCertificate);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/certificate/" + id + "/revisions")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].id").value(1L))
        .andExpect(jsonPath("$.[0].name").value("Sample"))
        .andExpect(jsonPath("$.[0].description").value("Sample Description"))
        .andExpect(jsonPath("$.[0].price").value(100.0))
        .andExpect(jsonPath("$.[0].duration").value(5L));
  }
}
