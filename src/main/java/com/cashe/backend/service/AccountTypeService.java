package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.AccountTypeCreateRequest;
import com.cashe.backend.service.dto.AccountTypeDto;
import com.cashe.backend.service.dto.AccountTypeUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface AccountTypeService {

    // Para tipos de cuenta definidos por el usuario
    AccountTypeDto createAccountType(AccountTypeCreateRequest createRequest, User user);

    Optional<AccountTypeDto> getAccountTypeByIdAndUser(Long id, User user); // Específico del usuario, no predefinido

    List<AccountTypeDto> getAccountTypesForUser(User user); // Incluye predefinidos y del usuario, ordenados

    AccountTypeDto updateAccountType(Long id, AccountTypeUpdateRequest updateRequest, User user); // Solo para los del
                                                                                                  // usuario

    void deleteAccountType(Long id, User user); // Solo para los del usuario, con validación de no estar en uso

    // Para tipos de cuenta predefinidos
    List<AccountTypeDto> getPredefinedAccountTypes();

    // Un método general para buscar por ID, podría ser útil internamente o para
    // otros servicios
    // Comentado por ahora, ya que los repositorios se pueden usar directamente para
    // esto si es interno.
    // Optional<AccountType> findById(Long id);
}