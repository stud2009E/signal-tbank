package pab.ta.handler.tbank.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import pab.ta.handler.base.lib.signal.AbstractSignalProducer;
import pab.ta.handler.base.lib.signal.DvgMacdSignalProducer;
import pab.ta.handler.base.lib.signal.MacdSignalProducer;
import pab.ta.handler.base.lib.signal.RsiSignalProducer;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import ru.ttech.piapi.core.connector.ConnectorConfiguration;
import ru.ttech.piapi.core.connector.ServiceStubFactory;
import ru.ttech.piapi.core.connector.SyncStubWrapper;
import ru.ttech.piapi.strategy.candle.backtest.BarsLoader;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource("classpath:secret.properties")
@EnableScheduling
@EnableCaching
public class Config {

    @Bean
    public ConnectorConfiguration connectConfig() {
        return ConnectorConfiguration.loadPropertiesFromResources("secret.properties");
    }

    @Bean
    public BarsLoader barsLoader(ConnectorConfiguration connectConfig) {
        return new BarsLoader(null, connectConfig, Executors.newCachedThreadPool());
    }

    @Bean
    public SyncStubWrapper<InstrumentsServiceBlockingStub> instrumentService(ConnectorConfiguration connectConfig) {
        var factory = ServiceStubFactory.create(connectConfig);

        return factory.newSyncService(InstrumentsServiceGrpc::newBlockingStub);
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

    @Bean
    public List<AbstractSignalProducer> signalProducers() {

        return List.of(
                new RsiSignalProducer(),
                new MacdSignalProducer(),
                new DvgMacdSignalProducer()
        );
    }
}