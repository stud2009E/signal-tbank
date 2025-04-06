package pab.ta.handler.tbank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.tinkoff.piapi.core.InvestApi;

@Configuration
@PropertySource("classpath:secret.properties")
public class Config {
    @Value("${tbank.token.api}")
    private String token;

    @Bean
    public InvestApi investApi() {
        return InvestApi.create(token);
    }

}