package esm.service;

import static com.epam.esm.exceptions.Codes.TAG_BAD_REQUEST;
import static com.epam.esm.exceptions.Codes.TAG_NOT_FOUND;
import static com.epam.esm.exceptions.Messages.*;
import static org.springframework.http.HttpStatus.*;

import com.epam.esm.Dto.Errors.ErrorDTO;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.GiftCertificateTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TagService {

    private final GiftCertificateTagRepository giftCertificateTagRepository;


    @Autowired
    public TagService(GiftCertificateTagRepository giftCertificateTagRepository) {
        this.giftCertificateTagRepository = giftCertificateTagRepository;
    }

    /**
     *
     * @param tagName name of the tag to be saved.
     * @return
     * if tag name is not valid, returns bad request
     * if tag already exists, returns bad request
     * if tag does not exist, but cannot be saved, return bad request
     * if it is saved, return CREATED and tag saved
     */
    public ResponseEntity<?> saveTag(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Tag name is required", TAG_BAD_REQUEST));
        }
        Tag tag = giftCertificateTagRepository.getTagByName(tagName);

        if (tag != null) {

            String message = TAG_ALREADY_EXISTS.formatted(tag.getId());
            return ResponseEntity.badRequest().body(new ErrorDTO(message, TAG_BAD_REQUEST));
        }

        Tag tagSaved = giftCertificateTagRepository.saveTag(tagName);
        if (tagSaved == null) {
            return ResponseEntity.status(BAD_REQUEST).body(new ErrorDTO(TAG_COULD_NOT_BE_SAVED, TAG_BAD_REQUEST));
        }

        Tag tagResponse = giftCertificateTagRepository.getTagById(tagSaved.getId());

        return ResponseEntity.status(CREATED).body(tagResponse);
    }

    /**
     *
     * @param tagId unique tag id
     * @return if tag is retrieved, returns tag
     * if not, returns not found
     */
    public ResponseEntity<?> getTag(long tagId) {
        Tag tag = giftCertificateTagRepository.getTagById(tagId);

        if (tag != null) {
            return ResponseEntity.status(FOUND).body(tag);
        } else {
            String message = TAG_ID_NOT_FOUND.formatted(tagId);

            ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
            return ResponseEntity.status(NOT_FOUND).body(errorResponse);
        }
    }

    /**
     *
     * @param tagId unique tag id
     * @return
     * if tag has been deleted, returns found
     * if not, returns not found
     */
    public ResponseEntity<?> deleteTag(long tagId) {

        boolean tagSuccessfullyDeleted = giftCertificateTagRepository.deleteTag(tagId);
        if (tagSuccessfullyDeleted) {
            return ResponseEntity.status(FOUND).body(null);
        }

        String message = TAG_ID_NOT_FOUND.formatted(tagId);
        ErrorDTO errorResponse = new ErrorDTO(message, TAG_NOT_FOUND);
        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
    }
}
