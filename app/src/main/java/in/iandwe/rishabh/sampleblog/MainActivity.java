package in.iandwe.rishabh.sampleblog;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike; //Database firebase reference for Like button

    private FirebaseAuth mAuth; //Firebase Authentication
    private FirebaseAuth.AuthStateListener mAuthListener; //Authentication listener
    private DatabaseReference mDatabaseCurrentUser;
    private Query mQueryCurrentUser;

    private boolean mProcessLike=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Authentication

        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser()==null){
                    Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }

            }
        };

        mBlogList=(RecyclerView)findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users"); //for the users directory
        mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes"); //add child Likes for adding to database

        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        /*Single user post showing//////
        String currentUserId=mAuth.getCurrentUser().getUid();
        mDatabaseCurrentUser=FirebaseDatabase.getInstance().getReference().child("Blog");

        mQueryCurrentUser=mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);
        //Single user post showing //////*/

        checkUserExists();// check user exist in on create
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        //FirebaseRecycler Adapter code for firebase UI v 3.2.0

        FirebaseRecyclerOptions<Blog> options =
                new FirebaseRecyclerOptions.Builder<Blog>()
                        .setQuery(mDatabase, Blog.class)  //Single user post showing use mQueryCurrentUser for it.
                        .build();


        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.blog_row, parent, false);

                return new BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder holder, int position, @NonNull Blog model) {
                final String post_key=getRef(position).getKey(); //getting the key of every individual post

                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDesc());
                holder.setImage(getApplicationContext(),model.getImage());
                holder.setUsername(model.getUsername());
                holder.setLikeButton(post_key);

                //setting on clicklistener of the recycler view
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      //  Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_LONG).show();
                        // Single Page Post
                        Intent singleBlogIntent=new Intent(MainActivity.this,BlogSingle.class);
                        singleBlogIntent.putExtra("blog_key",post_key);
                        startActivity(singleBlogIntent);

                    }
                });

                //Like button onClick
                holder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike=true;

                            //value listener or Like Button
                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mProcessLike){
                                    //Checking if the like is given by the existing user or not
                                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike=false;

                                    }else{
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("demo user");
                                        mProcessLike=false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }
                });
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening(); //Important to show my data
    }



    private void checkUserExists() {

        if (mAuth.getCurrentUser() != null) { //Important
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupAccountActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    //****************Holding my Recycler View***************//
    // add this to the recycler view
    public static class BlogViewHolder extends RecyclerView.ViewHolder{
        //Adding Like button to the post
        private ImageButton mLikeButton;
        View mView;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            //Adding Like button to the post
            mLikeButton=(ImageButton)mView.findViewById(R.id.like_button);
            //For Like/Unlike
            mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth=FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }
        public void setTitle(String title){
            TextView post_title=(TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);

        }
        public void setDesc(String desc){
            TextView post_desc=(TextView)mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setImage(Context ct, String image){
            ImageView post_image=(ImageView)mView.findViewById(R.id.post_image);
            Picasso.with(ct).load(image).into(post_image);
        }
        public void setUsername(String username){
            TextView post_username=(TextView)mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }
        //Changing the buttons (LIKE/UNLIKE)
        public void setLikeButton(final String post_key){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mLikeButton.setImageResource(R.drawable.ic_like_button_red);

                    }else{
                        mLikeButton.setImageResource(R.drawable.ic_like_normal);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu); //never miss super keyword to access the parent class onCreateOptionsMenu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.addblog){
            startActivity(new Intent(MainActivity.this,PostBlog.class));
        }
        if(item.getItemId()==R.id.logout){
            logout();
        }


        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }
}
