package rs.elfak.jajac.geowarfare.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.utils.Validator;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText mEmail;
    private EditText mPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);

        mEmail = (EditText) findViewById(R.id.login_email_text);
        mPassword = (EditText) findViewById(R.id.login_password_text);

        mEmail.setOnFocusChangeListener(this);
        mPassword.setOnFocusChangeListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            EditText editText = (EditText) view;
            int editTextId = editText.getId();

            switch (editTextId) {
                case R.id.login_email_text:
                    Validator.validateEmail(editText);
                    break;
                case R.id.login_password_text:
                    Validator.validatePassword(editText);
                    break;
            }
        }
    }

    private boolean allFieldsValid() {
        Validator.validateEmail(mEmail);
        Validator.validatePassword(mPassword);
        return mEmail.length() > 0 && mEmail.getError() == null &&
                mPassword.length() > 0 && mPassword.getError() == null;
    }

    public void onLoginClick(View view) {
        if (!allFieldsValid()) return;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.login_progress_dialog_message));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Successful!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "FAILED!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void onNoAccountClick(View v) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

}
