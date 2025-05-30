package com.cashe.backend.controller;

import com.cashe.backend.service.CurrencyService;
import com.cashe.backend.service.dto.CurrencyCreateRequest;
import com.cashe.backend.service.dto.CurrencyDto;
import com.cashe.backend.service.dto.CurrencyUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping
    public ResponseEntity<CurrencyDto> createCurrency(@Valid @RequestBody CurrencyCreateRequest createRequest) {
        CurrencyDto createdCurrency = currencyService.createCurrency(createRequest);
        return new ResponseEntity<>(createdCurrency, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CurrencyDto> getCurrencyByCode(@PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CurrencyDto>> getAllCurrencies() {
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @PutMapping("/{code}")
    public ResponseEntity<CurrencyDto> updateCurrency(@PathVariable String code,
            @Valid @RequestBody CurrencyUpdateRequest updateRequest) {
        CurrencyDto updatedCurrency = currencyService.updateCurrency(code, updateRequest);
        return ResponseEntity.ok(updatedCurrency);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable String code) {
        currencyService.deleteCurrency(code);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{code}/set-base")
    public ResponseEntity<CurrencyDto> setBaseCurrency(@PathVariable String code) {
        CurrencyDto baseCurrency = currencyService.setBaseCurrency(code);
        return ResponseEntity.ok(baseCurrency);
    }

    @GetMapping("/base")
    public ResponseEntity<CurrencyDto> getBaseCurrency() {
        return currencyService.getBaseCurrency()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}