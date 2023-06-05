package com.jwconsulting.invoicemanagement.service.impl;

import com.jwconsulting.invoicemanagement.dto.UserDTO;
import com.jwconsulting.invoicemanagement.dto.UserDTOMapper;
import com.jwconsulting.invoicemanagement.model.User;
import com.jwconsulting.invoicemanagement.repository.UserRepository;
import com.jwconsulting.invoicemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepo;
    @Override
    public UserDTO createUser(User user) {
        return UserDTOMapper.fromUser(userRepo.create(user));
    }
}
