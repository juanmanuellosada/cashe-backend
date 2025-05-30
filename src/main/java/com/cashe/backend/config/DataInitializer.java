package com.cashe.backend.config;

import com.cashe.backend.service.CurrencyService;
import com.cashe.backend.service.dto.CurrencyCreateRequest;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final CurrencyService currencyService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Iniciando la inicialización de datos esenciales...");

        currencyService.createIfNotExists(
                new CurrencyCreateRequest(
                        "ARS",
                        "Argentine Peso",
                        "$",
                        new BigDecimal("1.00"),
                        true));

        currencyService.createIfNotExists(
                new CurrencyCreateRequest(
                        "USD",
                        "US Dollar",
                        "$",
                        new BigDecimal("1160.00"),
                        false));

        currencyService.createIfNotExists(
                new CurrencyCreateRequest(
                        "EUR",
                        "Euro",
                        "€",
                        new BigDecimal("1288.99"),
                        false));

        logger.info("Inicialización de datos completada.");
    }
}
