package com.jwconsulting.invoicemanagement.repository.impl;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.enumeration.VerificationType;
import com.jwconsulting.invoicemanagement.exception.ApiException;
import com.jwconsulting.invoicemanagement.form.UpdateForm;
import com.jwconsulting.invoicemanagement.model.Role;
import com.jwconsulting.invoicemanagement.model.User;
import com.jwconsulting.invoicemanagement.model.UserPrincipal;
import com.jwconsulting.invoicemanagement.repository.RoleRepository;
import com.jwconsulting.invoicemanagement.repository.UserRepository;
import com.jwconsulting.invoicemanagement.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.jwconsulting.invoicemanagement.enumeration.RoleType.ROLE_USER;
import static com.jwconsulting.invoicemanagement.enumeration.VerificationType.ACCOUNT;
import static com.jwconsulting.invoicemanagement.enumeration.VerificationType.PASSWORD;
import static com.jwconsulting.invoicemanagement.query.UserQuery.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(User user) {
        int count = getEmailCount(user.getEmail().trim().toLowerCase());
        System.out.println("EMAIL COUNT VALUE: " + count);
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email already in use. Please use a different email and try again.");
        try {
            KeyHolder holder = new GeneratedKeyHolder();    //to access primary key
            SqlParameterSource parameters = getSqlParameterSource(user); //created parameters to send with the request
            jdbc.update(INSERT_USER_QUERY, parameters, holder); //we need to give jdbc the query, the parameters and key holder to update our database
            user.setId(requireNonNull(holder.getKey().longValue()));    //setting the id of the user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());   //add role to user
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));
           // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT.getType());
            user.setEnabled(false);
            user.setNotLocked(true);
            return user;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ApiException("An error occurred. Please try again.");
        }
    }



    @Override
    public Collection list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, Map.of("id", id), new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new ApiException("No User found by id: " + id);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if(user != null) {
            log.info("User found in the database with email: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
        } else {
            log.error("User not found inside of database with email: {}", email);
            throw new UsernameNotFoundException("User not found inside of database.");
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new ApiException("No User found by email: " + email);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);    //1 day
        String verificationCode = RandomStringUtils.randomAlphanumeric(8).toUpperCase();    //generates random 8 digit code consisting of numbers and letters
        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, Map.of("id", user.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, Map.of("userId", user.getId(), "code", verificationCode, "expirationDate", expirationDate));
//          sendSMS(user.getPhone(), "From: InvoiceManagement \nVerification Code\n" + verificationCode);
            log.info("Verification Code: {}", verificationCode);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(code)) throw new ApiException("This code has expired. Please login again.");
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, Map.of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                jdbc.update(DELETE_USER_CODE_QUERY, Map.of("code", code, "email", email));  //upon verification, delete the code so it can only be used once
                return userByCode;
            } else {
                throw new ApiException("Code is invalid. Please try again.");
            }
        } catch(EmptyResultDataAccessException e) {
            throw new ApiException("Unable to find record.");
        } catch(Exception e) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void resetPassword(String email) {
        if(getEmailCount(email.trim().toLowerCase()) <= 0) throw new ApiException("There is no account for this email address.");
        try {
                String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
                User user = getUserByEmail(email);
                String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
                jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_ID_QUERY, Map.of("userId", user.getId()));
                jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, Map.of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
                //TODO: send email with url to user
                log.info("Verification Url: " + verificationUrl);

        } catch(Exception e) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if(isLinkExpired(key, PASSWORD)) throw new ApiException("This link has expired. Please reset your password again.");
        try {
            User user =  jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
//            jdbc.update(DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY), Map.of("id", user.getId());  //click the link once and verify your password. click it again then the link will not be valid.
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.error(e.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        } catch(Exception e) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Passwords do not match. Please try again.");
        try {
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, Map.of("password", encoder.encode(password), "url", getVerificationUrl(key, PASSWORD.getType())));
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, Map.of("url", getVerificationUrl(key, PASSWORD.getType())));
        } catch (EmptyResultDataAccessException e) {
            log.error(e.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        }
    }

    @Override
    public User verifyAccountKey(String key) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, Map.of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            jdbc.update(UPDATE_USER_ENABLED_QUERY, Map.of("enabled", true, "id", user.getId()));
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new ApiException("This link is not valid.");
        } catch(Exception e) {
            throw new ApiException("An error occured. Please try again.");
        }
    }

    @Override
    public User updateUserDetails(UpdateForm user) {
        try {
            jdbc.update(UPDATE_USER_INFORMATION_QUERY, getUserDetailsSqlParametersSource(user));
            return get(user.getId());
        } catch(EmptyResultDataAccessException e) {
            throw new ApiException("No user found by id: " + user.getId());
        }
        catch(Exception e) {
            log.error(e.getMessage());
            throw new ApiException("An error occured. Please try again.");
        }
    }

    private Boolean isLinkExpired(String key, VerificationType password) {
        try {
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, Map.of("url", getVerificationUrl(key, password.getType())), Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            log.error(e.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        } catch(Exception e) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private Boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_DATE_QUERY, Map.of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            throw new ApiException("This code is not valid. Please enter the correct verification code.");
        } catch(Exception e) {
            throw new ApiException("An error occured. Please try again.");
        }
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }
    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }

    private SqlParameterSource getUserDetailsSqlParametersSource(UpdateForm user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone", user.getPhone())
                .addValue("address", user.getAddress())
                .addValue("title", user.getTitle())
                .addValue("bio", user.getBio());
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString(); //whatever url the server is running on
    }
}
