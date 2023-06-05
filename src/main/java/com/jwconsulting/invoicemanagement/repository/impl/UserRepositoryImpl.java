package com.jwconsulting.invoicemanagement.repository.impl;

import com.jwconsulting.invoicemanagement.exception.ApiException;
import com.jwconsulting.invoicemanagement.model.Role;
import com.jwconsulting.invoicemanagement.model.User;
import com.jwconsulting.invoicemanagement.repository.RoleRepository;
import com.jwconsulting.invoicemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static com.jwconsulting.invoicemanagement.enumeration.RoleType.ROLE_USER;
import static com.jwconsulting.invoicemanagement.enumeration.VerificationType.ACCOUNT;
import static com.jwconsulting.invoicemanagement.query.UserQuery.*;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {

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
