package com.hcl.library.exception;

public class BorrowLimitExceededException extends LibraryException {
    public BorrowLimitExceededException(String message) {
        super(message);
    }

    public BorrowLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
