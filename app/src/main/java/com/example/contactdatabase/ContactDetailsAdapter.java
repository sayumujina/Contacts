package com.example.contactdatabase;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

// ContactDetailsAdapter class to bind contact data to the RecyclerView
public class ContactDetailsAdapter extends RecyclerView.Adapter<ContactDetailsAdapter.ContactViewHolder> {
    private final ArrayList<Contact> contacts;
    private boolean selectionMode = false;
    public ArrayList<Integer> selectedContacts = new ArrayList<>();
    public ContactDetailsAdapter(ArrayList<Contact> contacts) {
        this.contacts = contacts != null ? contacts : new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        Log.d("ContactDetailsAdapter", "Number of contacts: " + contacts.size());
        return contacts.size();
    }

    // Holds references to the text view
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_details_template, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Log.d("ContactDetailsAdapter", "Binding position: " + position);

        Contact contact = contacts.get(position);
        if (contact != null) {
            holder.templateNameValue.setText(contact.getName() != null ? contact.getName() : "");
            holder.templateEmailValue.setText(contact.getEmail() != null ? contact.getEmail() : "");
            holder.templateDoBValue.setText(contact.getDateOfBirth() != null ? contact.getDateOfBirth() : "");

            // Set the avatar based on the stored avatar number
            int avatarNumber = contact.getAvatarId();
            String avatarResourceName = "avatar_" + avatarNumber;
            int index = avatarNumber - 1;

            if (index >= 0 && index < AvatarConstants.AVATAR_RESOURCES.length) {
                holder.contactAvatar.setImageResource(AvatarConstants.AVATAR_RESOURCES[index]);
            } else {
                holder.contactAvatar.setImageResource(R.drawable.avatar_1); // Default fallback
            }
        } else {
            holder.templateNameValue.setText("");
            holder.templateEmailValue.setText("");
            holder.templateDoBValue.setText("");
            holder.contactAvatar.setImageResource(R.drawable.avatar_1);
        }

        // Handle selection mode
        holder.itemCheckBox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.itemCheckBox.setChecked(selectedContacts.contains(position)); // Set checkbox state based on selection

        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                enterSelectionMode();
                selectedContacts.add(position);
                holder.itemCheckBox.setChecked(true);
            }

            return true;
        });

        // Handle item selection
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                if (selectedContacts.contains(position)) {
                    selectedContacts.remove(Integer.valueOf(position));
                    holder.itemCheckBox.setChecked(false);
                } else {
                    selectedContacts.add(position);
                    holder.itemCheckBox.setChecked(true);
                }

                // If all items are selected, notifies the contact list to change "Select All" to "Deselect All"
                // And vice versa
                if (selectedContacts.size() == contacts.size()) {
                    if (contactSelectionChangeListener != null) {
                        contactSelectionChangeListener.onContactSelectionChanged(true);
                    }
                } else {
                    if (contactSelectionChangeListener != null) {
                        contactSelectionChangeListener.onContactSelectionChanged(false);
                    }
                }

            } else {
                // Notify listener for edit request
                if (onContactClickListener != null) {
                    onContactClickListener.onContactClick(contact, position);
                }
            }
        });
    }

    // Listen to checkbox selection changes
    public interface OnContactSelectionChangeListener {
        void onContactSelectionChanged(boolean isAllContactsSelected);
    }

    private OnContactSelectionChangeListener contactSelectionChangeListener;

    public void setContactSelectionChangeListener(OnContactSelectionChangeListener listener) {
        this.contactSelectionChangeListener = listener;
    }

    // Listen to selection mode changes
    public interface SelectionModeListener {
        void onSelectionModeChanged(boolean isSelectionMode);
    }
    private SelectionModeListener selectionModeListener;
    public void setSelectionModeListener(SelectionModeListener listener) {
        this.selectionModeListener = listener;
    }

    // Enter selection mode
    public void enterSelectionMode() {
        selectionMode = true;
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeChanged(true);
        }
        notifyItemRangeChanged(0, getItemCount());
    }

    // Exit selection mode
    public void exitSelectionMode() {
        selectionMode = false;
        selectedContacts.clear();
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeChanged(false);
        }
        notifyDataSetChanged();
    }

    // Delete selected contacts from the database
    public void deleteSelectedContacts(DatabaseHelper db) {
        for (int i = 0; i <= selectedContacts.size() - 1; i++) {
            int position = selectedContacts.get(i);
            db.deleteContactById(contacts.get(position).getId());
        }
        contacts.clear();
        contacts.addAll(db.getAllContacts()); // Refresh the contacts list
        notifyDataSetChanged();
        exitSelectionMode();
    }


    // Listen to edit requests from user when clicking on a contact
    public interface OnContactClickListener {
        void onContactClick(Contact contact, int position);
    }
    private OnContactClickListener onContactClickListener;
    public void setOnContactClickListener(OnContactClickListener listener) {
        this.onContactClickListener = listener;
    }

    // Select all contacts
    public void selectAllContacts() {
        selectedContacts.clear();
        for (int i = 0; i < contacts.size(); i++) {
            selectedContacts.add(i);
        }
        notifyDataSetChanged();
    }

    // Deselect all contacts
    public void deselectAllContacts() {
        selectedContacts.clear();
        notifyDataSetChanged();
    }

    // ContactViewHolder class to hold the views for each contact item
    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView templateNameValue, templateEmailValue, templateDoBValue;
        public ImageView contactAvatar;
        public CheckBox itemCheckBox;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactAvatar = itemView.findViewById(R.id.contactAvatar);
            templateNameValue = itemView.findViewById(R.id.templateNameValue);
            templateEmailValue = itemView.findViewById(R.id.templateEmailValue);
            templateDoBValue = itemView.findViewById(R.id.templateDoBValue);
            itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
        }
    }
}
