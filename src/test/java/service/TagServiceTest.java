package service;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.GiftCertificateTagRepository;
import com.epam.esm.service.TagService;
import com.epam.esm.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static com.epam.esm.exceptions.Codes.TAG_BAD_REQUEST;
import static com.epam.esm.exceptions.Codes.TAG_NOT_FOUND;
import static com.epam.esm.exceptions.Messages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    public static final String TAG_NAME = "name";
    public static final long TAG_ID = 1L;

    @InjectMocks
    private TagService tagService;

    @Mock
    GiftCertificateTagRepository giftCertificateTagRepository;

    @Mock
    Tag tag;

    @Test
    public void saveTag_savedCorrectly_returnCreated() {

        Mockito.when(giftCertificateTagRepository.saveTag(TAG_NAME)).thenReturn(tag);
        Mockito.when(giftCertificateTagRepository.getTagById(TAG_ID)).thenReturn(tag);
        Mockito.when(tag.getId()).thenReturn(TAG_ID);

        ResponseEntity<?> actual = tagService.saveTag(TAG_NAME);
        Mockito.verify(giftCertificateTagRepository).saveTag(Mockito.eq(TAG_NAME));

        assertEquals(CREATED, actual.getStatusCode());
        assertEquals(tag, actual.getBody());
    }

    @Test
    public void saveTag_cannotSaveTag_returnBadRequest() {

        Mockito.when(giftCertificateTagRepository.saveTag(TAG_NAME)).thenReturn(null);

        ResponseEntity<?> actual = tagService.saveTag(TAG_NAME);
        ErrorDTO expected = new ErrorDTO(TAG_COULD_NOT_BE_SAVED, TAG_BAD_REQUEST);
        Mockito.verify(giftCertificateTagRepository).saveTag(
                Mockito.eq(TAG_NAME)
        );

        assertInstanceOf(ErrorDTO.class, actual.getBody());
        ErrorDTO actualBody = (ErrorDTO) actual.getBody();
        assertEquals(expected.errorMessage(),actualBody.errorMessage());
        assertEquals(expected.errorCode(),actualBody.errorCode());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void saveTag_tagAlreadyExists_returnBadRequest() {

        Mockito.when(giftCertificateTagRepository.getTagByName(TAG_NAME)).thenReturn(tag);

        ResponseEntity<?> actual = tagService.saveTag(TAG_NAME);
        String message = TAG_ALREADY_EXISTS.formatted(tag.getId());

        ResponseEntity<ErrorDTO> responseEntity = ResponseEntity.badRequest().body(new ErrorDTO(message, TAG_BAD_REQUEST));
        Mockito.verify(giftCertificateTagRepository).getTagByName(Mockito.eq(TAG_NAME));

        assertEquals(responseEntity.getBody(),actual.getBody());
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
    }

    @Test
    public void saveTag_tagNotValid_returnBadRequest() {

        ResponseEntity<?> actual = tagService.saveTag(null);
        ResponseEntity<ErrorDTO> responseEntity = ResponseEntity.badRequest()
                .body(new ErrorDTO("Tag name is required", TAG_BAD_REQUEST));
        assertEquals(responseEntity.getBody(),actual.getBody());
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
    }

    @Test
    public void getTag_tagExists_returnTag(){

        Mockito.when(giftCertificateTagRepository.getTagById(tag.getId())).thenReturn(tag);

        ResponseEntity<?> retrievedTag = tagService.getTag(tag.getId());
        ResponseEntity<?> expected = ResponseEntity.status(FOUND).body(tag);

        Mockito.verify(giftCertificateTagRepository).getTagById(Mockito.eq(tag.getId()));

        assertEquals(expected,retrievedTag);
    }

    @Test
    public void getTag_tagNotExist_returnNotFound(){

        Mockito.when(giftCertificateTagRepository.getTagById(tag.getId())).thenReturn(null);

        ResponseEntity<?> retrievedTag = tagService.getTag(tag.getId());

        String message = TAG_ID_NOT_FOUND.formatted(tag.getId());

        ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
        ResponseEntity.status(NOT_FOUND).body(errorResponse);
        ResponseEntity<?> expected = ResponseEntity.status(NOT_FOUND).body(errorResponse);

        Mockito.verify(giftCertificateTagRepository).getTagById(Mockito.eq(tag.getId()));

        assertEquals(expected,retrievedTag);
    }

    @Test
    public void deleteTag_tagExists_returnFound(){

        Mockito.when(giftCertificateTagRepository.deleteTag(tag.getId())).thenReturn(true);

        ResponseEntity<?> retrievedTag = tagService.deleteTag(tag.getId());
        ResponseEntity<?> expected = ResponseEntity.status(FOUND).body(null);

        Mockito.verify(giftCertificateTagRepository).deleteTag(Mockito.eq(tag.getId()));

        assertEquals(expected,retrievedTag);
    }

    @Test
    public void deleteTag_tagExists_returnNotFound(){

        Mockito.when(giftCertificateTagRepository.deleteTag(tag.getId())).thenReturn(false);

        ResponseEntity<?> retrievedTag = tagService.deleteTag(tag.getId());

        String message = TAG_ID_NOT_FOUND.formatted(tag.getId());
        ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
        ResponseEntity<?> expected = ResponseEntity.status(NOT_FOUND).body(errorResponse);

        Mockito.verify(giftCertificateTagRepository).deleteTag(Mockito.eq(tag.getId()));

        assertEquals(expected,retrievedTag);
    }
}
