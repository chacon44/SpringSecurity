package controller;
//
//import static org.hamcrest.core.Is.is;
//import static org.mockito.Mockito.doReturn;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.epam.esm.dto.TagRequestDTO;
//import com.epam.esm.controller.TagsController;
//import com.epam.esm.model.Tag;
//import com.epam.esm.service.TagService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//@ExtendWith(MockitoExtension.class)
//public class TagControllerTest {
//
//    @InjectMocks
//    private TagsController tagsController;
//
//    @Mock
//    private TagService tagService;
//
//    private MockMvc mockMvc;
//
//    public static String asJsonString(final Object obj) {
//        try {
//            return new ObjectMapper().writeValueAsString(obj);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @BeforeEach
//    public void setup() {
//        this.mockMvc = MockMvcBuilders.standaloneSetup(this.tagsController).build();
//    }
//
//    @Test
//    public void postTag() throws Exception {
//        // Arrange
//        TagRequestDTO tagRequestDTO = new TagRequestDTO("test tag");
//        Tag tag = new Tag(1L, tagRequestDTO.name());
//
//        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(tag);
//        doReturn(responseEntity).when(tagService).saveTag(tagRequestDTO.name());
//
//        // Act
//        mockMvc.perform(post("/tag")
//                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(tagRequestDTO)))
//                //Assert
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(tag.getId()), Long.class))
//                .andExpect(jsonPath("$.name", is(tag.getName())))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    public void getTag() throws Exception {
//        // Arrange
//        TagRequestDTO tagRequestDTO = new TagRequestDTO("test tag");
//        Tag tag = new Tag(1L, tagRequestDTO.name());
//        Long id = 1L;
//        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.FOUND).body(tag);
//        doReturn(responseEntity).when(tagService).getTag(id);
//
//        // Act
//        mockMvc.perform(get("/tag/{id}", id)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(id)))
//                //Assert
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(tag.getId()), Long.class))
//                .andExpect(jsonPath("$.name", is(tag.getName())))
//                .andExpect(status().isFound());
//    }
//
//    @Test
//    public void deleteTag() throws Exception {
//        // Arrange
//        TagRequestDTO tagRequestDTO = new TagRequestDTO("test tag");
//        Tag tag = new Tag(1L, tagRequestDTO.name());
//        Long id = 1L;
//        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.FOUND).body(tag);
//        doReturn(responseEntity).when(tagService).deleteTag(id);
//
//        // Act
//        mockMvc.perform(delete("/tag/{id}", id)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(id)))
//                //Assert
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(tag.getId()), Long.class))
//                .andExpect(jsonPath("$.name", is(tag.getName())))
//                .andExpect(status().isFound());
//    }
//}
