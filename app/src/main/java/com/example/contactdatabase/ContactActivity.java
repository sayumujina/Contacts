package com.example.contactdatabase;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import android.app.DatePickerDialog;
import android.widget.Toast;

import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class ContactActivity extends AppCompatActivity {
    private ImageView selectedAvatar;
    private int selectedAvatarId = R.id.avatar1;
    private int contactIdToEdit = -1;
    EditText editName;
    EditText editEmail;
    EditText editDateOfBirth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contact), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up action bar
        Toolbar toolbar = findViewById(R.id.contactEditToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Change the back button color to white
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }
        // Initialise necessary components
        setupDateSelection();
        setupAvatarSelection();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveContactDetails());
        
        // Remove error indicators when user starts typing\
        editName = findViewById(R.id.editName);
        editName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                removeErrorIndicators();
            }
        });

        editEmail = findViewById(R.id.editEmail);
        editEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                removeErrorIndicators();
            }
        });

        editDateOfBirth = findViewById(R.id.editDateOfBirth);
        editDateOfBirth.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                removeErrorIndicators();
            }
        });

        if (getIntent() != null && getIntent().hasExtra("isEditMode")) {
            contactIdToEdit = getIntent().getIntExtra("contactIdToEdit", -1);
            if (contactIdToEdit != -1) {
                loadExistingData();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Load existing data if in edit mode
    private void loadExistingData() {
        // Load contact data from database
        try (DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext())) {
            final Contact contact = databaseHelper.getContactById(contactIdToEdit);
            if (contact != null) {
                // Populate fields with existing data
                editName.setText(contact.getName());
                editEmail.setText(contact.getEmail());
                editDateOfBirth.setText(contact.getDateOfBirth());

                // Set selected avatar
                int avatarNumber = contact.getAvatarId();
                ImageView avatarView = findViewById(AvatarConstants.AVATAR_RESOURCES[avatarNumber - 1]);
                selectAvatar(avatarView);
            }
        } catch (Exception e) {
            Log.e("ContactActivity", "Error loading contact data", e);
        }
    }

    // Set up date selection
    private void setupDateSelection() {
        EditText editDateOfBirth = findViewById(R.id.editDateOfBirth);
        editDateOfBirth.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();

            // Getting current year, month and day.
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a DatePickerDialog instance and set it to current date
            DatePickerDialog datePickerDialog = new DatePickerDialog(
            ContactActivity.this,
                (view, chosenYear, chosenMonth, chosenDay) -> {
                    // Set the date to EditText
                    editDateOfBirth.setText(chosenDay + "/" + (chosenMonth + 1) + "/" + chosenYear);
                },
                year, month, day);
            // Show the DatePickerDialog
            datePickerDialog.show();
        });
    }

    // Remove error indicators from all fields
    private void removeErrorIndicators() {
        editName.setError(null);
        editEmail.setError(null);
        editDateOfBirth.setError(null);
    }

    // Set up avatar selection
    private void setupAvatarSelection() {
        ImageView avatar1 = findViewById(R.id.avatar1);
        ImageView avatar2 = findViewById(R.id.avatar2);
        ImageView avatar3 = findViewById(R.id.avatar3);

        View.OnClickListener avatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAvatar(v);
            }
        };

        avatar1.setOnClickListener(avatarClickListener);
        avatar2.setOnClickListener(avatarClickListener);
        avatar3.setOnClickListener(avatarClickListener);
    }

    private void selectAvatar(View clickedAvatar) {
        // Clear previous selection
        if (selectedAvatar != null) {
            selectedAvatar.setSelected(false);
        }

        // Set new selection
        selectedAvatar = (ImageView) clickedAvatar;
        selectedAvatar.setSelected(true);
        selectedAvatarId = clickedAvatar.getId();
    }

    // Get avatar number from avatar ID
    private int getAvatarNumber(int avatarId) {
        String resourceName = getResources().getResourceEntryName(avatarId);
        // Extract the digit after "avatar" (e.g., "avatar1" -> 1)
        return Integer.parseInt(resourceName.replace("avatar", ""));
    }

    // Validate required fields
    // Check for empty fields and non-alphanumeric characters if applicable
    private boolean validateRequiredFields() {
        // Set a flag to avoid returning errors too early
        boolean areAllFieldsFilled = true;

        if (editName.getText().toString().trim().isEmpty()) {
            editName.setError("Name is required");
            areAllFieldsFilled = false;
        } else if (!editName.getText().toString().matches("[a-zA-Z0-9 ]+")) {
            editName.setError("Name can only contain alphanumeric characters and spaces");
            areAllFieldsFilled = false;
        }

        if (editEmail.getText().toString().trim().isEmpty()) {
            editEmail.setError("Email is required");
            areAllFieldsFilled = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail.getText().toString()).matches()) {
            editEmail.setError("Please enter a valid email address");
            areAllFieldsFilled = false;
        }

        if (editDateOfBirth.getText().toString().trim().isEmpty()) {
            editDateOfBirth.setError("Date of Birth is required");
            areAllFieldsFilled = false;
        }

        return areAllFieldsFilled;
    }

    // Save contact details to the database
    private void saveContactDetails() {
        if (!validateRequiredFields()) {
            Toast.makeText(this, "Fail to add contact", Toast.LENGTH_LONG).show();
            return;
        }

        // Insert contact details into the database
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        int avatarNumber = getAvatarNumber(selectedAvatarId);
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String dateOfBirth = editDateOfBirth.getText().toString();

        if (contactIdToEdit != -1) {
            // Update existing contact
            boolean updated = databaseHelper.updateContactDetails(contactIdToEdit, avatarNumber, name, email, dateOfBirth);
            Toast.makeText(this, "Contact detail updated: " + updated, Toast.LENGTH_LONG).show();
        } else {
            // Insert new contact
            long createdContactDetail = databaseHelper.insertContactDetails(avatarNumber, name, email, dateOfBirth);
            Toast.makeText(this, "New contact detail created: " + createdContactDetail, Toast.LENGTH_LONG).show();
        }

        Intent detailsIntent = new Intent(this, ContactDetailsActivity.class);
        startActivity(detailsIntent);
    }
}