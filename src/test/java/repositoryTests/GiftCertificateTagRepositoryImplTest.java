package repositoryTests;

import com.epam.esm.mapper.GiftCertificateRowMapper;
import com.epam.esm.mapper.TagRowMapper;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.queries.PostgreSqlQueries;
import com.epam.esm.repository.GiftCertificateTagRepository;
import com.epam.esm.repository.GiftCertificateTagRepositoryImpl;
import config.TestRepositoryConfig;
import com.epam.esm.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Main.class)
@TestPropertySource("classpath:application-test.properties")
class GiftCertificateTagRepositoryImplTest {

    @Autowired
    private GiftCertificateTagRepository giftCertificateTagRepository;

    private GiftCertificate giftCertificate = null;

    private List<Long> tagIdsList = List.of();
    private final Long nonExistingId = 1000L;
    private final String nonExistingName = "xDfi4vc#sl";

    Tag tag1 = new Tag();
    Tag tag5 = new Tag();
    private GiftCertificate giftCertificate1 = new GiftCertificate();
    private GiftCertificate giftCertificate2 = new GiftCertificate();
    private GiftCertificate giftCertificate3 = new GiftCertificate();
    private List<GiftCertificate> giftCertificateList = List.of(giftCertificate1, giftCertificate2, giftCertificate3);

    @BeforeEach
    void setUp() {

        giftCertificate = new GiftCertificate(
                "certificate for test", "description for test", 10.50, 10L);

        tag1 = new Tag(1L, "tag 3");
        Tag tag2 = new Tag(2L, "tag 1");
        Tag tag3 = new Tag(3L, "tag 2");
        Tag tag4 = new Tag(4L, "blue");
        tag5 = new Tag(5L, "colour");
        Tag tag6 = new Tag(6L, "animal 1");

        tagIdsList = new ArrayList<>(List.of(1L, 3L, 4L, 5L));

        giftCertificate1.setId(1L);
        giftCertificate1.setName("certificate");
        giftCertificate1.setDescription("description 1");
        giftCertificate1.setPrice(310.00);
        giftCertificate1.setDuration(20L);
        giftCertificate1.setCreateDate("2023-11-21T16:48:04:309Z");
        giftCertificate1.setLastUpdateDate("2023-12-25T16:48:04:309Z");
        giftCertificate1.setTags(List.of(tag1, tag5));

        giftCertificate2.setId(2L);
        giftCertificate2.setName("certificate 2");
        giftCertificate2.setDescription("description 2");
        giftCertificate2.setPrice(372.12);
        giftCertificate2.setDuration(11L);
        giftCertificate2.setCreateDate("2023-11-25T16:28:04:309Z");
        giftCertificate2.setLastUpdateDate("2023-12-20T16:48:04:309Z");
        giftCertificate2.setTags(List.of(tag2, tag6));

        giftCertificate3.setId(3L);
        giftCertificate3.setName("name 3");
        giftCertificate3.setDescription("description three");
        giftCertificate3.setPrice(300.50);
        giftCertificate3.setDuration(23L);
        giftCertificate3.setCreateDate("2023-11-24T16:18:04:309Z");
        giftCertificate3.setLastUpdateDate("2023-12-10T16:48:04:309Z");
        giftCertificate3.setTags(List.of(tag1, tag3, tag4, tag5));
    }

    //CERTIFICATES
    @Test
    void saveAndGetGiftCertificate_correctRequest() {

        GiftCertificate savedGiftCertificate = giftCertificateTagRepository.saveGiftCertificate(giftCertificate, tagIdsList);
        assertNotNull(savedGiftCertificate);
        Long expectedIdSaved = giftCertificateList.size() + 1L;
        assertEquals(expectedIdSaved, savedGiftCertificate.getId());

        GiftCertificate giftCertificateSaved = giftCertificateTagRepository.getGiftCertificateByName(savedGiftCertificate.getName());
        assertNotNull(giftCertificateSaved);
    }

    @Test
    void saveGiftCertificate_AlreadyExistingName() {
        giftCertificate.setName(giftCertificate2.getName());
        GiftCertificate giftCertificateSaved = giftCertificateTagRepository.saveGiftCertificate(giftCertificate1, tagIdsList);
        assertNull(giftCertificateSaved);
    }

    @Test
    void getGiftCertificateById_existingId() {

        GiftCertificate actual = giftCertificateTagRepository.getGiftCertificateById(1L);

        assertNotNull(actual);
        assertEquals(giftCertificate1, actual);
    }

    @Test
    void getGiftCertificateById_nonExistingId() {
        GiftCertificate actual = giftCertificateTagRepository.getGiftCertificateById(nonExistingId);

        assertNull(actual);
    }

