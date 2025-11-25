package com.example.contactdatabase;

public class Contact {
   private final int id;
   private final int avatarId;
   private final String name;
   private final String email;
   private final String dateOfBirth;

    public Contact(int id, int avatarId, String name, String email, String dateOfBirth) {
        this.id = id;
        this.avatarId = avatarId;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }
    public int getId() {
        return id;
    }
    public int getAvatarId() {
        return avatarId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

}
