package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.esm.controller.TagsController;
import com.epam.esm.dto.TagRequestDTO;
import com.epam.esm.dto.TagResponseDTO;
import com.epam.esm.model.Tag;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class TagsControllerTest {

  @InjectMocks
  private TagsController tagsController;

  @Mock
  private TagService tagService;

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
    this.mockMvc = MockMvcBuilders.standaloneSetup(tagsController).build();
  }

  @Test
  public void testPostTag() throws Exception {
    // Given
    TagRequestDTO tagRequest = new TagRequestDTO("Tag1");
    TagResponseDTO tagResponse = new TagResponseDTO(1L, "Tag1");

    // Mocks
    when(tagService.saveTag(any(String.class))).thenReturn(tagResponse);

    // When & Then
    mockMvc.perform(post("/tag")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(tagRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Tag1"));
  }

  @Test
  public void testGetAllTags() throws Exception {
    // Given
    List<TagResponseDTO> tags = List.of(new TagResponseDTO(1L, "Tag1"));
    Page<TagResponseDTO> tagDTOPage = new PageImpl<>(tags);

    // Mocks
    when(tagService.getAllTags(any())).thenReturn(tagDTOPage);

    // When & Then
    mockMvc.perform(get("/tag"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Tag1")));
  }

  @Test
  public void testDeleteTagById() throws Exception {

    mockMvc.perform(delete("/tag/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  public void testGetTagRevisions() throws Exception {
    // Given
    long id = 1L;

    Tag sampleTag = new Tag();
    sampleTag.setId(id);
    sampleTag.setName("SampleTag");

    // Mocks
    when(auditReaderService.getReader()).thenReturn(auditReader);
    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(Tag.class, true, true))
        .thenReturn(auditQuery);
    when(auditReader.getRevisions(Tag.class, id)).thenReturn(Arrays.asList(1, 2));
    when(auditReader.find(Tag.class, id, 1)).thenReturn(sampleTag);
    when(auditReader.find(Tag.class, id, 2)).thenReturn(sampleTag);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders.get("/tag/" + id + "/revisions")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].id").value(id))
        .andExpect(jsonPath("$.[0].name").value("SampleTag"));
  }
}
