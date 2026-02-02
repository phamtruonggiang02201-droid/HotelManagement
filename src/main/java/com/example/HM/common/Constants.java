package com.example.HM.common;

public class Constants {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String SORT_DIRECTION_ASC = "asc";
    public static final String SORT_DIRECTION_DESC = "desc";

    // Validation Regex
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9._]{3,20}$";
    public static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String REGEX_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    public static final String REGEX_FULLNAME = "^[\\p{L} ]{2,50}$";
    public static final String REGEX_PHONE = "^[0-9]{10,11}$";
    public static final String REGEX_ID_NUMBER = "^[a-zA-Z0-9]{8,20}$";
    public static final String REGEX_NATIONALITY = "^[\\p{L} ]{2,30}$";
}
