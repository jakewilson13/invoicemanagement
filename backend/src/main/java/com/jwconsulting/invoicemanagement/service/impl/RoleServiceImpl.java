package com.jwconsulting.invoicemanagement.service.impl;

import com.jwconsulting.invoicemanagement.model.Role;
import com.jwconsulting.invoicemanagement.repository.RoleRepository;
import com.jwconsulting.invoicemanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository<Role> roleRepo;
    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepo.getRoleByUserId(id);
    }
}
