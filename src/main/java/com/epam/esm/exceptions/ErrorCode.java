package com.epam.esm.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  //TAGS
  TAG_ALREADY_EXISTS("200"+"_01", HttpStatus.FOUND),
  TAG_DATABASE_ERROR("400"+"_01", HttpStatus.SERVICE_UNAVAILABLE),
  TAG_NOT_FOUND("404"+"_01", HttpStatus.NOT_FOUND),
  TAG_INTERNAL_SERVER_ERROR("500"+"_01", HttpStatus.INTERNAL_SERVER_ERROR),
  TAG_BAD_REQUEST("402"+"_01", HttpStatus.BAD_REQUEST),

  //CERTIFICATE
  CERTIFICATE_ALREADY_EXISTS("200"+"_02", HttpStatus.FOUND),
  CERTIFICATE_DATABASE_ERROR("400"+"_02", HttpStatus.SERVICE_UNAVAILABLE),
  CERTIFICATE_NOT_FOUND("404"+"_02", HttpStatus.NOT_FOUND),
  CERTIFICATE_INTERNAL_SERVER_ERROR("500"+"_02", HttpStatus.INTERNAL_SERVER_ERROR),
  CERTIFICATE_BAD_REQUEST("402"+"_02", HttpStatus.BAD_REQUEST),

  //USER
  USER_DATABASE_ERROR("400"+"_03", HttpStatus.SERVICE_UNAVAILABLE),
  USER_NOT_FOUND("404"+"_03", HttpStatus.NOT_FOUND),
  USER_INTERNAL_SERVER_ERROR("500"+"_03", HttpStatus.INTERNAL_SERVER_ERROR),
  USER_BAD_REQUEST("402"+"_03", HttpStatus.BAD_REQUEST),

  //ORDER
  ORDER_DATABASE_ERROR("400"+"_04", HttpStatus.SERVICE_UNAVAILABLE),
  ORDER_NOT_FOUND("404"+"_04", HttpStatus.NOT_FOUND),
  ORDER_INTERNAL_SERVER_ERROR("500"+"_04", HttpStatus.INTERNAL_SERVER_ERROR),
  ORDER_BAD_REQUEST("402"+"_04", HttpStatus.BAD_REQUEST),
  ORDER_CONVERSION_ERROR("405"+"_04", HttpStatus.EXPECTATION_FAILED),

  //GENERAL
  DATABASE_ERROR("400", HttpStatus.SERVICE_UNAVAILABLE);
  private final String errorCode;
  private final HttpStatus status;
  ErrorCode(String errorCode, HttpStatus status) {
    this.errorCode = errorCode;
    this.status = status;
  }

}
