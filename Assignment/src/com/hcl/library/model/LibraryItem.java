package com.hcl.library.model;

import java.time.Year;
import java.util.Comparator;

public abstract class LibraryItem implements Borrowable, Comparable<LibraryItem> {
    private static int idCounter = 1001;

    private final int id;
    private String title;
    private int year;
    private boolean available;

    public LibraryItem(String title, int year) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        int currentYear = Year.now().getValue();
        if (year < 1900 || year > currentYear) {
            throw new IllegalArgumentException("Year must be between 1900 and " + currentYear);
        }
        this.id = idCounter++;
        this.title = title.trim();
        this.year = year;
        this.available = true;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void borrow() {
        this.available = false;
    }

    @Override
    public void giveBack() {
        this.available = true;
    }

    public abstract double lateFee(int daysLate);

    @Override
    public int compareTo(LibraryItem other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    public static final Comparator<LibraryItem> BY_YEAR_DESC_THEN_TITLE =
            Comparator.comparing(LibraryItem::getYear, Comparator.reverseOrder())
                    .thenComparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryItem item = (LibraryItem) o;
        return id == item.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return id + ", " + title + ", " + year;
    }
}
