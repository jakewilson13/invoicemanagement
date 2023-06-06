package com.jwconsulting.invoicemanagement.service.impl;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.dto.UserDTOMapper;
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

    private UserDTO mapToUserDto(User user) {
        return fromUser(user, roleRepo.getRoleByUserId(user.getId()));
    }
}
