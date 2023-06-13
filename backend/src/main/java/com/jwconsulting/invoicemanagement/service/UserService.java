package com.jwconsulting.invoicemanagement.service;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.form.UpdateForm;
import com.jwconsulting.invoicemanagement.model.User;

public interface UserService {
    UserDTO createUser(User user);
    UserDTO getUserByEmail(String email);
    void sendVerificationCode(UserDTO user);

    UserDTO verifyCode(String email, String code);

    void resetPassword(String email);

    UserDTO verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    UserDTO verifyAccountKey(String key);

    UserDTO updateUserDetails(UpdateForm user);
    UserDTO getUserById(Long userId);
    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);
}
