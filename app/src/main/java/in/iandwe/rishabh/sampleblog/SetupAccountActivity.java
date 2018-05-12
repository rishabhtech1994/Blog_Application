package in.iandwe.rishabh.sampleblog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupAccountActivity extends AppCompatActivity {
    private ImageButton profilePic;
    private EditText profileUserName;
    private Button setuUpSubmit;
    private Uri mImageUri=null;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorageProfile;
    private ProgressDialog mProgress;
    private static final int GALLERY_REQUEST=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);
        profilePic=(ImageButton)findViewById(R.id.profilePic);
        profileUserName=(EditText)findViewById(R.id.userProfileName);
        setuUpSubmit=(Button)findViewById(R.id.setUpSubmit);
        mProgress=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users"); //creating Database References
        mStorageProfile= FirebaseStorage.getInstance().getReference().child("Profile Images");
        //Image Button
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent= new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
        //Submit button
        setuUpSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProfile();
            }
        });
    }

    private void submitProfile() {
        final String name=profileUserName.getText().toString().trim();
        final String user_id=mAuth.getCurrentUser().getUid();
        if(!TextUtils.isEmpty(name) && mImageUri!= null){
            mProgress.setMessage("Setting Up Your Profile");
            mProgress.show();
            StorageReference filePath=mStorageProfile.child(mImageUri.getLastPathSegment()); //Setting the name to the storage folder
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl=taskSnapshot.getDownloadUrl().toString();
                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUrl);
                    mProgress.dismiss();
                    Intent profileIntent=new Intent(SetupAccountActivity.this,MainActivity.class);
                    profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(profileIntent);
                }
            });
        }
     }


    //Image cropping
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK) {
             Uri imageUri = data.getData();
            // mImageSelect.setImageURI(mImageUri);
            CropImage.activity(imageUri)    // dont forget to add the mImageUri to change the activity.
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        //Crop Image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                profilePic.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

