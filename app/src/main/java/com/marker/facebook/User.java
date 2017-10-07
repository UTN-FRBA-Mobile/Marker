package com.marker.facebook;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
