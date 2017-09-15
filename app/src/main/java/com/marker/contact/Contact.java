package com.marker.contact;


import java.util.ArrayList;
import java.util.List;

public class Contact {
    String name;
    String phone;
    String email;
    Boolean checked;

    Contact(String name, String phone, String email){
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.checked = false;
    }

    public static final List<Contact> initializeData(){
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("Eze", "1155555555", "eze@android.com"));
        contacts.add(new Contact("Fer", "1166666666", "fer@android.com"));
        contacts.add(new Contact("Fran", "1177777777", "fran@android.com"));
        contacts.add(new Contact("Sandro", "1188888888", "sandro@android.com"));
        return contacts;
    }
};


