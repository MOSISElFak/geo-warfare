package rs.elfak.jajac.geowarfare.fragments;

import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rs.elfak.jajac.geowarfare.R;
import rs.elfak.jajac.geowarfare.activities.MainActivity;
import rs.elfak.jajac.geowarfare.activities.RegisterActivity;
import rs.elfak.jajac.geowarfare.utils.Validator;

public class EditUserInfoFragment extends Fragment implements View.OnFocusChangeListener, View.OnClickListener {

    private static final int REQUEST_CHOOSE_IMAGE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private static final String ARG_DISPLAY_NAME = "display_name";
    private static final String ARG_FULL_NAME = "full_name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_AVATAR_PATH = "avatar_path";

    private Context mContext;

    // Passed in arguments
    private String mDisplayName;
    private String mFullName;
    private String mPhone;
    private String mAvatarPath;

    // UI elements
    private EditText mDisplayNameEt;
    private EditText mFullNameEt;
    private EditText mPhoneEt;
    private ImageView mAvatarImg;
    private TextView mAvatarErrorTv;

    // New avatar image path, used to check whether we need to delete the old and upload the new image
    private String mNewAvatarPath;

    public EditUserInfoFragment() {
        // Required empty public constructor
    }

    public static EditUserInfoFragment newInstance(String displayName, String fullName,
                                                   String phone, String avatarPath) {
        EditUserInfoFragment fragment = new EditUserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DISPLAY_NAME, displayName);
        args.putString(ARG_FULL_NAME, fullName);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_AVATAR_PATH, avatarPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.edit_user_info_title));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDisplayName = getArguments().getString(ARG_DISPLAY_NAME);
            mFullName = getArguments().getString(ARG_FULL_NAME);
            mPhone = getArguments().getString(ARG_PHONE);
            mAvatarPath = getArguments().getString(ARG_AVATAR_PATH);
            mNewAvatarPath = mAvatarPath;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_edit_user_info, container, false);

        mDisplayNameEt = (EditText) inflatedView.findViewById(R.id.display_name_text);
        mFullNameEt = (EditText) inflatedView.findViewById(R.id.full_name_text);
        mPhoneEt = (EditText) inflatedView.findViewById(R.id.phone_text);
        mAvatarImg = (ImageView) inflatedView.findViewById(R.id.avatar_image);
        mAvatarErrorTv = (TextView) inflatedView.findViewById(R.id.avatar_tv);

        Button chooseAvatarButton = (Button) inflatedView.findViewById(R.id.choose_avatar_btn);

        mDisplayNameEt.setText(mDisplayName);
        mFullNameEt.setText(mFullName);
        mPhoneEt.setText(mPhone);
        Glide.with(mContext)
                .load(mAvatarPath)
                .into(mAvatarImg);

        mDisplayNameEt.setOnFocusChangeListener(this);
        mFullNameEt.setOnFocusChangeListener(this);
        mPhoneEt.setOnFocusChangeListener(this);

        chooseAvatarButton.setOnClickListener(this);

        return inflatedView;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            EditText editText = (EditText) view;
            int editTextId = editText.getId();

            switch (editTextId) {
                case R.id.display_name_text:
                    Validator.validateDisplayName(editText);
                    break;
                case R.id.full_name_text:
                    Validator.validateFullName(editText);
                    break;
                case R.id.phone_text:
                    Validator.validatePhone(editText);
                    break;
            }
        }
    }

    public boolean allFieldsValid() {
        Validator.validateDisplayName(mDisplayNameEt);
        Validator.validateFullName(mFullNameEt);
        Validator.validatePhone(mPhoneEt);
        Validator.validateAvatar(mAvatarImg, mAvatarErrorTv);

        return mDisplayNameEt.length() > 0 && mDisplayNameEt.getError() == null &&
                mFullNameEt.length() > 0 && mFullNameEt.getError() == null &&
                mPhoneEt.length() > 0 && mPhoneEt.getError() == null &&
                mAvatarImg.getDrawable() != null && mAvatarErrorTv.getError() == null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_avatar_btn:
                checkReadStoragePermission();
                break;
            case R.id.avatar_image:
                mAvatarErrorTv.requestFocus();
                break;
        }
    }

    private void checkReadStoragePermission() {
        final Activity activity = getActivity();
        final String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int userPermission = ContextCompat.checkSelfPermission(activity, storagePermission);
        boolean permissionGranted = userPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            // Explain the user why the app requires the storage permission and ask for it
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, storagePermission)) {
                new AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.register_storage_permission_title))
                        .setMessage(getString(R.string.register_storage_permission_message))
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(
                                        new String[]{storagePermission},
                                        REQUEST_STORAGE_PERMISSION);
                            }
                        }).create().show();
            } else {
                // User checked "never ask again", explain why gallery isn't available and run camera
                new AlertDialog.Builder(activity)
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
            Toast.makeText(mContext, "Camera error. Provide storage access.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "Camera error. Provide storage access.", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent getCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // If there's a camera activity, we let the user choose Gallery or Camera
        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {
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
                        mContext,
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri dataImageUri = data.getData();

            // If an Uri is found in getData(), we need to get the real file path
            // from gallery, because imageUri is a "content://..." Uri
            if (dataImageUri != null) {
                mNewAvatarPath = getRealPathFromURI(mContext, dataImageUri);
            }

            Glide.with(mContext)
                    .load(mNewAvatarPath)
                    .into(mAvatarImg);
            mAvatarErrorTv.setError(null);
        }
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

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mNewAvatarPath = image.getAbsolutePath();
        return image;
    }

    public String getDisplayName() {
        return mDisplayNameEt.getText().toString().trim();
    }

    public String getFullName() {
        return mFullNameEt.getText().toString().trim();
    }

    public String getPhone() {
        return mPhoneEt.getText().toString().trim();
    }

    public String getAvatarPath() {
        return mNewAvatarPath;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.action_bar_edit_profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_profile_save_item:
                Toast.makeText(getContext(), "SAVE!", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

}