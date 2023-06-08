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
    public static final String DELETE_PASSWORD_VERIFICATION_BY_ID_QUERY = "DELETE FROM resetpasswordverifications WHERE user_id = :userId";
    public static final String INSERT_PASSWORD_VERIFICATION_QUERY = "INSERT INTO resetpasswordverifications (user_id, url, expiration_date) VALUES (:userId, :url, :expirationDate)";
    public static final String SELECT_EXPIRATION_BY_URL = "SELECT expiration_date < NOW () AS is_expired FROM resetpasswordverifications WHERE url = :url";
    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY = "SELECT * FROM users WHERE id = (SELECT user_id FROM resetpasswordverifications WHERE url = :url)";
    public static final String UPDATE_USER_PASSWORD_BY_URL_QUERY = "UPDATE users SET password = :password WHERE id = (SELECT user_id FROM resetpasswordverifications WHERE url = :url)";
    public static final String DELETE_VERIFICATION_BY_URL_QUERY = "DELETE FROM resetpasswordverifications WHERE url = :url";
    public static final String SELECT_USER_BY_ACCOUNT_URL_QUERY = "SELECT * FROM users WHERE id = (SELECT user_id FROM accountverifications WHERE url = :url)";
    public static final String UPDATE_USER_ENABLED_QUERY = "UPDATE users SET enabled = :enabled WHERE id = :id";
}
