package com.jwconsulting.invoicemanagement.service.impl;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.form.UpdateForm;
import com.jwconsulting.invoicemanagement.model.Role;
import com.jwconsulting.invoicemanagement.model.User;
import com.jwconsulting.invoicemanagement.repository.RoleRepository;
import com.jwconsulting.invoicemanagement.repository.UserRepository;
import com.jwconsulting.invoicemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jwconsulting.invoicemanagement.dto.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepo;
    private final RoleRepository<Role> roleRepo;
    @Override
    public UserDTO createUser(User user) {
        return mapToUserDto(userRepo.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDto(userRepo.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        userRepo.sendVerificationCode(user);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDto(userRepo.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        userRepo.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {

        return mapToUserDto(userRepo.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        userRepo.renewPassword(key, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return mapToUserDto(userRepo.verifyAccountKey(key));
    }

    @Override
    public UserDTO updateUserDetails(UpdateForm user) {
        return mapToUserDto(userRepo.updateUserDetails(user));
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return mapToUserDto(userRepo.get(userId));
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
        userRepo.updatePassword(id, currentPassword, newPassword, confirmNewPassword);
    }
    @Override
    public void updateUserRole(Long userId, String roleName) {
        roleRepo.updateUserRole(userId, roleName);
    }

    @Override
    public void updateUserAcountSettings(Long userId, Boolean enabled, Boolean notLocked) {
        userRepo.updateUserAccountSettings(userId, enabled, notLocked);
    }

    private UserDTO mapToUserDto(User user) {
        return fromUser(user, roleRepo.getRoleByUserId(user.getId()));
    }
}
