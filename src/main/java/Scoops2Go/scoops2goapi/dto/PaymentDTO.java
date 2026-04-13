package Scoops2Go.scoops2goapi.dto;

public record PaymentDTO(
        boolean success,
        String transactionId,
        String message
) {}
