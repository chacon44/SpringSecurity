package controller;

import com.epam.esm.Dto.GiftCertificate.GiftCertificateRequestDTO;
import com.epam.esm.Main;
import com.epam.esm.controller.CertificatesController;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.service.GiftCertificateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CertificatesControllerTest {

    @InjectMocks
    private CertificatesController certificatesController;

    @Mock
    private GiftCertificateService giftCertificateService;

    @Mock
    GiftCertificate giftCertificate;

    @Mock
    GiftCertificateRequestDTO giftCertificateRequestDTO;

    private MockMvc mockMvc;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.certificatesController).build();
    }

    @Test
    public void postCertificate() throws Exception {

        GiftCertificate giftCertificateToSave = new GiftCertificate(
                giftCertificateRequestDTO.name(),
                giftCertificateRequestDTO.description(),
                giftCertificateRequestDTO.price(),
                giftCertificateRequestDTO.duration()
        );

        List<Long> tagIds = giftCertificateRequestDTO.tagIds();
        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(giftCertificate);
        doReturn(responseEntity).when(giftCertificateService).saveGiftCertificate(giftCertificateToSave, tagIds);

        // Act
        mockMvc.perform(post("/certificate")
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(giftCertificateRequestDTO)))
                //Assert
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(giftCertificate.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(giftCertificate.getName())))
                .andExpect(jsonPath("$.description", is(giftCertificate.getDescription())))
                .andExpect(jsonPath("$.price", is(giftCertificate.getPrice())))
                .andExpect(jsonPath("$.duration", is(giftCertificate.getDuration().intValue())))
                .andExpect(jsonPath("$.createDate", is(giftCertificate.getCreateDate())))
                .andExpect(jsonPath("$.lastUpdateDate", is(giftCertificate.getLastUpdateDate())))
                .andExpect(jsonPath("$.tags", is(giftCertificate.getTags())))
                .andExpect(status().isCreated());
    }

    @Test
    public void getCertificate() throws Exception {
        // Arrange

        Long id = giftCertificate.getId();
        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(giftCertificate);
        doReturn(responseEntity).when(giftCertificateService).getGiftCertificateById(id);

        // Act
        mockMvc.perform(get("/certificate/{id}", id)
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(id)))
                //Assert
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id), Long.class))
                .andExpect(jsonPath("$.name", is(giftCertificate.getName())))
                .andExpect(jsonPath("$.description", is(giftCertificate.getDescription())))
                .andExpect(jsonPath("$.price", is(giftCertificate.getPrice())))
                .andExpect(jsonPath("$.duration", is(giftCertificate.getDuration().intValue())))
                .andExpect(jsonPath("$.createDate", is(giftCertificate.getCreateDate())))
                .andExpect(jsonPath("$.lastUpdateDate", is(giftCertificate.getLastUpdateDate())))
                .andExpect(jsonPath("$.tags", is(giftCertificate.getTags())))
                .andExpect(status().isCreated());
    }

    @Test
    public void getCertificate_filter() throws Exception {
        // Arrange
        List<Long> tagIds = List.of(1L, 2L);
        Long id = 1L;
        Tag tag1 = new Tag(1L, "tag1");
        Tag tag2 = new Tag(2L, "tag2");

        String tagName = "tag1";
        String searchWord = "name";
        String nameOrder = "ASC";
        String createDateOrder = "ASC";

        List<Tag> tags = List.of(tag1, tag2);
        GiftCertificateRequestDTO giftCertificateRequestDTO = new GiftCertificateRequestDTO(
                "name", "description", 10.50, 20L, tagIds);

//        GiftCertificate giftCertificateToGet = new GiftCertificate(
//                1L,
//                giftCertificateRequestDTO.name(),
//                giftCertificateRequestDTO.description(),
//                giftCertificateRequestDTO.price(),
//                giftCertificateRequestDTO.duration(),
//                "createDate",
//                "last update date",
//                tags);

        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.OK).body(giftCertificate);
        doReturn(responseEntity).when(giftCertificateService).getFilteredCertificates(tagName,searchWord,nameOrder,createDateOrder);

        // Act
        mockMvc.perform(get("/certificate/")
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)

                .param("tagName", tagName)
                .param("searchWord", searchWord)
                .param("nameOrder", nameOrder)
                .param("createDateOrder", createDateOrder))
                //Assert
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(giftCertificate.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(giftCertificate.getName())))
                .andExpect(jsonPath("$.description", is(giftCertificate.getDescription())))
                .andExpect(jsonPath("$.price", is(giftCertificate.getPrice())))
                .andExpect(jsonPath("$.duration", is(giftCertificate.getDuration().intValue())))
                .andExpect(jsonPath("$.createDate", is(giftCertificate.getCreateDate())))
                .andExpect(jsonPath("$.lastUpdateDate", is(giftCertificate.getLastUpdateDate())))
                .andExpect(jsonPath("$.tags", is(giftCertificate.getTags())))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteCertificate() throws Exception {
        // Arrange
        Long id = 1L;

        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.FOUND).body(null);
        doReturn(responseEntity).when(giftCertificateService).deleteGiftCertificate(id);

        // Act
        mockMvc.perform(delete("/certificate/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(id)))
                //Assert
                .andExpect(status().isFound());
    }

    @Test
    public void putCertificate() throws Exception {
        Long id = 1L;
        List<Long> tagIds = giftCertificateRequestDTO.tagIds();
        GiftCertificate giftCertificateToUpdate = new GiftCertificate(
                giftCertificateRequestDTO.name(),
                giftCertificateRequestDTO.description(),
                giftCertificateRequestDTO.price(),
                giftCertificateRequestDTO.duration());
        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.OK).body(giftCertificate);
        doReturn(responseEntity).when(giftCertificateService).updateGiftCertificate(id, giftCertificateToUpdate, tagIds);

        mockMvc.perform(put("/certificate/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(giftCertificateRequestDTO)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(giftCertificate.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(giftCertificate.getName())))
                .andExpect(jsonPath("$.description", is(giftCertificate.getDescription())))
                .andExpect(jsonPath("$.price", is(giftCertificate.getPrice())))
                .andExpect(jsonPath("$.duration", is(giftCertificate.getDuration().intValue())))
                .andExpect(jsonPath("$.createDate", is(giftCertificate.getCreateDate())))
                .andExpect(jsonPath("$.lastUpdateDate", is(giftCertificate.getLastUpdateDate())))
                .andExpect(jsonPath("$.tags", is(giftCertificate.getTags())))
                .andExpect(status().isOk());
    }
}
