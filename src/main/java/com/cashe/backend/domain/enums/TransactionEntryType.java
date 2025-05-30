package com.cashe.backend.domain.enums;

public enum TransactionEntryType {
    DEBIT, // Salida de una cuenta, o aumento de deuda en tarjeta
    CREDIT // Entrada a una cuenta, o disminución de deuda en tarjeta (pago/reembolso)
}