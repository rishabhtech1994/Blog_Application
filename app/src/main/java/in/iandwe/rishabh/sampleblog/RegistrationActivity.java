package in.iandwe.rishabh.sampleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userName,userEmail,userPass;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference; //for storing the username and userimage
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth=FirebaseAuth.getInstance(); //creating firebase  Auth instance
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users"); //creating database reference instance
        userName=(EditText)findViewById(R.id.nameUser);
        userEmail=(EditText)findViewById(R.id.emailUser);
        userPass=(EditText)findViewById(R.id.passUser);
        mProgress=new ProgressDialog(this);
        registerButton=(Button)findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        mProgress.setMessage("Signing Up");
        mProgress.show();
        final String user=userName.getText().toString().trim();
        String email=userEmail.getText().toString().trim();
        String pass=userPass.getText().toString().trim();
        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){
            mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db=mDatabaseReference.child(user_id);
                        current_user_db.child("name").setValue(user);
                        current_user_db.child("image").setValue("default");
                        mProgress.dismiss();
                        Intent mainIntent=new Intent(RegistrationActivity.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });
        }
    }
}
