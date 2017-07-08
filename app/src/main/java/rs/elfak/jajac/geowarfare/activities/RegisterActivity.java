package rs.elfak.jajac.geowarfare.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mRepeatPassword;
    private EditText mDisplayName;
    private EditText mFullName;
    private EditText mPhone;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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

    private boolean allFieldsValid() {
        Validator.validateEmail(mEmail);
        Validator.validatePassword(mPassword);
        Validator.validateRepeatPassword(mPassword, mRepeatPassword);
        Validator.validateDisplayName(mDisplayName);
        Validator.validateFullName(mFullName);
        Validator.validatePhone(mPhone);
        return mEmail.length() > 0 && mEmail.getError() == null &&
                mPassword.length() > 0 && mPassword.getError() == null &&
                mRepeatPassword.length() > 0 && mRepeatPassword.getError() == null &&
                mDisplayName.length() > 0 && mDisplayName.getError() == null &&
                mFullName.length() > 0 && mFullName.getError() == null &&
                mPhone.length() > 0 && mPhone.getError() == null;
    }

    private UserModel getUserModel(String newUserId) {
        return new UserModel(
                newUserId,
                mEmail.getText().toString().trim(),
                mDisplayName.getText().toString().trim(),
                mFullName.getText().toString().trim(),
                mPhone.getText().toString().trim()
        );
    }

    public void onRegisterClick(View v) {
        if (!allFieldsValid()) return;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.register_progress_dialog_message));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            String newUserId = mAuth.getCurrentUser().getUid();
                            UserModel newUser = getUserModel(newUserId);
                            mDatabase.child("users").child(newUserId).setValue(newUser);
                        } else {
                            Toast.makeText(RegisterActivity.this, "FAILED!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

}