    @Test
    void getGiftCertificateByName_existingName() {

        GiftCertificate actual = giftCertificateTagRepository.getGiftCertificateByName("certificate");

        assertNotNull(actual);
        assertEquals(giftCertificate1, actual);
    }

    @Test
    void getGiftCertificateByName_nonExistingName() {

        GiftCertificate actual = giftCertificateTagRepository.getGiftCertificateByName(nonExistingName);
        assertNull(actual);
    }

    @Test
    void getCertificatesByTagName_existingTagName() {

        List<GiftCertificate> expected = List.of(giftCertificate1, giftCertificate3);

        List<GiftCertificate> actual = giftCertificateTagRepository.getCertificatesByTagName("colour");

        assertEquals(expected, actual);
    }

    @Test
    void getCertificatesByTagName_nonExistingTagName() {

        List<GiftCertificate> actual = giftCertificateTagRepository.getCertificatesByTagName(nonExistingName);

        assertTrue(actual.isEmpty());
    }

    @Test
    void getCertificatesBySearchWord_existingWord() {

        List<GiftCertificate> expected = List.of(giftCertificate1, giftCertificate2);
        List<GiftCertificate> actual = giftCertificateTagRepository.searchCertificatesByKeyword("certificate");

        assertEquals(expected, actual);
    }

    @Test
    void getCertificatesBySearchWord_partiallyContainsWord() {

        List<GiftCertificate> expected = List.of(giftCertificate1, giftCertificate2);
        List<GiftCertificate> actual = giftCertificateTagRepository.searchCertificatesByKeyword("cert");

        assertEquals(expected, actual);
    }

    @Test
    void getCertificatesBySearchWord_nonExistingWord() {

        List<GiftCertificate> actual = giftCertificateTagRepository.searchCertificatesByKeyword(nonExistingName);
        assertTrue(actual.isEmpty());
    }

    @Test
    void sortCertificates_sortByNameOnly_ascendantOrder() {

        giftCertificate1.setName("position 2");
        giftCertificate3.setName("position 1");
        giftCertificate2.setName("position 3");

        String nameOrder = "ASC";
        String dateOrder = "";
        List<GiftCertificate> expected = List.of(giftCertificate3, giftCertificate1, giftCertificate2);

        List<GiftCertificate> sorted = giftCertificateTagRepository.sortCertificates(new ArrayList<>(giftCertificateList), nameOrder, dateOrder);
        assertEquals(expected, sorted);
    }

    @Test
    void sortCertificates_sortByNameOnly_descendantOrder() {

        giftCertificate1.setName("position 2");
        giftCertificate3.setName("position 1");
        giftCertificate2.setName("position 3");

        String nameOrder = "DESC";
        String dateOrder = "";
        List<GiftCertificate> expected = List.of(giftCertificate2, giftCertificate1, giftCertificate3);

        List<GiftCertificate> sorted = giftCertificateTagRepository.sortCertificates(new ArrayList<>(giftCertificateList), nameOrder, dateOrder);
        assertEquals(expected, sorted);
    }

    @Test
    void sortCertificates_sortByDateOnly_descendantOrder() {

        String nameOrder = "";
        String dateOrder = "ASC";
        List<GiftCertificate> expected = List.of(giftCertificate1, giftCertificate3, giftCertificate2);

        List<GiftCertificate> sorted = giftCertificateTagRepository.sortCertificates(new ArrayList<>(giftCertificateList), nameOrder, dateOrder);
        assertEquals(expected, sorted);
    }

    @Test
    void sortCertificates_sortByNameAndDate() {

        giftCertificate1.setName("position 2");
        giftCertificate2.setName("position 2");
        giftCertificate3.setName("position 1");

        giftCertificate1.setCreateDate("2021-11-21T16:48:04:309Z");
        giftCertificate2.setCreateDate("2020-11-21T16:48:04:309Z");
        giftCertificate3.setCreateDate("2023-11-21T16:48:04:309Z");

        String nameOrder = "DESC";
        String dateOrder = "ASC";
        List<GiftCertificate> expected = List.of(giftCertificate2, giftCertificate1, giftCertificate3);

        List<GiftCertificate> sorted = giftCertificateTagRepository.sortCertificates(new ArrayList<>(giftCertificateList), nameOrder, dateOrder);
        assertEquals(expected, sorted);
    }

    @Test
    void sortCertificates_noOrdersDefined() {

        List<GiftCertificate> expected = List.of(giftCertificate2, giftCertificate1, giftCertificate3);

        List<GiftCertificate> sorted = giftCertificateTagRepository.sortCertificates(expected, null, null);
        assertEquals(expected, sorted);
    }

