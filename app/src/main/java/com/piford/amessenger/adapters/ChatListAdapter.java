package com.piford.amessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.piford.amessenger.R;
import com.piford.amessenger.common.Common;
import com.piford.amessenger.models.Message;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Message> chatList = new ArrayList<Message> ( );
    Context context;
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private String loggedPhone;
    private boolean multipleSelectionMode;
    private ArrayList<Message> selectedList = new ArrayList<> (0);

    public ChatListAdapter () {
        loggedPhone = FirebaseAuth.getInstance ( ).getCurrentUser ( ).getPhoneNumber ( );

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {

        this.context = parent.getContext ( );
        LayoutInflater inflater = LayoutInflater.from (context);
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case LEFT:
                holder = new LeftHolder (inflater.inflate (R.layout.item_chat_left, parent, false));
                break;
            case RIGHT:
                holder = new RightHolder (inflater.inflate (R.layout.item_chat_right, parent, false));
        }
        return holder;
    }


    @Override
    public int getItemViewType (int position) {
        if (chatList.get (position).getFrom ( ).equals (loggedPhone)) {
            return RIGHT;
        } else {
            return LEFT;
        }
//        return super.getItemViewType (position);
    }

    OnMultipleSelectListener onMultipleSelectListener;

    public void clearMultipleSelectionMode () {
        multipleSelectionMode = false;
        selectedList.clear ( );
        notifyDataSetChanged ( );
    }

    public void deleteSelected (String commId) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance ( ).getReference ( ).child ("chat").child (commId);
        for (int i = 0; i < selectedList.size ( ); i++) {
            Message selectedMessage = selectedList.get (i);
            chatRef.child (selectedMessage.getId ( )).removeValue ( ).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                @Override
                public void onSuccess (Void aVoid) {

                    if (onMultipleSelectListener != null) {
                        onMultipleSelectListener.singleMode ( );
                    }
                }
            }).addOnFailureListener (new OnFailureListener ( ) {
                @Override
                public void onFailure (@NonNull Exception e) {
                    Toast.makeText (context, "ErrorL " + e, Toast.LENGTH_SHORT).show ( );
                }
            });
        }
    }

    public interface OnMultipleSelectListener {
        void onMultipleSelect ();

        void singleMode ();
    }

    public void setOnMultipleSelectListener (OnMultipleSelectListener onMultipleSelectListener) {
        this.onMultipleSelectListener = onMultipleSelectListener;
    }

    public boolean isMultipleSelectionMode () {
        return multipleSelectionMode;
    }

    int selectedIndex;

    @Override
    public void onBindViewHolder (@NonNull RecyclerView.ViewHolder holder, int position) {
        final Message message = chatList.get (position);
        if (chatList.get (position).getFrom ( ).equals (loggedPhone)) {
            final RightHolder rightHolder = (RightHolder) holder;
            rightHolder.tvContent.setText (message.getContent ( ));

            if (multipleSelectionMode) {
                selectedIndex = -1;
                for (int i = 0; i < selectedList.size ( ); i++) {
                    Message selectedMessage = selectedList.get (i);
                    if (selectedMessage.getId ( ).equals (message.getId ( ))) {
                        selectedIndex = i;
                        break;
                    }
                }
                if (selectedIndex == -1) {
                    rightHolder.viewOverlay.setVisibility (View.GONE);
                } else {
                    rightHolder.viewOverlay.setVisibility (View.VISIBLE);
                }
                rightHolder.tvContent.setOnClickListener (new View.OnClickListener ( ) {
                    @Override
                    public void onClick (View view) {
                        selectItem (rightHolder, message);
                    }
                });
            }

            rightHolder.tvContent.setOnLongClickListener (new View.OnLongClickListener ( ) {
                @Override
                public boolean onLongClick (View view) {
                    multipleSelectionMode = true;
                    selectItem (rightHolder, message);
                    if (onMultipleSelectListener != null) {
                        onMultipleSelectListener.onMultipleSelect ( );
                    }
                    return true;
                }
            });
        } else {
            LeftHolder leftHolder = (LeftHolder) holder;
            leftHolder.tvContent.setText (message.getContent ( ));

        }
    }

    private void selectItem (RightHolder rightHolder, Message message) {
        selectedIndex = -1;
        for (int i = 0; i < selectedList.size ( ); i++) {
            Message selectedMessage = selectedList.get (i);
            if (selectedMessage.getId ( ).equals (message.getId ( ))) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1) {
            selectedList.add (message);
            rightHolder.viewOverlay.setVisibility (View.VISIBLE);
        } else {
            selectedList.remove (selectedIndex);
            rightHolder.viewOverlay.setVisibility (View.VISIBLE);
        }
        notifyDataSetChanged ( );
    }

    @Override
    public int getItemCount () {
        return chatList.size ( );
    }

    public void clear () {
        chatList.clear ( );
        notifyDataSetChanged ( );
    }

    class LeftHolder extends RecyclerView.ViewHolder {
        TextView tvContent;

        public LeftHolder (@NonNull View itemView) {
            super (itemView);
            tvContent = itemView.findViewById (R.id.tvContent);

        }
    }


    class RightHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        View viewOverlay;

        public RightHolder (@NonNull View itemView) {
            super (itemView);
            tvContent = itemView.findViewById (R.id.tvContent);
            viewOverlay = itemView.findViewById (R.id.viewOverlay);

        }
    }

    public void addItem (Message item) {
        chatList.add (item);
        notifyDataSetChanged ( );
    }

}
