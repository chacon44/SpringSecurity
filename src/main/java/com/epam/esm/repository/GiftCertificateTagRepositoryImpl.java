package com.epam.esm.repository;

import static com.epam.esm.enums.Columns.GIFT_CERTIFICATE_ID;
import static com.epam.esm.enums.Columns.TAG_TABLE_ID;
import static com.epam.esm.exceptions.Messages.*;
import static com.epam.esm.logs.LogMessages.*;
import static com.epam.esm.queries.PostgreSqlQueries.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import com.epam.esm.enums.Columns;
import com.epam.esm.mapper.GiftCertificateRowMapper;
import com.epam.esm.mapper.TagRowMapper;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import com.epam.esm.queries.PostgreSqlQueries;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class GiftCertificateTagRepositoryImpl implements GiftCertificateTagRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TagRowMapper tagRowMapper;
    private final GiftCertificateRowMapper certificateRowMapper;

    private final PostgreSqlQueries queries;

    public GiftCertificateTagRepositoryImpl(JdbcTemplate jdbcTemplate, TagRowMapper tagRowMapper, GiftCertificateRowMapper certificateRowMapper, PostgreSqlQueries queries) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagRowMapper = tagRowMapper;
        this.certificateRowMapper = certificateRowMapper;
        this.queries = queries;
    }

    /**
     * @param giftCertificate cannot be null or empty
     * @param tagList         can be empty
     * @return null if certificate name already exists
     * certificate if it does not exist
     */
    @Override
    public GiftCertificate saveGiftCertificate(GiftCertificate giftCertificate, List<Long> tagList) {
        log.info(SAVING_GIFT_CERTIFICATE);

        boolean certificateNameExists = getGiftCertificateByName(giftCertificate.getName()) != null;
        if (certificateNameExists)
            return null;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(queries.getSaveGiftCertificate(), RETURN_GENERATED_KEYS);

                    ps.setString(1, giftCertificate.getName());
                    ps.setString(2, giftCertificate.getDescription());
                    ps.setDouble(3, giftCertificate.getPrice());
                    ps.setLong(4, giftCertificate.getDuration());

                    return ps;
                }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();

        // Get the generated certificate ID and return a new Tag object
        Long id = ((Number) Objects.requireNonNull(keys).get(GIFT_CERTIFICATE_ID.getColumn())).longValue();
        giftCertificate.setId(id);
        joinTags(id, tagList);
        return giftCertificate;
    }

    /**
     * @param giftCertificateId cannot be empty or null
     * @return certificate if id exists
     * null if not
     */
    @Override
    public GiftCertificate getGiftCertificateById(@NonNull Long giftCertificateId) {
        log.info(GETTING_GIFT_CERTIFICATES_BY_ID, giftCertificateId);

        try {
            GiftCertificate giftCertificate = jdbcTemplate.queryForObject(GET_GIFT_CERTIFICATE_BY_ID, certificateRowMapper, giftCertificateId);

            List<Tag> tagsToAdd = getTagsListByCertificateId(giftCertificateId);
            Objects.requireNonNull(giftCertificate).setTags(tagsToAdd);
            return giftCertificate;
        } catch (EmptyResultDataAccessException e) {
            log.warn(CERTIFICATE_WITH_ID_NOT_FOUND.formatted(giftCertificateId));
            return null;
        }
    }

    /**
     * @param giftCertificateName unique name associated to certificate
     * @return certificate if name exists
     * null if not
     */
    @Override
    public GiftCertificate getGiftCertificateByName(String giftCertificateName) {
        log.info(GETTING_GIFT_CERTIFICATE_BY_NAME, giftCertificateName);
        try {
            GiftCertificate giftCertificate = jdbcTemplate.queryForObject(GET_GIFT_CERTIFICATE_BY_NAME, certificateRowMapper, giftCertificateName);
            List<Tag> tagsToAdd = getTagsListByCertificateId(Objects.requireNonNull(giftCertificate).getId());
            giftCertificate.setTags(tagsToAdd);

            return giftCertificate;
        } catch (EmptyResultDataAccessException e) {
            log.warn(CERTIFICATE_WITH_NAME_NOT_FOUND.formatted(giftCertificateName));
            return null;
        }
    }

    /**
     * @param tagName unique name associated to tag
     * @return tag if name exists
     * an empty list if not
     */
    @Override
    public List<GiftCertificate> getCertificatesByTagName(String tagName) {

        Tag tag = getTagByName(tagName);
        if (tag != null) {
            log.info(TAG_FOUND.formatted(tagName, tag.getId()));

            List<Long> giftCertificates = jdbcTemplate.query(
                    GET_CERTIFICATES_BY_TAG_ID,
                    (rs, rowNum) -> rs.getLong(GIFT_CERTIFICATE_ID.getColumn()),
                    tag.getId());

            return giftCertificates.stream().map(this::getGiftCertificateById).collect(toList());
        }
        return List.of();
    }

    /**
     *
     * @param keyword word to search in name or description
     * @return
     * the list of certificates can be empty
     */
    @Override
    public List<GiftCertificate> searchCertificatesByKeyword(String keyword) {

        String searchTerm = "%" + keyword + "%";

        List<GiftCertificate> giftCertificates = jdbcTemplate.query(
                GET_GIFT_CERTIFICATE_BY_SEARCH_WORD,
                certificateRowMapper, searchTerm, searchTerm);

        return giftCertificates.stream().map(giftCertificate -> getGiftCertificateById(giftCertificate.getId())).collect(toList());
    }

    /**
     *
     * @param commonList list obtained after filtering by keyword and tag name
     * @param nameOrder order by name
     * @param createDateOrder order by creation date
     * @return list of certificates
     */
    @Override
    public List<GiftCertificate> sortCertificates(List<GiftCertificate> commonList, String nameOrder, String createDateOrder) {

        List<String> order = List.of("ASC", "DESC");
        boolean hasToBeOrderedByName = nameOrder != null && order.contains(nameOrder);
        boolean hasToBeOrderedByCreateDate = createDateOrder != null && order.contains(createDateOrder);

        Comparator<GiftCertificate> nameComparator = null;
        Comparator<GiftCertificate> dateComparator = null;

        if (hasToBeOrderedByName) {
            log.info(SORTING_CERTIFICATES_BY_NAME);
            nameComparator = nameOrder.equals("ASC") ?
                    comparing(GiftCertificate::getName) :
                    comparing(GiftCertificate::getName).reversed();

        }
        if (hasToBeOrderedByCreateDate) {
            log.info(SORTING_CERTIFICATES_BY_CREATE_DATE);
            dateComparator = createDateOrder.equals("ASC") ?
                    comparing(GiftCertificate::getCreateDate) :
                    comparing(GiftCertificate::getCreateDate).reversed();
        }

        if (hasToBeOrderedByName) {
            if (hasToBeOrderedByCreateDate)
                commonList.sort(nameComparator.thenComparing(dateComparator));
            else
                commonList.sort(nameComparator);
        } else if (hasToBeOrderedByCreateDate)
            commonList.sort(dateComparator);

        return commonList;
    }

    /**
     *
     * @param tagName unique name associated to tag
     * @param searchWord word that has to be searched in name or description
     * @param nameOrder order by name
     * @param createDateOrder order by creation date
     * @return list of certificates
     */
    @Override
    public List<GiftCertificate> filterCertificates(String tagName, String searchWord, String nameOrder, String createDateOrder) {

        List<GiftCertificate> commonList = new ArrayList<>();
        if (tagName != null && !tagName.isEmpty()) {
            commonList.addAll(getCertificatesByTagName(tagName));
            if (searchWord != null && !searchWord.isEmpty())
                commonList.retainAll(searchCertificatesByKeyword(searchWord));
        } else if (searchWord != null && !searchWord.isEmpty()) {
            commonList.addAll(searchCertificatesByKeyword(searchWord));
        }

        return sortCertificates(commonList, nameOrder, createDateOrder);
    }

    /**
     *
     * @param giftCertificateId unique id associated to certificate
     * @return
     * true if the certificate was deleted, false if not
     */
    @Override
    public boolean deleteGiftCertificate(long giftCertificateId) {
        log.info(DELETING_GIFT_CERTIFICATE_BY_ID, giftCertificateId);
        jdbcTemplate.update(DELETE_CERTIFICATE_FROM_JOINT_TABLE, giftCertificateId);

        return jdbcTemplate.update(DELETE_GIFT_CERTIFICATE_BY_ID, giftCertificateId) > 0;
    }

    /**
     * @param certificate_id unique id associated to certificate
     * @param giftCertificate certificate containing new data to be updated
     * @param tagIds list of unique ids associated to tags
     * @return
     * null if some tags do not exist, or certificate does not exist
     * updated certificate if it is updated
     */
    @Override
    public GiftCertificate updateGiftCertificate(long certificate_id, GiftCertificate giftCertificate, List<Long> tagIds) {
        log.info(UPDATING_GIFT_CERTIFICATE, certificate_id);

        boolean thereAreNonExistingTags = !filterValidTags(tagIds);
        boolean certificateDoesNotExist = getGiftCertificateById(certificate_id) == null;
        if (thereAreNonExistingTags || certificateDoesNotExist) {
            return null;
        }

        joinTags(certificate_id, tagIds);
        jdbcTemplate.update(queries.getUpdateGiftCertificate(),
                giftCertificate.getName(),
                giftCertificate.getDescription(),
                giftCertificate.getPrice(),
                giftCertificate.getDuration(),
                certificate_id);

        return getGiftCertificateById(certificate_id);
    }

    /**
     *
     * @param tagId unique id associated to tag
     * @return
     * Tag if it exists
     * null if not
     */
    @Override
    public Tag getTagById(long tagId) {
        log.info(GETTING_TAG_BY_ID, tagId);
        try {
            return jdbcTemplate.queryForObject(GET_TAG_BY_ID, tagRowMapper, tagId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(TAG_ID_NOT_FOUND.formatted(tagId));
            return null;
        }
    }

    /**
     * @param certificate_id unique id associated to certificate
     * @return
     * tag ids list associated to certificate id. Can be empty
     *
     */
    @Override
    public List<Long> tagIdListByCertificateId(long certificate_id) {
        log.info(GETTING_TAG_IDS_BY_CERTIFICATE_ID, certificate_id);
        return jdbcTemplate.query(
                GET_TAGS_BY_CERTIFICATE_ID,
                (rs, rowNum) -> rs.getLong(Columns.TAG_TABLE_ID.getColumn()),
                certificate_id);
    }

    /**
     * @param certificate_id unique id associated to certificate
     * @return tags list associated to certificate id. Can be empty
     */
    @Override
    public List<Tag> getTagsListByCertificateId(long certificate_id) {
        return tagIdListByCertificateId(certificate_id).stream()
                .map(this::getTagById)
                .filter(Objects::nonNull)
                .map(tag -> new Tag(tag.getId(), tag.getName()))
                .collect(toList());
    }

    /**
     *
     * @param tagName unique name associated to tag
     * @return
     * tag if name exists
     * null if not
     */
    @Override
    public Tag getTagByName(String tagName) {
        log.info(GETTING_TAG_BY_NAME, tagName);
        try {
            return jdbcTemplate.queryForObject(GET_TAG_BY_NAME, tagRowMapper, tagName);
        } catch (EmptyResultDataAccessException e) {
            log.warn(TAG_NAME_NOT_FOUND.formatted(tagName));
            return null;
        }
    }

    /**
     *
     * @param tagName unique name associated to tag
     * @return
     * tag if name does not exist
     * null if it already exists
     */
    @Override
    public Tag saveTag(@NonNull String tagName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        if(getTagByName(tagName) == null) {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SAVE_TAG, RETURN_GENERATED_KEYS);
                ps.setString(1, tagName);
                return ps;
            }, keyHolder);

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null) {
                Long id = ((Number) keys.get(TAG_TABLE_ID.getColumn())).longValue();
                return new Tag(id, tagName);
            } else return null;
        }
        else return null;
    }

    /**
     *
     * @param tagId unique id associated to tag
     * @return
     * true if tag is deleted
     * false if not
     */
    @Override
    public boolean deleteTag(long tagId) {
        log.info(DELETING_TAG_BY_ID, tagId);
        jdbcTemplate.update(DELETE_TAG_FROM_JOINT_TABLE, tagId);
        return jdbcTemplate.update(DELETE_TAG_BY_ID, tagId) > 0;
    }

    /**
     *
     * @param giftCertificateId
     * cannot be null
     * @param tagIds list of unique ids associated to tag
     * @throws RuntimeException if any tag does not exist
     */
    @Override
    public void joinTags(@NonNull Long giftCertificateId, List<Long> tagIds) throws RuntimeException {

        log.info("Joining tags to gift certificate with id {}", giftCertificateId);

        jdbcTemplate.update(DELETE_CERTIFICATE_FROM_JOINT_TABLE, giftCertificateId);
        tagIds.forEach(tagId -> {
            Tag tag = getTagById(tagId);
            if (tag == null) {
                throw new RuntimeException("Tag with id " + tagId + " not found");
            }
            jdbcTemplate.update(SAVE_TAGS_TO_GIFT_CERTIFICATES, giftCertificateId, tagId);
        });
    }

    private boolean filterValidTags(List<Long> tagIds) {
        log.info("Validating tags");

        return tagIds.stream().allMatch(tagId -> getTagById(tagId) != null);
    }
}

