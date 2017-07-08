package rs.elfak.jajac.geowarfare.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mRepeatPassword;
    private EditText mDisplayName;
    private EditText mFullName;
    private EditText mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = (EditText) findViewById(R.id.register_email_text);
        mPassword = (EditText) findViewById(R.id.register_password_text);
        mRepeatPassword = (EditText) findViewById(R.id.register_repeat_password_text);
        mDisplayName = (EditText) findViewById(R.id.register_display_name_text);
        mFullName = (EditText) findViewById(R.id.register_full_name_text);
        mPhone = (EditText) findViewById(R.id.register_phone_text);

        mEmail.setOnFocusChangeListener(this);
        mPassword.setOnFocusChangeListener(this);
        mRepeatPassword.setOnFocusChangeListener(this);
        mDisplayName.setOnFocusChangeListener(this);
        mFullName.setOnFocusChangeListener(this);
        mPhone.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            EditText editText = (EditText) view;
            int editTextId = editText.getId();

            switch (editTextId) {
                case R.id.register_email_text:
                    Validator.validateEmail(editText);
                    break;
                case R.id.register_password_text:
                    Validator.validatePassword(editText);
                    break;
                case R.id.register_repeat_password_text:
                    Validator.validateRepeatPassword(mPassword, editText);
                    break;
                case R.id.register_display_name_text:
                    Validator.validateDisplayName(editText);
                    break;
                case R.id.register_full_name_text:
                    Validator.validateFullName(editText);
                    break;
                case R.id.register_phone_text:
                    Validator.validateFullName(editText);
                    break;
            }
        }
    }

    public void onAddImageClick(View v) {

    }

    public void onRegisterClick(View v) {

    }

}
