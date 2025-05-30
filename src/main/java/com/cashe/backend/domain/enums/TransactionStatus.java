package com.cashe.backend.domain.enums;

public enum TransactionStatus {
    PENDING_APPROVAL, // Para transacciones futuras que requieren aprobación
    APPROVED, // Transacción aprobada y efectiva
    REJECTED, // Transacción futura rechazada
    CANCELLED // Transacción que fue aprobada y luego cancelada (requiere lógica de anulación)
}