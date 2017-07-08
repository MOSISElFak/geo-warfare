package rs.elfak.jajac.geowarfare.utils;

import android.widget.EditText;

import org.apache.commons.validator.routines.EmailValidator;

import rs.elfak.jajac.geowarfare.App;
import rs.elfak.jajac.geowarfare.R;

public class Validator {

    public static void validateEmail(EditText emailEt) {
        String email = emailEt.getText().toString().trim();

        if (email.isEmpty()) {
            emailEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!EmailValidator.getInstance().isValid(email)) {
            emailEt.setError(App.getContext().getString(R.string.email_bad_format_error));
        } else {
            emailEt.setError(null);
        }
    }

    public static void validatePassword(EditText passwordEt) {
        String password = passwordEt.getText().toString().trim();
        // Minimum 8 characters, at least one letter and one number.
        String pattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{8,}$";

        if (password.isEmpty()) {
            passwordEt.setError(App.getContext().getString(R.string.required_field_error));
        } else if (!password.matches(pattern)) {
            passwordEt.setError(App.getContext().getString(R.string.password_bad_format_error));
        } else {
            passwordEt.setError(null);
        }
    }

}
