package com.example.contactdatabase;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactDetailsActivity extends AppCompatActivity {
    RecyclerView.Adapter contactDetailsAdapter;
    RecyclerView recyclerView;
    LinearLayout optionsBar;
    SearchView searchView;
    DatabaseHelper dbHelper;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Contact> allContacts;
    Button selectAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Delete the database file completely
        // FOR TESTING ONLY
        // this.deleteDatabase("contact_details_database");

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_recycler_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contactRecyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up action bar
        Toolbar toolbar = findViewById(R.id.contactListToolbar);
        setSupportActionBar(toolbar);

        // Get the RecyclerView from the layout
        recyclerView = findViewById(R.id.contactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.requestLayout();

        // Get the contact details from the database and displays it
        dbHelper = new DatabaseHelper(this);

        ArrayList<Contact> contacts = dbHelper.getAllContacts();
        contactDetailsAdapter = new ContactDetailsAdapter(contacts);
        recyclerView.setAdapter(contactDetailsAdapter);

        optionsBar = findViewById(R.id.optionsBar);
        selectAllButton = findViewById(R.id.selectAllButton);
        searchView = findViewById(R.id.searchView);

        setupSearchView();
        setupAdapterListeners();
        setupClickListeners(dbHelper);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.switchToAddContactButton) {
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Set up click listeners
    private void setupClickListeners(DatabaseHelper databaseHelper) {
        setupAdapterListeners();

        // Return button
        ImageView returnButton = findViewById(R.id.returnIcon);
        returnButton.setOnClickListener(v -> {
            ((ContactDetailsAdapter) contactDetailsAdapter).exitSelectionMode();
        });

        // Select all button
        selectAllButton = findViewById(R.id.selectAllButton);

        selectAllButton.setOnClickListener(v -> {
            toggleCheckboxSelectionMode();
        });

        // Delete button
        ImageView deleteButton = findViewById(R.id.trashIcon);
        deleteButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete selected contacts")
                    .setMessage("Are you sure you want to delete the selected contacts?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        ((ContactDetailsAdapter) contactDetailsAdapter).deleteSelectedContacts(databaseHelper);
                        // Refresh search results after deletion
                        allContacts = databaseHelper.getAllContacts();
                        String currentQuery = searchView.getQuery().toString();
                        if (!currentQuery.isEmpty()) {
                            performSearch(currentQuery);
                        } else {
                            updateRecyclerView(allContacts);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    // Set up adapter listeners
    private void setupAdapterListeners() {
        // Selection mode listener
        ((ContactDetailsAdapter) contactDetailsAdapter).setSelectionModeListener(isSelectionMode -> {
            optionsBar.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        });

        // Edit listener
        ((ContactDetailsAdapter) contactDetailsAdapter).setOnContactClickListener((contact, position) -> {
            Intent intent = new Intent(this, ContactActivity.class);
            intent.putExtra("contactIdToEdit", contact.getId());
            intent.putExtra("isEditMode", true);
            startActivity(intent);
        });

        // Checkbox selection change listener
        ((ContactDetailsAdapter) contactDetailsAdapter).setContactSelectionChangeListener(isAllContactsSelected -> {
            if (isAllContactsSelected) {
                selectAllButton.setText("Deselect All");
            } else {
                selectAllButton.setText("Select All");
            }
        });
    }

    // Toggle selection mode
    private void toggleCheckboxSelectionMode() {
        if (selectAllButton.getText().toString().equals("Select All")) {
            ((ContactDetailsAdapter) contactDetailsAdapter).selectAllContacts();
            selectAllButton.setText("Deselect All");
        } else {
            ((ContactDetailsAdapter) contactDetailsAdapter).deselectAllContacts();
            selectAllButton.setText("Select All");
        }
    }

    // Set up the search view
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Show all contacts when search is empty
                    updateRecyclerView(allContacts);
                } else {
                    performSearch(newText);
                }
                return true;
            }
        });
    }

    // Update RecyclerView per search
    private void updateRecyclerView(List<Contact> contacts) {
        contactDetailsAdapter = new ContactDetailsAdapter(new ArrayList<>(contacts));
        recyclerView.setAdapter(contactDetailsAdapter);

        // Re-setup listeners for the new adapter
        setupAdapterListeners();
    }

    // Check if an entry contains a keyword (case insensitive)
    private boolean containsIgnoreCase(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    private boolean hikeContainsKeyword(Contact contact, String keyword) {
        // Search in all text fields
        return containsIgnoreCase(contact.getName(), keyword) ||
               containsIgnoreCase(contact.getEmail(), keyword) ||
               containsIgnoreCase(contact.getDateOfBirth(), keyword);
    }

    // Perform search and update RecyclerView
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            updateRecyclerView(allContacts);
            return;
        }

        String searchQuery = query.trim().toLowerCase();
        List<Contact> filteredContacts = new ArrayList<>();

        for (Contact hike : allContacts) {
            if (hikeContainsKeyword(hike, searchQuery)) {
                filteredContacts.add(hike);
            }
        }

        updateRecyclerView(filteredContacts);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Focus
        searchView.requestFocus();
        // Refresh data when returning from edit activity
        allContacts = dbHelper.getAllContacts();
        String currentQuery = searchView.getQuery().toString();
        if (!currentQuery.isEmpty()) {
            performSearch(currentQuery);
        } else {
            updateRecyclerView(allContacts);
        }
    }
}