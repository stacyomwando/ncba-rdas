package com.ncba.rdas.scheduler;

import com.ncba.rdas.service.CountryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheWarmupScheduler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupScheduler.class);
    private final CountryService countryService;

    public CacheWarmupScheduler(CountryService countryService) {
        this.countryService = countryService;
    }

    /** Runs once at startup — pre-loads all caches so the first request is instant */
    @Override
    public void run(ApplicationArguments args) {
        log.info("=== RDAS: warming caches on startup ===");
        refreshAll();
        log.info("=== RDAS: cache warm-up complete ===");
    }

    /** Refreshes every 24 hours at midnight — silent background refresh */
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledRefresh() {
        log.info("=== RDAS: scheduled cache refresh ===");
        refreshAll();
    }

    private void refreshAll() {
        try {
            countryService.warmCountries();
            countryService.warmContinents();
            countryService.warmCurrencies();
            countryService.warmLanguages();
        } catch (Exception e) {
            log.error("Cache refresh failed (stale data served): {}", e.getMessage());
        }
    }
}
