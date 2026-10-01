package com.multistorecommerce.store;

import com.multistorecommerce.store.application.DevelopmentStoreSeed;
import com.multistorecommerce.store.domain.Store;
import com.multistorecommerce.store.infrastructure.StoreRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(properties = {"spring.profiles.active=dev", "app.seed.enabled=true"})
@AutoConfigureMockMvc
class StorePostgresIT {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres@sha256:639ab7ceb90e13123085b741fb31ef493fba25463002f6da665352e7b534b652");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired StoreRepository stores;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired DevelopmentStoreSeed seed;
    @BeforeEach void clear() { stores.deleteAll(); }

    @Test void migrationFiltersAndOrdersRealPostgresRows() throws Exception {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class)).isEqualTo(1);
        stores.saveAllAndFlush(List.of(store("zulu", true), store("alpha", true), store("hidden", false)));
        mvc.perform(get("/api/v1/stores")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].slug").value("alpha"))
            .andExpect(jsonPath("$[1].slug").value("zulu"));
    }
    @Test void emptyDatabaseWorks() throws Exception {
        mvc.perform(get("/api/v1/stores")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void databaseEnforcesConstraints() {
        stores.saveAndFlush(store("unique", true));
        assertThatThrownBy(() -> stores.saveAndFlush(store("unique", true))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> stores.saveAndFlush(store("Invalid Slug", true))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> stores.saveAndFlush(new Store(UUID.randomUUID(), "blank", " ", true)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test void developmentSeedIsRepeatable() throws Exception {
        seed.run(new DefaultApplicationArguments());
        seed.run(new DefaultApplicationArguments());
        assertThat(stores.count()).isEqualTo(3);
        assertThat(stores.findByActiveTrueOrderBySlugAsc()).hasSize(2);
    }
    @Test void operationalAndLocalDocsEndpointsWork() throws Exception {
        for (String path : List.of("/actuator/health/liveness", "/actuator/health/readiness")) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
        }
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/stores'].get").exists());
    }
    private Store store(String slug, boolean active) { return new Store(UUID.randomUUID(), slug, "Fictional " + slug, active); }
}
