package controller;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.esm.Dto.GiftCertificateRequestDTO;
import com.epam.esm.controller.CertificatesController;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.service.CertificateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class CertificatesControllerTest {

    @InjectMocks
    private CertificatesController certificatesController;

    @Mock
    private CertificateService certificateService;

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


        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(giftCertificate);
        doReturn(responseEntity).when(certificateService).saveGiftCertificate(
            any(GiftCertificate.class),
            anyList()
        );
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
        doReturn(responseEntity).when(certificateService).getGiftCertificateById(id);

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


        String tagName = "tag1";
        String searchWord = "name";
        String nameOrder = "ASC";
        String createDateOrder = "ASC";




        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.OK).body(giftCertificate);
        doReturn(responseEntity).when(certificateService).getFilteredCertificates(tagName,searchWord,nameOrder,createDateOrder);

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
        doReturn(responseEntity).when(certificateService).deleteGiftCertificate(id);

        // Act
        mockMvc.perform(delete("/certificate/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(id)))
                //Assert
                .andExpect(status().isFound());
    }

//    @Test
//    public void putCertificate() throws Exception {
//        Long id = 1L;
//
//        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.OK).body(giftCertificate);
//        doReturn(responseEntity).when(giftCertificateService).updateGiftCertificate(
//            any(Long.class),
//            any(GiftCertificate.class),
//            anyList()
//        );
//        mockMvc.perform(put("/certificate/{id}", id)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(giftCertificateRequestDTO)))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(giftCertificate.getId()), Long.class))
//                .andExpect(jsonPath("$.name", is(giftCertificate.getName())))
//                .andExpect(jsonPath("$.description", is(giftCertificate.getDescription())))
//                .andExpect(jsonPath("$.price", is(giftCertificate.getPrice())))
//                .andExpect(jsonPath("$.duration", is(giftCertificate.getDuration().intValue())))
//                .andExpect(jsonPath("$.createDate", is(giftCertificate.getCreateDate())))
//                .andExpect(jsonPath("$.lastUpdateDate", is(giftCertificate.getLastUpdateDate())))
//                .andExpect(jsonPath("$.tags", is(giftCertificate.getTags())))
//                .andExpect(status().isOk());
//    }
}
