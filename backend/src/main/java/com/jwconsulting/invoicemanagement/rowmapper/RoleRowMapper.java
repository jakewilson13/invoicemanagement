package com.jwconsulting.invoicemanagement.rowmapper;

import com.jwconsulting.invoicemanagement.model.Role;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleRowMapper implements RowMapper<Role> {
    /**
     mapping all of our rows in the database to a java object.
     just creates constructor and then passes in the values using the getters/setters.
     **/
    @Override
    public Role mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Role.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .permission(resultSet.getString("permission"))
                .build();
    }
}
