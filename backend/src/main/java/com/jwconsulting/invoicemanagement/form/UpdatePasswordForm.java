package com.jwconsulting.invoicemanagement.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Creating this form class to use inside of the request body for a user to reset there password WHILE user is logged in.
 * Cannot leverage existing ResetPasswordForm class because this takes in the current password as a param
 **/
@Getter
@Setter
public class UpdatePasswordForm {
    @NotEmpty(message = "Current password cannot be empty.")
    private String currentPassword;
    @NotEmpty(message = "New password cannot be empty.")
    private String newPassword;
    @NotEmpty(message = "Confirm password cannot be empty.")
    private String confirmNewPassword;
}
