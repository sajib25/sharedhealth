package org.sharedhealth.healthId.web.config;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching(proxyTargetClass = true)
public class HealthIdCacheConfiguration implements CachingConfigurer {

    public static final String IDENTITY_CACHE = "identityCache";
    public static final String CACHE_EVICTION_POLICY = "LRU";


    @Bean(destroyMethod = "shutdown", name = "ehCacheManager")
    public net.sf.ehcache.CacheManager ehCacheManager() {

        net.sf.ehcache.config.Configuration ehCacheConfig = new net.sf.ehcache.config.Configuration();
        ehCacheConfig.addCache(getIdentityCacheConfiguration());
        return net.sf.ehcache.CacheManager.newInstance(ehCacheConfig);
    }

    private CacheConfiguration getIdentityCacheConfiguration() {
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setName(IDENTITY_CACHE);
        cacheConfig.setMemoryStoreEvictionPolicy(CACHE_EVICTION_POLICY);
        cacheConfig.setMaxEntriesLocalHeap(500);
        cacheConfig.setTimeToLiveSeconds(2 * 60);
        cacheConfig.persistence(getPersistenceConfiguration());
        return cacheConfig;
    }

    private PersistenceConfiguration getPersistenceConfiguration() {
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.setStrategy("NONE");
        return persistenceConfiguration;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

}
