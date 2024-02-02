package service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.epam.esm.dto.TagResponseDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.service.TagService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    public static final String TAG_NAME = "name";
    public static final Long TAG_ID = 1L;

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    Tag tag;

    @Test
    public void givenNewTagName_whenSaveTag_thenTagIsSavedAndReturned() {
        // Given
        given(tag.getId()).willReturn(TAG_ID);
        given(tag.getName()).willReturn(TAG_NAME);
        given(tagRepository.findByName(TAG_NAME)).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willReturn(tag);

        // When
        TagResponseDTO actual = tagService.saveTag(TAG_NAME);

        // Then
        verify(tagRepository).save(any(Tag.class));
        assertEquals(TAG_NAME, actual.name());
        assertEquals(TAG_ID, actual.id());
    }
    @Test
    public void saveTag_nullOrEmptyTagName_throwsException() {
        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.saveTag(null));
        assertEquals("Tag name cannot be empty", exception.getMessage());
        assertEquals(ErrorCode.TAG_BAD_REQUEST, exception.getCode());

        exception = assertThrows(CustomizedException.class, () -> tagService.saveTag(""));
        assertEquals("Tag name cannot be empty", exception.getMessage());
        assertEquals(ErrorCode.TAG_BAD_REQUEST, exception.getCode());
    }

    @Test
    public void saveTag_tagAlreadyExists_throwsException() {
        Mockito.when(tagRepository.findByName(TAG_NAME)).thenReturn(Optional.of(tag));
        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.saveTag(TAG_NAME));
        assertTrue(exception.getMessage().startsWith("This tag already exists in id"));
        assertEquals(ErrorCode.TAG_ALREADY_EXISTS, exception.getCode());
    }

    @Test
    public void saveTag_dataAccessExceptionThrown_throwsException() {
        Mockito.when(tagRepository.save(any(Tag.class))).thenThrow(new EmptyResultDataAccessException(1));
        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.saveTag(TAG_NAME));
        assertEquals("Tag could not be saved", exception.getMessage());
        assertEquals(ErrorCode.TAG_DATABASE_ERROR, exception.getCode());
    }

    @Test
    public void getMostUsedTag_tagExists_returnsTag() {

        Mockito.when(tag.getId()).thenReturn(TAG_ID);
        Mockito.when(tagRepository.findMostUsedTagOfUserWithHighestTotalCostOfOrders()).thenReturn(Optional.of(TAG_ID));
        Mockito.when(tagRepository.getReferenceById(TAG_ID)).thenReturn(tag);

        TagResponseDTO actual = tagService.getMostUsedTag();

        verify(tagRepository).getReferenceById(TAG_ID);
        assertEquals(TAG_ID, actual.id());
    }

    @Test
    public void getMostUsedTag_noTag_returnsNull() {
        Mockito.when(tagRepository.findMostUsedTagOfUserWithHighestTotalCostOfOrders()).thenReturn(Optional.empty());

        TagResponseDTO actual = tagService.getMostUsedTag();

        assertNull(actual);
    }

    @Test
    public void getMostUsedTag_dataAccessExceptionThrown_throwsException() {
        Mockito.when(tagRepository.findMostUsedTagOfUserWithHighestTotalCostOfOrders())
            .thenThrow(new EmptyResultDataAccessException(1));

        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.getMostUsedTag());

        assertEquals("Database error while fetching tag", exception.getMessage());
        assertEquals(ErrorCode.TAG_DATABASE_ERROR, exception.getCode());
    }

    @Test
    public void getAllTags_fetchesTags_noError() {
        Mockito.when(tag.getId()).thenReturn(TAG_ID);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> page = new PageImpl<>(Collections.singletonList(tag));

        Mockito.when(tagRepository.findAll(pageable)).thenReturn(page);

        Page<TagResponseDTO> results = tagService.getAllTags(pageable);

        assertEquals(1, results.getNumberOfElements());
        assertEquals(TAG_ID, results.getContent().get(0).id());
    }
    @Test
    public void getAllTags_dataAccessExceptionThrown_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(tagRepository.findAll(pageable)).thenThrow(new EmptyResultDataAccessException(1));

        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.getAllTags(pageable));

        assertEquals("Failed to fetch tags from the database", exception.getMessage());
        assertEquals(ErrorCode.TAG_DATABASE_ERROR, exception.getCode());
    }
    @Test
    public void deleteTag_existingTag_noError() {
        Mockito.when(tagRepository.existsById(TAG_ID)).thenReturn(true);

        assertDoesNotThrow(() -> tagService.deleteTag(TAG_ID));
    }

    @Test
    public void deleteTag_nonExistingTag_throwsException() {
        Mockito.when(tagRepository.existsById(TAG_ID)).thenReturn(false);

        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.deleteTag(TAG_ID));
        assertEquals("Could not find a tag with id " + TAG_ID, exception.getMessage());
        assertEquals(ErrorCode.TAG_NOT_FOUND, exception.getCode());
    }

    @Test
    public void deleteTag_dataAccessExceptionThrown_throwsException() {
        Mockito.when(tagRepository.existsById(TAG_ID)).thenReturn(true);
        Mockito.doThrow(new EmptyResultDataAccessException(1)).when(tagRepository).deleteById(TAG_ID);

        CustomizedException exception = assertThrows(CustomizedException.class, () -> tagService.deleteTag(TAG_ID));
        assertEquals("Database error during deleting tag with id " + TAG_ID, exception.getMessage());
        assertEquals(ErrorCode.TAG_DATABASE_ERROR, exception.getCode());
    }
}
