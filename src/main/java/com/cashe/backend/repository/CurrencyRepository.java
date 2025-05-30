package com.cashe.backend.repository;

import com.cashe.backend.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> { // La clave primaria es String (code)

    Optional<Currency> findByIsBaseCurrencyTrue();

}