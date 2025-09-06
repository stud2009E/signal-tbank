package pab.ta.handler.tbank.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource("classpath:secret.properties")
@EnableScheduling
@EnableCaching
public class Config {
    @Value("${tbank.token.api}")
    private String token;

    @Bean
    public InvestApi investApi() {
        return InvestApi.createReadonly(token);
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("assets");
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(7, TimeUnit.DAYS)
                        .maximumSize(10_000)
        );

        return cacheManager;
    }
}