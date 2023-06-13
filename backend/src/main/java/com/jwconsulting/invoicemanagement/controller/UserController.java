package com.jwconsulting.invoicemanagement.controller;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.exception.ApiException;
import com.jwconsulting.invoicemanagement.form.LoginForm;
import com.jwconsulting.invoicemanagement.form.ResetPasswordForm;
import com.jwconsulting.invoicemanagement.form.UpdateForm;
import com.jwconsulting.invoicemanagement.model.HttpResponse;
import com.jwconsulting.invoicemanagement.model.User;
import com.jwconsulting.invoicemanagement.model.UserPrincipal;
import com.jwconsulting.invoicemanagement.provider.TokenProvider;
import com.jwconsulting.invoicemanagement.service.RoleService;
import com.jwconsulting.invoicemanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jwconsulting.invoicemanagement.dto.UserDTOMapper.toUser;
import static com.jwconsulting.invoicemanagement.utils.UserUtils.getAuthenticatedUser;
import static com.jwconsulting.invoicemanagement.utils.UserUtils.getLoggedInUser;
import static java.time.LocalDateTime.now;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/user")
public class UserController {
    private static final String TOKEN_PREFIX = "Bearer ";
    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authManager;
    private final TokenProvider provider;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication) {
        UserDTO user = userService.getUserByEmail(getAuthenticatedUser(authentication).getEmail());
        System.out.println("Authenticated User: " + authentication);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Profile Retrieved")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }
    /**
     * START
     * The methods below is for a user to reset password when a user is not logged into the application.
     * Will display reset password link upon login screen of the application.
     **/
    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDTO user = userService.verifyCode(email, code);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user, "access_token", provider.createAccessToken(getUserPrincipal(user))
                                , "refresh_token", provider.createRefreshToken(getUserPrincipal(user))))
                        .message("Login Successful.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }

    @GetMapping("/reset/password/{email}")
    public ResponseEntity<HttpResponse> profile(@PathVariable("email") String email) {
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent. Please check your email to reset your password at: " + email)
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key) {
        UserDTO user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Please enter a new password.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }
    @PostMapping("/reset/password/{key}")
    public ResponseEntity<HttpResponse> resetPasswordWithUrl(@PathVariable("key") String key, @RequestBody @Valid ResetPasswordForm resetPasswordForm) {
        userService.renewPassword(key, resetPasswordForm.getPassword(), resetPasswordForm.getConfirmPassword());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset successfully.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }
    /**
     * END
     * This is the end of logic to reset a users password when they are not already logged into the application.
     **/

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userService.verifyAccountKey(key).isEnabled() ? "Account already verified" : "Account verified.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if(isHeaderTokenValid(request)) {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length());   //removing bearer  to get just the token
            UserDTO user = userService.getUserById(provider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(Map.of("user", user, "access_token", provider.createAccessToken(getUserPrincipal(user))
                                    , "refresh_token", token))
                            .message("Token refreshed")
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .path(request.getRequestURI())
                            .build());
        } else {
            return ResponseEntity.badRequest().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .message("Refresh Token missing or invalid")
                            .developerMessage("Refresh Token missing or invalid")
                            .status(HttpStatus.BAD_REQUEST)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .path(request.getRequestURI())
                            .build());
        }
    }

    @PatchMapping ("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm user) throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        UserDTO updatedUser = userService.updateUserDetails(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", updatedUser))
                        .message("User Updated Successfully.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }

    @PostMapping(value = "/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        Authentication authentication = authenticate(loginForm.getEmail(), loginForm.getPassword());
        UserDTO user = getLoggedInUser(authentication);
        return user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user);
    }

    @PostMapping(value = "/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
        UserDTO userDTO = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDTO))
                        .message("User created successfully.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Handles WhiteLabel Exception error page that spring defaults to throw.
     * Implementing Error Controller and added config in properties file to default to this exception.
     */
    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError1(HttpServletRequest request) {
        return new ResponseEntity<>(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("No mapping for a " + request.getMethod() + " request for this path on the server.")
                        .status(HttpStatus.NOT_FOUND)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .path(request.getRequestURI())
                        .build(), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO user) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user, "access_token", provider.createAccessToken(getUserPrincipal(user))
                                , "refresh_token", provider.createRefreshToken(getUserPrincipal(user))))
                        .message("Login Successful.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Verification Code Sent.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .path(request.getRequestURI())
                        .build());
    }

    private boolean isHeaderTokenValid(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION) != null
                && request.getHeader(HttpHeaders.AUTHORIZATION).startsWith(TOKEN_PREFIX)
                && provider.isTokenValid(
                        provider.getSubject(request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length()), request),   //getSubject is the email
                        request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length())
        );
    }
    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }

    private UserPrincipal getUserPrincipal(UserDTO user) {
        return new UserPrincipal(toUser(userService.getUserByEmail(user.getEmail())), roleService.getRoleByUserId(user.getId()));
    }

    private Authentication authenticate (String email, String password) {
        try {
            Authentication authentication = authManager.authenticate(unauthenticated(email, password));
            return authentication;
        } catch(Exception e) {
            throw new ApiException(e.getMessage());
        }
    }
}