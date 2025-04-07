package com.capstone.withyou.exception;

public class StockAlreadyInWatchListException extends RuntimeException {
    public StockAlreadyInWatchListException(String message) {
        super(message);
    }
}
