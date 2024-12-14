package pab.ta.handler.tbank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import pab.ta.handler.base.component.rule.BBMinWidthRuleWrapper;
import pab.ta.handler.base.component.task.BaseSignalSelector;
import ru.tinkoff.piapi.core.InvestApi;

@Configuration
@EnableCaching
@ComponentScan(basePackages = "pab.ta.handler.base.component",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {BaseSignalSelector.class, BBMinWidthRuleWrapper.class}
                )
        }
)
@PropertySource("classpath:secret.properties")
public class Config {
    @Value("${tbank.token.api}")
    private String token;

    @Bean
    public InvestApi investApi() {
        return InvestApi.create(token);
    }

}