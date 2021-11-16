package com.akpaswan.Wyou.fragmentss;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akpaswan.Wyou.adapters.MessageListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.akpaswan.Wyou.R;
import com.akpaswan.Wyou.models.User;

public class MessageFragment extends Fragment {
    RecyclerView recyclerView;
    MessageListAdapter adapter;
    FirebaseUser firebaseUser;
    ImageView imgNoContact;

    @Nullable
    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_userlist, container, false);
        recyclerView = view.findViewById (R.id.recyclerView);
        imgNoContact = view.findViewById (R.id.imgNoContact);
        recyclerView.setLayoutManager (new LinearLayoutManager (getActivity ( )));
        adapter = new MessageListAdapter ( );
        recyclerView.setAdapter (adapter);
        firebaseUser = FirebaseAuth.getInstance ( ).getCurrentUser ( );
        loadData ( );

        return view;
    }

    private void loadData () {
        final FirebaseDatabase database = FirebaseDatabase.getInstance ( );
        DatabaseReference userRef = database.getReference ( ).child ("users");
        userRef.addValueEventListener (new ValueEventListener ( ) {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                adapter.clear ( );
                Iterable<DataSnapshot> itr = dataSnapshot.getChildren ( );
                for (DataSnapshot snapshot : itr) {
                    User user = snapshot.getValue (User.class);
                    if (user.getPhone ( ).equals (firebaseUser.getPhoneNumber ( ))) {
                        continue;
                    }
                    //System.out.println (user.getName ( ) + "   " + user.getPhone ( ));
                    adapter.addItem (user);
                }
                if (adapter.getItemCount ( ) == 0) {
                    imgNoContact.setVisibility (View.VISIBLE);
                } else {
                    imgNoContact.setVisibility (View.GONE);

                }
            }

            @Override
            public void onCancelled (DatabaseError databaseError) {
                Toast.makeText (getActivity ( ), "Error: " + databaseError, Toast.LENGTH_SHORT).show ( );
            }
        });

    }

}
