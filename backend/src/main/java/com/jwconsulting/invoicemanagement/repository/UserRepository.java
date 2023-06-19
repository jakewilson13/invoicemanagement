package com.jwconsulting.invoicemanagement.repository;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.form.UpdateForm;
import com.jwconsulting.invoicemanagement.model.User;

import java.util.Collection;

public interface UserRepository <T extends User> {
    /**Basic CRUD Operations**/
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /**More Complex Operations**/
    User getUserByEmail(String email);
    void sendVerificationCode(UserDTO user);
    User verifyCode(String email, String code);
    void resetPassword(String email);
    T verifyPasswordKey(String key);
    void renewPassword(String key, String password, String confirmPassword);
    T verifyAccountKey(String key);
    T updateUserDetails(UpdateForm user);
    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);
    void updateUserAccountSettings(Long userId, Boolean enabled, Boolean notLocked);
}
