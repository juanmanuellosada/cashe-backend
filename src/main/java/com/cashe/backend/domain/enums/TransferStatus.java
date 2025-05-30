package com.cashe.backend.domain.enums;

public enum TransferStatus {
    PENDING, // La transferencia está programada o esperando alguna condición
    COMPLETED, // La transferencia se ha completado exitosamente
    CANCELLED, // La transferencia fue cancelada antes de completarse
    FAILED // La transferencia falló
}