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

    public void setName(String name) { this.name = name; }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (id == null) return super.equals(obj);
        if (obj != null && obj instanceof User) {
            return id.equals(((User)obj).id);
        } else {
            return super.equals(obj);
        }
    }
}
