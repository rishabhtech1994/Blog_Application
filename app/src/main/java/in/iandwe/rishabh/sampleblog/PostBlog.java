package in.iandwe.rishabh.sampleblog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class PostBlog extends AppCompatActivity {

    private ImageButton mImageSelect;
    private EditText mPostName;
    private EditText mPostDesc;
    private Button mSubmitButton;
    private Uri mImageUri=null;
    private FirebaseAuth mAuth; //Authentication
    private FirebaseUser mUser;
    private ProgressDialog mProgress;
    private StorageReference mStorage; //creating instance of firebase storage
    private DatabaseReference mDatabase; // Database referencing
    private DatabaseReference mDatabaseUsers;

    private static final int GALLERY_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_blog);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser(); //Authentication with the current user
        mStorage=FirebaseStorage.getInstance().getReference();   //storage reference
        mDatabase=FirebaseDatabase.getInstance().getReference().child("Blog"); //database reference
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
        mImageSelect=(ImageButton)findViewById(R.id.imageButton);
        mPostName=(EditText)findViewById(R.id.mPostName);
        mPostDesc=(EditText)findViewById(R.id.mPostDesc);
        mSubmitButton=(Button)findViewById(R.id.mSubmitButton);
        mProgress=new ProgressDialog(this);
        mImageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent= new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
        //Submit button on click function
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posting();
            }
        });
    }

    private void posting() {
        mProgress.setMessage("Uploading the Post");
        final String post_name=mPostName.getText().toString().trim();
        final String post_desc=mPostDesc.getText().toString().trim();
        if(!TextUtils.isEmpty(post_name) && !TextUtils.isEmpty(post_desc)&& mImageUri!=null){
            mProgress.show(); //if user have entered something than the progress dialog will load
            StorageReference filePath=mStorage.child("Blog Images").child(mImageUri.getLastPathSegment()); //Setting the name to the storage folder
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl=taskSnapshot.getDownloadUrl();
                    final DatabaseReference newPost=mDatabase.push();  //newPost is the root directory newPost/Blog/randomId



                    //getting value of user name and uid into the post
                    mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(post_name);
                            newPost.child("desc").setValue(post_desc);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("uid").setValue(mUser.getUid()); // identifying which user is posting the blog
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(PostBlog.this,MainActivity.class));
                                    }
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mProgress.dismiss();
                    startActivity(new Intent(PostBlog.this,MainActivity.class));
                }
            });
        }
    }

    /*******************Random String generator Code********************************/
    /*public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK) {
            mImageUri = data.getData();
           // mImageSelect.setImageURI(mImageUri);
            CropImage.activity(mImageUri)    // dont forget to add the mImageUri to change the activity.
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        //Crop Image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mImageSelect.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
            }
        }
    }
}