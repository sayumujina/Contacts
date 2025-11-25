package com.example.contactdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contact_details_database";
    private static final String ID_COLUMN = "id";
    private static final String AVATAR_COLUMN = "avatarId";
    private static final String NAME_COLUMN = "name";
    private static final String EMAIL_COLUMN = "email";
    private static final String DOB_COLUMN = "date_of_birth";

    private final SQLiteDatabase contactDatabase;

    private static final String DATABASE_CREATE_QUERY = String.format(
            "CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s INT NOT NULL, " +
                    "%s TEXT NOT NULL, " +
                    "%s TEXT NOT NULL, " +
                    "%s TEXT NOT NULL);",
            DATABASE_NAME,
            ID_COLUMN,
            AVATAR_COLUMN,
            NAME_COLUMN,
            EMAIL_COLUMN,
            DOB_COLUMN);

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        contactDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DATABASE_CREATE_QUERY);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating database: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
            Log.w(this.getClass().getName(), DATABASE_NAME + " table upgraded from version " + oldVersion + " to " + newVersion);
            onCreate(db);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database: " + e.getMessage());
        }
    }

    // Insert contact details into the database
    public long insertContactDetails(int avatarId, String name, String email, String dateOfBirth) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(AVATAR_COLUMN, avatarId);
        rowValues.put(NAME_COLUMN, name);
        rowValues.put(EMAIL_COLUMN, email);
        rowValues.put(DOB_COLUMN, dateOfBirth);
        return contactDatabase.insertOrThrow(DATABASE_NAME, null, rowValues);
    }

    // Retrieve all contact details from the database
    public ArrayList<Contact> getAllContacts() {
        // Initialise a temporary table to hold contact details
        Cursor results = contactDatabase.query(
                DATABASE_NAME,
                new String[]{ID_COLUMN, AVATAR_COLUMN, NAME_COLUMN, EMAIL_COLUMN, DOB_COLUMN},
                null, null, null, null, "name"
        );

        // Return all contact details as a table
        Contact[] contacts = new Contact[results.getCount()];
        int contactIndex = 0;
        while(results.moveToNext()){
            contacts[contactIndex] =  new Contact(
                    results.getInt(results.getColumnIndexOrThrow(ID_COLUMN)),
                    results.getInt(results.getColumnIndexOrThrow(AVATAR_COLUMN)),
                    results.getString(results.getColumnIndexOrThrow(NAME_COLUMN)),
                    results.getString(results.getColumnIndexOrThrow(EMAIL_COLUMN)),
                    results.getString(results.getColumnIndexOrThrow(DOB_COLUMN))
            );
            // Append contact to the table
            contactIndex++;
        }

        // Closes the cursor to avoid memory leaks
        results.close();

        // Return contacts as a table
        return new ArrayList<>(java.util.Arrays.asList(contacts));
    }

    // Delete contact by ID
    public void deleteContactById(int contactId) {
        String whereClause = ID_COLUMN + " = ?";
        String[] whereArgs = { String.valueOf(contactId) };
        contactDatabase.delete(DATABASE_NAME, whereClause, whereArgs);
    }

    // Get contact by ID
    public Contact getContactById(int contactId) {
        String selection = ID_COLUMN + " = ?";
        String[] selectionArgs = {String.valueOf(contactId)};
        Cursor results = contactDatabase.query(
                DATABASE_NAME,
                new String[]{ID_COLUMN, AVATAR_COLUMN, NAME_COLUMN, EMAIL_COLUMN, DOB_COLUMN},
                selection, selectionArgs, null, null, null
        );
        Contact contact = null;
        if (results.moveToFirst()) {
            contact = new Contact(
                    results.getInt(results.getColumnIndexOrThrow(ID_COLUMN)),
                    results.getInt(results.getColumnIndexOrThrow(AVATAR_COLUMN)),
                    results.getString(results.getColumnIndexOrThrow(NAME_COLUMN)),
                    results.getString(results.getColumnIndexOrThrow(EMAIL_COLUMN)),
                    results.getString(results.getColumnIndexOrThrow(DOB_COLUMN))
            );
            Log.d("DatabaseHelper", "Retrieved Contact by ID: " + contact.getName());
        }
        results.close();
        return contact;
    }

    // Update contact details
    public boolean updateContactDetails(int contactId, int avatarId, String name, String email, String dateOfBirth) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(AVATAR_COLUMN, avatarId);
        rowValues.put(NAME_COLUMN, name);
        rowValues.put(EMAIL_COLUMN, email);
        rowValues.put(DOB_COLUMN, dateOfBirth);

        String whereClause = ID_COLUMN + " = ?";
        String[] whereArgs = { String.valueOf(contactId) };

        int rowsAffected = contactDatabase.update(DATABASE_NAME, rowValues, whereClause, whereArgs);
        return rowsAffected > 0;
    }
}


