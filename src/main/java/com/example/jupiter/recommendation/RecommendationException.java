package com.example.jupiter.recommendation;

public class RecommendationException extends RuntimeException{
    public RecommendationException(String errorMessage){
        super(errorMessage);
    }
}
