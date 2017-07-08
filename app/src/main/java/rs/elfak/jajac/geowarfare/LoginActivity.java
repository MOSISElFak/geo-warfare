package rs.elfak.jajac.geowarfare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import rs.elfak.jajac.geowarfare.utils.Validator;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);

        mEmail = (EditText) findViewById(R.id.login_email_text);
        mPassword = (EditText) findViewById(R.id.login_password_text);

        mEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) Validator.validateEmail((EditText) view);
            }
        });

        mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) Validator.validatePassword((EditText) view);
            }
        });
    }

    private boolean allFieldsValid() {
        Validator.validateEmail(mEmail);
        Validator.validatePassword(mPassword);
        return mEmail.length() > 0 && mEmail.getError() == null &&
                mPassword.length() > 0 && mPassword.getError() == null;
    }

    public void onLoginClick(View view) {
        if (allFieldsValid()) {

        }
    }

    public void onNoAccountClick(View v) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

}
