package com.example.mynoteapp411;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mynoteapp411.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseFirestore firestore;
    private RecyclerView rvNotes;
    private FloatingActionButton btAdd;
    private ImageButton btLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);



        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("posts");
        firestore = FirebaseFirestore.getInstance();
        rvNotes = findViewById(R.id.rv_note);
        rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        btAdd = findViewById(R.id.bt_add);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote();
            }
        });
        toolbar.findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện khi nút được nhấn
                // Hiển thị menu
                showMenu(v);
            }
        });



//        login("noteapp@gmail.com","123456");
//        createNewUser("newuser123@gmail.com","123456");
//        postDataToRTDB();
//        readDataFromRTDB();
//        postDataToFireStore();
//        addPostData(new Post("Ngoc","NoteApp test"));
//        addPostData(new Post("Test post","anything"));


    }

    private void showMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.main_menu);
        popupMenu.show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(myRef, Post.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Post, PostHolder>(options) {
            @Override
            public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_items, parent, false);
                return new PostHolder(view);
            }
            @Override
            protected void onBindViewHolder(PostHolder holder, int position, Post model) {
                // Bind the Chat object to the ChatHolder
                holder.tvTitle.setText(model.getTitle());
                holder.tvContent.setText(model.getContent());
                holder.layoutNote.setBackgroundColor(Color.parseColor(model.getColor()));

                ImageView ivAction = holder.itemView.findViewById(R.id.ib_moreOption);
                ivAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });


            }
        };
        rvNotes.setAdapter(adapter);
        adapter.startListening();
    }
    public static class PostHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvContent;
        private LinearLayout layoutNote;
        public PostHolder(View view) {
            super(view);
            tvContent = view.findViewById(R.id.tv_content);
            tvTitle = view.findViewById(R.id.tv_title);
            layoutNote = view.findViewById(R.id.layout_note);
        }
    }
    private void addNote(){
        AlertDialog.Builder nDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View nView = inflater.inflate(R.layout.add_note,null);
        nDialog.setView(nView);

        AlertDialog dialog = nDialog.create();
        dialog.setCancelable(true);
        dialog.show();

        Button save = nView.findViewById(R.id.bt_save);
        EditText edtTitle = nView.findViewById(R.id.edt_title);
        EditText edtContent = nView.findViewById(R.id.edt_content);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = myRef.push().getKey();
                String title =  edtTitle.getText().toString();
                String content = edtContent.getText().toString();
                myRef.child(id).setValue(new Post(id,title,content,getRandomColor())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"Add note successful",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this,"Add note failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void addNoteTest(){
        String id = myRef.push().getKey();
        String title = "Test title 2";
        String content = "Test content 2";
        myRef.child(id).setValue(new Post(id,title,content,getRandomColor())).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Add note successful",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"Add note failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.menu_logout) {
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getRandomColor(){
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#8581e9");
        colors.add("#7fffd4");
        colors.add("#05c1ff");
        colors.add("#fa9901");
        colors.add("#b9e2b6");
        colors.add("#96efe4");
        colors.add("#b38e74");
        Random random = new Random();
        return colors.get(random.nextInt(colors.size()));
    }
    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("DEBUG","Login successful");
                        }else{
                            Log.d("DEBUG","Login failed");
                        }
                    }
                });
    }
    private void createNewUser(String newEmail, String newPass){
        mAuth.createUserWithEmailAndPassword(newEmail,newPass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("DEBUG","New user add successful");
                        }else{
                            Log.d("DEBUG","New user add failed");
                        }
                    }
                });
    }
    private void resetPass(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("DEBUG","Send pass successful");
                        }else{
                            Log.d("DEBUG","Send pass failed");
                        }
                    }
                });
    }
    private void signOut(){
        mAuth.signOut();
    }
    private void postDataToRTDB(String data){
        // Write a message to the database
        myRef.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("DEBUG","Post data successful");
                }else{
                    Log.d("DEBUG","Post data failed");
                }
            }
        });
    }
    private void readDataFromRTDB(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                Log.d("DEBUG", "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DEBUG", "Failed to read value.", error.toException());
            }
        });
    }
    private void postDataToFireStore(){
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

// Add a new document with a generated ID
        firestore.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("DEBUG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEBUG", "Error adding document", e);
                    }
                });
    }
    private void addPostData(Post data){
        DatabaseReference myRefRoot = database.getReference();
        myRefRoot.child("posts").setValue(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("DEBUG","Post data successful");
                        }else{
                            Log.d("DEBUG","Post data failed");
                        }
                    }
                });
    }
}