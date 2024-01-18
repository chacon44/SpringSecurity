package com.epam.esm.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {

  TAG("_01"),
  CERTIFICATE("_02"),
  USER("_03"),
  ORDER("_04"),

  //TAGS
  TAG_DATABASE_ERROR("400"+TAG.errorCode),
  TAG_NOT_FOUND("404"+TAG.errorCode),
  TAG_INTERNAL_SERVER_ERROR("500"+TAG.errorCode),
  TAG_BAD_REQUEST("402"+TAG.errorCode),

  //CERTIFICATE
  CERTIFICATE_ALREADY_FOUND("200"+CERTIFICATE.errorCode),
  CERTIFICATE_DATABASE_ERROR("400"+CERTIFICATE.errorCode),
  CERTIFICATE_NOT_FOUND("404"+CERTIFICATE.errorCode),
  CERTIFICATE_INTERNAL_SERVER_ERROR("500"+CERTIFICATE.errorCode),
  CERTIFICATE_BAD_REQUEST("402"+CERTIFICATE.errorCode),

  //USER
  USER_DATABASE_ERROR("400"+USER.errorCode),
  USER_NOT_FOUND("404"+USER.errorCode),
  USER_INTERNAL_SERVER_ERROR("500"+USER.errorCode),
  USER_BAD_REQUEST("402"+USER.errorCode),

  //ORDER
  ORDER_DATABASE_ERROR("400"+ORDER.errorCode),
  ORDER_NOT_FOUND("404"+ORDER.errorCode),
  ORDER_INTERNAL_SERVER_ERROR("500"+ORDER.errorCode),
  ORDER_BAD_REQUEST("402"+ORDER.errorCode),
  ORDER_CONVERSION_ERROR("405"+ORDER.errorCode),

  //GENERAL
  DATABASE_ERROR("400");
  private final String errorCode;

  ErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

}
