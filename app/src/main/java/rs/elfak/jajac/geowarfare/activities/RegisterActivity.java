package rs.elfak.jajac.geowarfare.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.models.UserModel;
import rs.elfak.jajac.geowarfare.utils.Validator;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private static final int REQUEST_CHOOSE_IMAGE = 1;

    private EditText mEmail;
    private EditText mPassword;
    private EditText mRepeatPassword;
    private EditText mDisplayName;
    private EditText mFullName;
    private EditText mPhone;
    private ImageView mAvatar;
    private TextView mAvatarError;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private String mImagePath;

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
        mAvatar = (ImageView) findViewById(R.id.avatar_image);
        mAvatarError = (TextView) findViewById(R.id.avatar_tv);

        mEmail.setOnFocusChangeListener(this);
        mPassword.setOnFocusChangeListener(this);
        mRepeatPassword.setOnFocusChangeListener(this);
        mDisplayName.setOnFocusChangeListener(this);
        mFullName.setOnFocusChangeListener(this);
        mPhone.setOnFocusChangeListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
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
                    Validator.validatePhone(editText);
                    break;
            }
        }
    }

    public void onAddImageClick(View v) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // If there's a camera activity, we let the user choose Gallery or Camera
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create a file to internally store the new camera image
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(
                        this,
                        "rs.elfak.jajac.geowarfare.fileprovider",
                        imageFile
                );
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                Intent chooseIntent = Intent.createChooser(galleryIntent, "Select image from");
                chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

                startActivityForResult(chooseIntent, REQUEST_CHOOSE_IMAGE);
            }
        } else {
            // If no Camera available, choose from gallery only
            startActivityForResult(galleryIntent, REQUEST_CHOOSE_IMAGE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            // If an Uri is not found in getData(), then we fetch the new camera image
            if (imageUri == null) {
                File file = new File(mImagePath);
                imageUri = Uri.fromFile(file);
            }

            mAvatar.setImageURI(imageUri);
            mAvatarError.setError(null);
        }
    }

    private boolean allFieldsValid() {
        Validator.validateEmail(mEmail);
        Validator.validatePassword(mPassword);
        Validator.validateRepeatPassword(mPassword, mRepeatPassword);
        Validator.validateDisplayName(mDisplayName);
        Validator.validateFullName(mFullName);
        Validator.validatePhone(mPhone);
        Validator.validateAvatar(mAvatar, mAvatarError);

        return mEmail.length() > 0 && mEmail.getError() == null &&
                mPassword.length() > 0 && mPassword.getError() == null &&
                mRepeatPassword.length() > 0 && mRepeatPassword.getError() == null &&
                mDisplayName.length() > 0 && mDisplayName.getError() == null &&
                mFullName.length() > 0 && mFullName.getError() == null &&
                mPhone.length() > 0 && mPhone.getError() == null &&
                mAvatar.getDrawable() != null && mAvatarError.getError() == null;
    }

    private UserModel getUserModel(String newUserId, String storageImgUrl) {
        return new UserModel(
                newUserId,
                mEmail.getText().toString().trim(),
                mDisplayName.getText().toString().trim(),
                mFullName.getText().toString().trim(),
                mPhone.getText().toString().trim(),
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

        // First we create the user in realtime database so he/she's authenticated
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Then we store the avatar in Storage and get its downloadUrl
                        // from the snapshot, and save all user data in realtime database
                        if (task.isSuccessful()) {
                            StorageReference avatarsRef = mStorage.child("avatars").child(mImagePath);
                            avatarsRef.putFile(Uri.fromFile(new File(mImagePath)))
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            String newUserId = mAuth.getCurrentUser().getUid();
                                            String storageImageUrl = taskSnapshot.getDownloadUrl().toString();
                                            UserModel newUser = getUserModel(newUserId, storageImageUrl);
                                            mDatabase.child("users").child(newUserId).setValue(newUser);

                                            progressDialog.hide();
                                            Intent profileIntent = new Intent(RegisterActivity.this, MainActivity
                                                    .class);
                                            startActivity(profileIntent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, getString(R.string
                                                    .register_failed_message), Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
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

    public void onAvatarClick(View view) {
        mAvatarError.requestFocus();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mImagePath = image.getAbsolutePath();
        return image;
    }
}
