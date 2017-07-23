package rs.elfak.jajac.geowarfare.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.fragments.EditUserInfoFragment;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.providers.FirebaseProvider;
import rs.elfak.jajac.geowarfare.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements
        View.OnFocusChangeListener,
        EditUserInfoFragment.OnFragmentInteractionListener {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mRepeatPassword;

    private EditUserInfoFragment mUserInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserInfoFragment = (EditUserInfoFragment) getSupportFragmentManager().findFragmentById(
                R.id.register_user_info_fragment);

        mEmail = (EditText) findViewById(R.id.register_email_text);
        mPassword = (EditText) findViewById(R.id.register_password_text);
        mRepeatPassword = (EditText) findViewById(R.id.register_repeat_password_text);

        mEmail.setOnFocusChangeListener(this);
        mPassword.setOnFocusChangeListener(this);
        mRepeatPassword.setOnFocusChangeListener(this);
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
            }
        }
    }

    private boolean allFieldsValid() {
        Validator.validateEmail(mEmail);
        Validator.validatePassword(mPassword);
        Validator.validateRepeatPassword(mPassword, mRepeatPassword);
        boolean userInfoValid = mUserInfoFragment.allFieldsValid();

        return mEmail.length() > 0 && mEmail.getError() == null &&
                mPassword.length() > 0 && mPassword.getError() == null &&
                mRepeatPassword.length() > 0 && mRepeatPassword.getError() == null &&
                userInfoValid;
    }

    private UserModel getUserModel(String newUserId, String storageImgUrl) {
        return new UserModel(
                newUserId,
                mEmail.getText().toString().trim(),
                mUserInfoFragment.getDisplayName(),
                mUserInfoFragment.getFullName(),
                mUserInfoFragment.getPhone(),
                storageImgUrl
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

        final FirebaseProvider firebaseProvider = FirebaseProvider.getInstance();

        // First we create the user using FirebaseAuth so he/she's authenticated
        firebaseProvider.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Then we store the avatar in Storage and get its downloadUrl
                        // from the snapshot, and save all user data in realtime database
                        if (task.isSuccessful()) {
                            String imageFileName = mUserInfoFragment.getAvatarFileName();
                            String localImageUri = mUserInfoFragment.getAvatarPath();
                            firebaseProvider.uploadAvatarImage(imageFileName, localImageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            String newUserId = firebaseProvider.getCurrentUser().getUid();
                                            String storageImageUri = taskSnapshot.getDownloadUrl().toString();
                                            UserModel newUser = getUserModel(newUserId, storageImageUri);
                                            Map<String, Object> userInfoValues = newUser.toMap();
                                            firebaseProvider.updateUserInfo(newUserId, userInfoValues);

                                            progressDialog.dismiss();
                                            Intent profileIntent = new Intent(RegisterActivity.this,
                                                    MainActivity.class);
                                            startActivity(profileIntent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this, getString(R.string
                                                    .register_failed_message), Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            progressDialog.dismiss();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mEmail.setError(getString(R.string.email_bad_format_error));
                                mEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mEmail.setError(getString(R.string.register_email_exists_error));
                                mEmail.requestFocus();
                            } catch (Exception e) {
                                Toast.makeText(RegisterActivity.this, "Exception: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

    }

    @Override
    public void onEditFinished() {
        // We do nothing here because the action is handled using the register button
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserInfoFragment = null;
    }
}
