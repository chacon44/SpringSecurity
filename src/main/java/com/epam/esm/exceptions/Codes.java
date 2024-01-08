package com.epam.esm.exceptions;

public class Codes {


    //2XX
    public static final int CERTIFICATE_REQUEST_IS_OK = 200_01;
    public static final int CERTIFICATE_CREATED = 201_01;
    public static final int CERTIFICATE_ACCEPTED = 202_01;

    //3XX
    public static final int CERTIFICATE_FOUND = 302_01;

    public static final int CERTIFICATE_BAD_REQUEST = 400_01;
    public static final int CERTIFICATE_NOT_FOUND = 404_01;

    //2XX
    public static final int TAG_REQUEST_IS_OK = 200_02;
    public static final int TAG_CREATED = 201_02;
    public static final int TAG_ACCEPTED = 202_02;

    //3XX
    public static final int TAG_BAD_REQUEST = 400_02;
    public static final int TAG_NOT_FOUND = 404_02;

}
