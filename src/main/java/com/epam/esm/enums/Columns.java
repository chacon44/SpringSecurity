package com.epam.esm.enums;

import lombok.Getter;

@Getter
public enum Columns {

    //CERTIFICATE TABLE
    GIFT_CERTIFICATE_ID("certificate_id"),
    GIFT_CERTIFICATE_NAME("certificate_name"),
    GIFT_CERTIFICATE_DESCRIPTION("description"),
    GIFT_CERTIFICATE_PRICE("price"),
    GIFT_CERTIFICATE_DURATION("duration"),
    GIFT_CERTIFICATE_CREATE_DATE("create_date"),
    GIFT_CERTIFICATE_LAST_UPDATE_DATE("last_update_date"),

    //TAG TABLE
    TAG_TABLE_ID("tag_id"),
    TAG_TABLE_NAME("tag_name");

    private final String column;
    Columns(String column) {
        this.column = column;
    }

    @Override
    public String toString() {
        return this.getColumn();
    }
}
