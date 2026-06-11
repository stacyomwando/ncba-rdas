package com.ncba.rdas.service;

import com.ncba.rdas.client.CountryInfoSoapClient;
import com.ncba.rdas.config.CacheConfig;
import com.ncba.rdas.dto.ApiResponse;
import com.ncba.rdas.dto.PagedResponse;
import com.ncba.rdas.model.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CountryService {

    private static final Logger log = LoggerFactory.getLogger(CountryService.class);
    private final CountryInfoSoapClient soapClient;

    public CountryService(CountryInfoSoapClient soapClient) {
        this.soapClient = soapClient;
    }

    // ── Cache warming (called on startup + daily scheduler) ──────────────────

    @CachePut(value = CacheConfig.COUNTRIES, key = "'all'")
    public List<Country> warmCountries() {
        log.info("Warming country cache...");
        List<Country> list = soapClient.fetchAllCountries();
        log.info("Cached {} countries", list.size());
        return list;
    }

    @CachePut(value = CacheConfig.CONTINENTS, key = "'all'")
    public List<String[]> warmContinents() {
        return soapClient.fetchAllContinents();
    }

    @CachePut(value = CacheConfig.CURRENCIES, key = "'all'")
    public List<String[]> warmCurrencies() {
        return soapClient.fetchAllCurrencies();
    }

    @CachePut(value = CacheConfig.LANGUAGES, key = "'all'")
    public List<String[]> warmLanguages() {
        return soapClient.fetchAllLanguages();
    }

    // ── Cache reads ───────────────────────────────────────────────────────────

    @Cacheable(value = CacheConfig.COUNTRIES, key = "'all'")
    public List<Country> getCountries() {
        return warmCountries();
    }

    @Cacheable(value = CacheConfig.CONTINENTS, key = "'all'")
    public List<String[]> getContinents() {
        return warmContinents();
    }

    @Cacheable(value = CacheConfig.CURRENCIES, key = "'all'")
    public List<String[]> getCurrencies() {
        return warmCurrencies();
    }

    @Cacheable(value = CacheConfig.LANGUAGES, key = "'all'")
    public List<String[]> getLanguages() {
        return warmLanguages();
    }

    // ── Business logic ────────────────────────────────────────────────────────

    public PagedResponse<Country> searchCountries(
            String search, String continent, String currency, String language,
            int page, int size, String sortBy, String sortDir) {

        List<Country> all = getCountries();

        List<Country> filtered = all.stream()
                .filter(c -> search == null || search.isBlank() ||
                        c.getName().toLowerCase().contains(search.toLowerCase()) ||
                        c.getIsoCode().equalsIgnoreCase(search))
                .filter(c -> continent == null || continent.isBlank() ||
                        c.getContinentCode().equalsIgnoreCase(continent))
                .filter(c -> currency == null || currency.isBlank() ||
                        c.getCurrencyIsoCode().equalsIgnoreCase(currency))
                .filter(c -> language == null || language.isBlank() ||
                        (c.getLanguages() != null &&
                         c.getLanguages().stream().anyMatch(l -> l.equalsIgnoreCase(language))))
                .collect(Collectors.toList());

        // Sort
        Comparator<Country> cmp = switch (sortBy == null ? "name" : sortBy.toLowerCase()) {
            case "isocode" -> Comparator.comparing(Country::getIsoCode, String.CASE_INSENSITIVE_ORDER);
            case "capital" -> Comparator.comparing(c -> nvl(c.getCapitalCity()));
            case "currency"-> Comparator.comparing(c -> nvl(c.getCurrencyIsoCode()));
            default        -> Comparator.comparing(c -> nvl(c.getName()));
        };
        if ("desc".equalsIgnoreCase(sortDir)) cmp = cmp.reversed();
        filtered.sort(cmp);

        // Paginate
        long total = filtered.size();
        int from = Math.min(page * size, filtered.size());
        int to   = Math.min(from + size, filtered.size());
        return PagedResponse.of(filtered.subList(from, to), page, size, total);
    }

    public Country getByCode(String isoCode) {
        return getCountries().stream()
                .filter(c -> c.getIsoCode().equalsIgnoreCase(isoCode))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Country not found: " + isoCode));
    }

    public PagedResponse<Country> getByCurrency(String currencyCode, int page, int size) {
        List<Country> list = getCountries().stream()
                .filter(c -> c.getCurrencyIsoCode().equalsIgnoreCase(currencyCode))
                .collect(Collectors.toList());
        if (list.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No countries found for currency: " + currencyCode);
        int from = Math.min(page * size, list.size());
        int to   = Math.min(from + size, list.size());
        return PagedResponse.of(list.subList(from, to), page, size, list.size());
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
