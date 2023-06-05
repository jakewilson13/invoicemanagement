package com.jwconsulting.invoicemanagement.service;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.model.User;

public interface UserService {
    UserDTO createUser(User user);
    UserDTO getUserByEmail(String email);
}
