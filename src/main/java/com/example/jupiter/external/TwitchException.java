package com.example.jupiter.external;

public class TwitchException extends RuntimeException{
    public TwitchException(String errorMessage){
        super(errorMessage);
    }
}
