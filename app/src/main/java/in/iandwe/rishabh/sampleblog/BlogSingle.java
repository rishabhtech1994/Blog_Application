package in.iandwe.rishabh.sampleblog;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class BlogSingle extends AppCompatActivity {

    private String mPost_key;
    private DatabaseReference mDatabase;
    private ImageView mBlogSingleImage;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDesc;
    private Button removePost;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        mBlogSingleImage=(ImageView)findViewById(R.id.mSingleImage);
        mBlogSingleTitle=(TextView)findViewById(R.id.mSingleTitle);
        mBlogSingleDesc=(TextView)findViewById(R.id.mSingleDesc);
        removePost=(Button)findViewById(R.id.removeButton);
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mAuth=FirebaseAuth.getInstance();

        mDatabase.keepSynced(true);


        mPost_key=getIntent().getExtras().getString("blog_key");
        //Toast.makeText(BlogSingle.this, post_key, Toast.LENGTH_LONG).show();
        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_title=(String)dataSnapshot.child("title").getValue();
                String post_desc=(String)dataSnapshot.child("desc").getValue();
                String post_image=(String)dataSnapshot.child("image").getValue();
                String post_uid=(String)dataSnapshot.child("uid").getValue();

                mBlogSingleTitle.setText(post_title);
                mBlogSingleDesc.setText(post_desc);
                Picasso.with(BlogSingle.this).load(post_image).into(mBlogSingleImage);
                //check the current user to make the remove button visible
                if(mAuth.getCurrentUser().getUid().equals(post_uid)){
                    removePost.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        removePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(mPost_key).removeValue();
                startActivity(new Intent(BlogSingle.this,MainActivity.class));
            }
        });

   }
}
