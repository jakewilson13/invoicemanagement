package com.jwconsulting.invoicemanagement.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordForm {
    @NotEmpty(message = "New password field cannot be empty.")
    private String password;
    @NotEmpty(message = "Confirm password field cannot be empty.")
    private String confirmPassword;
}
