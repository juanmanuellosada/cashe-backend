package com.cashe.backend.validation.validator;

import com.cashe.backend.service.dto.TransactionCreateRequest; // Asumiendo que el DTO estará aquí
import com.cashe.backend.validation.constraint.EitherAccountIdOrCardId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EitherAccountIdOrCardIdValidator
        implements ConstraintValidator<EitherAccountIdOrCardId, TransactionCreateRequest> {

    @Override
    public void initialize(EitherAccountIdOrCardId constraintAnnotation) {
        // No se necesita inicialización especial
    }

    @Override
    public boolean isValid(TransactionCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Otra validación (ej. @NotNull) debería manejar el caso de request nulo
        }

        boolean accountIdPresent = request.getAccountId() != null;
        boolean cardIdPresent = request.getCardId() != null;

        // Válido si exactamente uno está presente
        return (accountIdPresent && !cardIdPresent) || (!accountIdPresent && cardIdPresent);
    }
}