package rs.elfak.jajac.geowarfare.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
    private static final int REQUEST_STORAGE_PERMISSION = 2;

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

    private Uri mImageUri;

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
        checkReadStoragePermission();
    }

    private void checkReadStoragePermission() {
        final String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int userPermission = ContextCompat.checkSelfPermission(RegisterActivity.this, storagePermission);
        boolean permissionGranted = userPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            // Explain the user why the app requires the storage permission and ask for it
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, storagePermission)) {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle(getString(R.string.register_storage_permission_title))
                        .setMessage(getString(R.string.register_storage_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(
                                        RegisterActivity.this,
                                        new String[]{storagePermission},
                                        REQUEST_STORAGE_PERMISSION);
                            }
                        }).create().show();
            } else {
                // User checked "never ask again", explain why gallery isn't available and run camera
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle(getString(R.string.register_storage_permission_title))
                        .setMessage(getString(R.string.register_storage_permission_message_no_ask))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            onStoragePermissionDenied();
                            }
                        }).create().show();
            }
        } else {
            onStoragePermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                // If request is granted, the result arrays won't be empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onStoragePermissionGranted();
                } else {
                    onStoragePermissionDenied();
                }
                return;
            }
        }
    }

    /**
     * If storage access is granted, we can offer the user to choose image from gallery or camera
     */
    private void onStoragePermissionGranted() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        Intent cameraIntent = getCameraIntent();

        if (cameraIntent != null) {
            Intent chooseIntent = Intent.createChooser(galleryIntent, "Select image from");
            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

            startActivityForResult(chooseIntent, REQUEST_CHOOSE_IMAGE);
        } else {
            Toast.makeText(RegisterActivity.this, "Camera error. Provide storage access.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * If storage access id denied, we let the user take a picture with the camera
     */
    private void onStoragePermissionDenied() {
        Intent cameraIntent = getCameraIntent();

        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, REQUEST_CHOOSE_IMAGE);
        } else {
            Toast.makeText(RegisterActivity.this, "Camera error. Provide storage access.", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent getCameraIntent() {
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

                return cameraIntent;
            }
        }

        // If no camera available or File failed to create, we return null
        return null;
    }

    /**
     * Called when the user has selected an image from the gallery or taken one with the Camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri dataImageUri = data.getData();

            // If an Uri is found in getData(), we need to get the real file path
            // from gallery, because imageUri is a "content://..." Uri
            if (dataImageUri != null) {
                mImageUri = Uri.parse(getRealPathFromURI(RegisterActivity.this, dataImageUri));
            }

            mAvatar.setImageURI(mImageUri);
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
                            StorageReference avatarsRef = mStorage.child("avatars").child(mImageUri.toString());
                            avatarsRef.putFile(Uri.fromFile(new File(mImageUri.toString())))
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            String newUserId = mAuth.getCurrentUser().getUid();
                                            String storageImageUrl = taskSnapshot.getDownloadUrl().toString();
                                            UserModel newUser = getUserModel(newUserId, storageImageUrl);
                                            mDatabase.child("users").child(newUserId).setValue(newUser);

                                            progressDialog.dismiss();
                                            Intent profileIntent = new Intent(RegisterActivity.this, MainActivity
                                                    .class);
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

    public void onAvatarClick(View view) {
        mAvatarError.requestFocus();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mImageUri = Uri.parse(image.getAbsolutePath());
        return image;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
