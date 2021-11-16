package com.piford.amessenger;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.piford.amessenger.adapters.ChatListAdapter;
import com.piford.amessenger.common.Common;
import com.piford.amessenger.models.Message;
import com.piford.amessenger.models.User;

import java.util.Date;

public class ChatActivity extends AppCompatActivity implements ChatListAdapter.OnMultipleSelectListener {
    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ChatListAdapter adapter;
    DatabaseReference chatRef;
    private User chatUser;
    private String senderPhone;
    private String receiverPhone;
    private Button btnDelete;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_chat);
        recyclerView = findViewById (R.id.recyclerView);
        etMessage = findViewById (R.id.etMessage);
        btnSend = findViewById (R.id.btnSend);
        btnDelete = findViewById (R.id.btnDelete);
        LinearLayoutManager layoutManager = new LinearLayoutManager (this);
        layoutManager.setStackFromEnd (true);
        recyclerView.setLayoutManager (layoutManager);
        chatRef = FirebaseDatabase.getInstance ( ).getReference ( ).child ("chat");
        adapter = new ChatListAdapter ( );
        adapter.setOnMultipleSelectListener (this);
        recyclerView.setAdapter (adapter);


        btnSend.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                sendMessage ( );
            }
        });
        btnDelete.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                adapter.deleteSelected (Common.getMessageId (senderPhone, receiverPhone));
            }
        });

        chatUser = (User) getIntent ( ).getSerializableExtra (Common.CHAT_USER);
        receiverPhone = chatUser.getPhone ( );
        senderPhone = FirebaseAuth.getInstance ( ).getCurrentUser ( ).getPhoneNumber ( );
        getSupportActionBar ( ).setTitle (receiverPhone);

        loadChatData ( );
    }

    private void sendMessage () {
        String msg = etMessage.getText ( ).toString ( );
        if (msg.trim ( ).isEmpty ( )) {
            return;
        }
        Message message = new Message ( );
        message.setContent (msg);
        message.setDateTime (new Date ( ));
        message.setFrom (senderPhone);
        message.setTo (receiverPhone);

        String randomId = new Date ( ).hashCode ( ) + "";
        chatRef.child (Common.getMessageId (senderPhone, receiverPhone)).child (randomId).setValue (message).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess (Void aVoid) {
                etMessage.setText ("");

            }
        }).addOnCompleteListener (new OnCompleteListener<Void> ( ) {
            @Override
            public void onComplete (@NonNull Task<Void> task) {

            }
        }).addOnFailureListener (new OnFailureListener ( ) {
            @Override
            public void onFailure (@NonNull Exception e) {
                Toast.makeText (ChatActivity.this, "Error: " + e.toString ( ), Toast.LENGTH_SHORT).show ( );
            }
        });
    }

    private void loadChatData () {
        chatRef.child (Common.getMessageId (senderPhone, receiverPhone)).addValueEventListener (new ValueEventListener ( ) {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                adapter.clear ( );
                Iterable<DataSnapshot> itr = dataSnapshot.getChildren ( );
                for (DataSnapshot snapshot : itr) {
                    Message message = snapshot.getValue (Message.class);
                    message.setId (snapshot.getKey ( ));
                    adapter.addItem (message);
                }
            }

            @Override
            public void onCancelled (DatabaseError databaseError) {
                Toast.makeText (ChatActivity.this, "Error: " + databaseError, Toast.LENGTH_SHORT).show ( );
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        menu.add ("Logout");
        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        if (item.getTitle ( ).equals ("Logout")) {
            FirebaseAuth.getInstance ( ).signOut ( );
//            System.exit (0);
            finish ( );
        }
        return super.onOptionsItemSelected (item);
    }

    @Override
    public void onMultipleSelect () {
        btnDelete.setVisibility (View.VISIBLE);
    }

    @Override
    public void singleMode () {
        btnDelete.setVisibility (View.GONE);
        adapter.clearMultipleSelectionMode ( );

    }

    @Override
    public void onBackPressed () {
        if (adapter.isMultipleSelectionMode ( )) {
            adapter.clearMultipleSelectionMode ( );
            btnDelete.setVisibility (View.GONE);
        } else {
            super.onBackPressed ( );
        }
    }
}
