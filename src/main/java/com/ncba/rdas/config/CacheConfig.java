package com.ncba.rdas.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    public static final String COUNTRIES  = "countries";
    public static final String CONTINENTS = "continents";
    public static final String CURRENCIES = "currencies";
    public static final String LANGUAGES  = "languages";

    @Value("${cache.ttl.hours:24}")
    private long ttlHours;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager(COUNTRIES, CONTINENTS, CURRENCIES, LANGUAGES);
        mgr.setCaffeine(Caffeine.newBuilder().expireAfterWrite(ttlHours, TimeUnit.HOURS).maximumSize(1000).recordStats());
        return mgr;
    }
}
