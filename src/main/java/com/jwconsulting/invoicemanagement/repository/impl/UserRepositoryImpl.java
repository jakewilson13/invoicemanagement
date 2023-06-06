package com.jwconsulting.invoicemanagement.repository.impl;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.exception.ApiException;
import com.jwconsulting.invoicemanagement.model.Role;
import com.jwconsulting.invoicemanagement.model.User;
import com.jwconsulting.invoicemanagement.model.UserPrincipal;
import com.jwconsulting.invoicemanagement.repository.RoleRepository;
import com.jwconsulting.invoicemanagement.repository.UserRepository;
import com.jwconsulting.invoicemanagement.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import static com.jwconsulting.invoicemanagement.query.UserQuery.*;
import static com.jwconsulting.invoicemanagement.utils.SMSUtils.sendSMS;
import static java.util.Objects.requireNonNull;
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
        return null;
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
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()).getPermission());
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
        String expirationDate = DateFormatUtils.format(addDays(new Date(), 1), DATE_FORMAT);    //1 day
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

    private Boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_DATE_QUERY, Map.of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            throw new ApiException("This code is not valid. Please login again.");
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

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString(); //whatever url the server is running on
    }
}
