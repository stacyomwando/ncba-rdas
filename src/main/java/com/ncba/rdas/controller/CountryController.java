package com.ncba.rdas.controller;

import com.ncba.rdas.dto.ApiResponse;
import com.ncba.rdas.dto.PagedResponse;
import com.ncba.rdas.model.Country;
import com.ncba.rdas.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reference Data", description = "Country, currency, language and geographical reference data")
public class CountryController {

    private final CountryService service;

    public CountryController(CountryService service) {
        this.service = service;
    }

    @GetMapping("/countries")
    @Operation(summary = "Search countries", description = "Filter by name, continent, currency, language. Supports pagination and sorting.")
    public ResponseEntity<PagedResponse<Country>> searchCountries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String continent,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {
        return ResponseEntity.ok(service.searchCountries(search, continent, currency, language, page, size, sortBy, sortDir));
    }

    @GetMapping("/countries/{isoCode}")
    @Operation(summary = "Get country by ISO code", description = "Returns full details for a single country.")
    public ResponseEntity<ApiResponse<Country>> getCountry(@PathVariable String isoCode) {
        return ResponseEntity.ok(ApiResponse.success(service.getByCode(isoCode)));
    }

    @GetMapping("/countries/currency/{currencyCode}")
    @Operation(summary = "Get countries by currency", description = "All countries sharing the same currency code.")
    public ResponseEntity<PagedResponse<Country>> getByCurrency(
            @PathVariable String currencyCode,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getByCurrency(currencyCode, page, size));
    }

    @GetMapping("/continents")
    @Operation(summary = "List all continents")
    public ResponseEntity<ApiResponse<List<String[]>>> getContinents() {
        return ResponseEntity.ok(ApiResponse.success(service.getContinents()));
    }

    @GetMapping("/currencies")
    @Operation(summary = "List all currencies")
    public ResponseEntity<ApiResponse<List<String[]>>> getCurrencies() {
        return ResponseEntity.ok(ApiResponse.success(service.getCurrencies()));
    }

    @GetMapping("/languages")
    @Operation(summary = "List all languages")
    public ResponseEntity<ApiResponse<List<String[]>>> getLanguages() {
        return ResponseEntity.ok(ApiResponse.success(service.getLanguages()));
    }
}
