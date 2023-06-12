package com.jwconsulting.invoicemanagement.utils;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.model.UserPrincipal;
import org.springframework.security.core.Authentication;

public class UserUtils {
    public static UserDTO getAuthenticatedUser(Authentication auth) {
        return ((UserDTO) auth.getPrincipal());
    }

    public static UserDTO getLoggedInUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
