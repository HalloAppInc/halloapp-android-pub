package com.halloapp.contacts;

public class Contact {

    final long id;
    final long addressBookId;
    final String name;
    final String phone;
    final String user;
    final boolean member;

    public Contact(long id, long addressBookId, String name, String phone, String user, boolean member) {
        this.id = id;
        this.addressBookId = addressBookId;
        this.name = name;
        this.phone = phone;
        this.user = user;
        this.member = member;
    }
}
