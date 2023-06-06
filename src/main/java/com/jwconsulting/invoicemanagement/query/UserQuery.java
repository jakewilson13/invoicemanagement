package com.jwconsulting.invoicemanagement.query;

public class UserQuery {
    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM users WHERE email = :email";
    public static final String INSERT_USER_QUERY = "INSERT INTO users (first_name, last_name, email, password) VALUES (:firstName, :lastName, :email, :password)"; //values will be whatever we pass in the parameter source method in repository impl or the maps
    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO accountverifications (user_id, url) VALUES (:userId, :url)";
    public static final String SELECT_USER_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = :email";
    public static final String DELETE_VERIFICATION_CODE_BY_USER_ID = "DELETE FROM twofactorverifications WHERE user_id = :id";
    public static final String INSERT_VERIFICATION_CODE_QUERY = "INSERT INTO twofactorverifications (user_id, code, expiration_date) VALUES (:userId, :code, :expirationDate)";
    public static final String SELECT_USER_BY_USER_CODE_QUERY = "SELECT * FROM users WHERE id = (SELECT user_id FROM twofactorverifications WHERE code = :code)";
    public static final String DELETE_USER_CODE_QUERY = "DELETE FROM twofactorverifications WHERE code = :code AND user_id IN (SELECT id FROM users WHERE email = :email)";
    public static final String SELECT_CODE_EXPIRATION_DATE_QUERY = "SELECT expiration_date < NOW () AS is_expired FROM twofactorverifications WHERE code = :code";
}
