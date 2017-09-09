package com.marker.contact;


import java.util.ArrayList;
import java.util.List;

public class Contact {
    public final String id;
    String name;
    String phone;
    String email;

    Contact(String id, String name, String phone, String email){
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public static final List<Contact> initializeData(){
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("1", "Eze", "1155555555", "eze@android.com"));
        contacts.add(new Contact("2", "Fer", "1166666666", "fer@android.com"));
        contacts.add(new Contact("3", "Fran", "1177777777", "fran@android.com"));
        contacts.add(new Contact("4", "Sandro", "1188888888", "sandro@android.com"));
        return contacts;
    }
};


