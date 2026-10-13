package com.hcl.library.model;

public class Magazine extends LibraryItem {
    public static final double DAILY_FEE = 1.0;

    private int issueNumber;

    public Magazine(String title, int year, int issueNumber) {
        super(title, year);
        if (issueNumber <= 0) {
            throw new IllegalArgumentException("Issue number must be positive");
        }
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public double lateFee(int daysLate) {
        return daysLate > 0 ? daysLate * DAILY_FEE : 0.0;
    }

    @Override
    public String toString() {
        return "Magazine[" + super.toString() + ", Issue #" + issueNumber + "]";
    }
}
