package com.jwconsulting.invoicemanagement.service;

import com.jwconsulting.invoicemanagement.model.Role;

import java.util.Collection;

public interface RoleService {
    Role getRoleByUserId(Long id);
    Collection<Role> getRoles();
}
