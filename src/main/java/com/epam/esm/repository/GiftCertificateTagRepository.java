package com.epam.esm.repository;

import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Tag;
import java.util.List;

public interface GiftCertificateTagRepository {

    /**
    CERTIFICATES
    */

    //SAVE
    GiftCertificate saveGiftCertificate(GiftCertificate giftCertificate, List<Long> tagList);

    //GET
    GiftCertificate getGiftCertificateById(Long certificate_id);
    GiftCertificate getGiftCertificateByName(String giftCertificateName);
    List<GiftCertificate> getCertificatesByTagName(String tagName);
    List<GiftCertificate> searchCertificatesByKeyword(String keyWord);

    //DELETE
    boolean deleteGiftCertificate(long certificate_id);

    //UPDATE
    GiftCertificate updateGiftCertificate(long certificate_id, GiftCertificate giftCertificate, List<Long> tagIds);

    /**
     TAGS
     */

    //SAVE
    Tag saveTag(String tagName);
    //GET
    Tag getTagById(long tag_id);
    Tag getTagByName(String name);
    List<Tag> getTagsListByCertificateId(long certificate_id);
    List<Long> tagIdListByCertificateId(long certificate_id);
    List<GiftCertificate> sortCertificates(List<GiftCertificate> commonList, String nameOrder, String createDateOrder);
    List<GiftCertificate> filterCertificates(String tagName, String searchWord, String nameOrder, String createDateOrder);

    //DELETE
    boolean deleteTag(long tag_id);

    void joinTags(Long giftCertificateId, List<Long> tagIds);
}