    @Test
    void filterCertificates() {

        String nameOrder = "DESC";
        String dateOrder = "ASC";
        List<GiftCertificate> expected = new ArrayList<>(List.of(giftCertificate2));

        List<GiftCertificate> sorted = giftCertificateTagRepository.filterCertificates("tag 1", "certificate", nameOrder, dateOrder);
        assertEquals(expected, sorted);
    }

    @Test
    void deleteCertificate_existingCertificate() {

        assertTrue(giftCertificateTagRepository.deleteGiftCertificate(giftCertificate1.getId()));
    }

    @Test
    void deleteCertificate_nonExistingCertificate() {

        assertFalse(giftCertificateTagRepository.deleteGiftCertificate(nonExistingId));
    }

    @Test
    void updateCertificate_existingCertificate() {

        Long id = giftCertificate1.getId();
        assertNotNull(giftCertificateTagRepository.updateGiftCertificate(id, giftCertificate, tagIdsList));
    }

    @Test
    void updateCertificate_nonExistingCertificate() {

        assertNull(giftCertificateTagRepository.updateGiftCertificate(nonExistingId, giftCertificate, tagIdsList));
    }

    //TAGS
    @Test
    void getTagById_getExistingTag() {

        Tag tag = giftCertificateTagRepository.getTagById(tag5.getId());
        assertNotNull(tag);
    }

    @Test
    void getTagById_getNonExistingTag() {
        Tag tag = giftCertificateTagRepository.getTagById(nonExistingId);
        assertNull(tag);
    }

    @Test
    void tagsByCertificateId_existingCertificateId() {

        List<Long> expectedTagIds = giftCertificate3.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toList());
        List<Long> actualTags = giftCertificateTagRepository.tagIdListByCertificateId(3L);

        assertEquals(expectedTagIds, actualTags);
    }

    @Test
    void tagsByCertificateId_nonExistingCertificateId() {

        List<Long> actualTags = giftCertificateTagRepository.tagIdListByCertificateId(nonExistingId);
        assertTrue(actualTags.isEmpty());
    }

    @Test
    void getTagsListByCertificateId_existingId() {
        List<Tag> expectedTags = giftCertificate1.getTags();

        List<Tag> actualTags = giftCertificateTagRepository.getTagsListByCertificateId(giftCertificate1.getId());

        assertEquals(expectedTags, actualTags);
    }

    @Test
    void getTagsListByCertificateId_nonExistingId() {

        List<Tag> tagsList = giftCertificateTagRepository.getTagsListByCertificateId(nonExistingId);

        assertTrue(tagsList.isEmpty());
    }

    @Test
    void getTagByName_getExistingTagName() {
        Tag actualTag = giftCertificateTagRepository.getTagByName(tag1.getName());
        Tag expectedTag = tag1;

        assertNotNull(actualTag);
        assertEquals(expectedTag, actualTag);
    }

    @Test
    void getTagByName_getNonExistingTagName() {
        Tag tag = giftCertificateTagRepository.getTagByName(nonExistingName);

        assertNull(tag);
    }

    @Test
    void saveTag_validRequest() {

        Tag actual = giftCertificateTagRepository.saveTag(nonExistingName);

        assertNotNull(actual);
        Tag expected = new Tag(7L, nonExistingName);
        assertEquals(expected, actual);
    }

    @Test
    void saveTag_alreadyExistingTag_returnNull() {

        Tag tag = giftCertificateTagRepository.saveTag(tag1.getName());

        assertNull(tag);
    }

    @Test
    void deleteTag_existingTag() {

        assertTrue(giftCertificateTagRepository.deleteTag(tag1.getId()));
    }

    @Test
    void deleteTag_nonExistingTag() {

        assertFalse(giftCertificateTagRepository.deleteTag(nonExistingId));
    }

    @Test
    void joinTags_correctRequest() {

        giftCertificateTagRepository.joinTags(giftCertificate1.getId(), tagIdsList);

        GiftCertificate giftCertificate = giftCertificateTagRepository.getGiftCertificateById(giftCertificate1.getId());

        assertNotNull(giftCertificate);

        List<Tag> savedTags = giftCertificate.getTags();
        List<Long> tagIds = new ArrayList<>();
        for (Tag savedTag : savedTags) tagIds.add(savedTag.getId());

        assertEquals(tagIdsList, tagIds);
    }

    @Test
    void joinTags_nonExistingTags() {

        tagIdsList.add(nonExistingId);

        Exception exception = assertThrows(RuntimeException.class, () ->
                giftCertificateTagRepository
                        .joinTags(giftCertificate1.getId(), tagIdsList));

        String expectedMessage = "Tag with id " + nonExistingId + " not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
