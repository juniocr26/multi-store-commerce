package com.multistorecommerce.store.application;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DevelopmentStoreSeed implements ApplicationRunner {
    private final DataSource dataSource;
    public DevelopmentStoreSeed(DataSource dataSource) { this.dataSource = dataSource; }
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        new ResourceDatabasePopulator(new ClassPathResource("db/dev/stores.sql")).execute(dataSource);
    }
}
