package com.hcl.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Member {
    private static int idCounter = 1;

    private final int id;
    private String name;
    private String email;
    private final List<LibraryItem> borrowedItems = new ArrayList<>();

    public Member(int id, String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'");
        }
        this.id = id;
        this.name = name.trim();
        this.email = email.trim();
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }

    public Member(String name, String email) {
        this(idCounter++, name, email);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<LibraryItem> getBorrowedItems() {
        return Collections.unmodifiableList(borrowedItems);
    }

    public void addBorrowedItem(LibraryItem item) {
        if (item != null && !borrowedItems.contains(item)) {
            borrowedItems.add(item);
        }
    }

    public boolean removeBorrowedItem(LibraryItem item) {
        return borrowedItems.remove(item);
    }

    @Override
    public String toString() {
        return "Member[" + id + ", " + name + ", " + email + "]";
    }
}
