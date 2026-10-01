package com.multistorecommerce.store.application;

import com.multistorecommerce.store.infrastructure.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StoreService {
    private final StoreRepository repository;
    public StoreService(StoreRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<StoreSummary> listActive() {
        return repository.findByActiveTrueOrderBySlugAsc().stream()
            .map(store -> new StoreSummary(store.getId(), store.getSlug(), store.getName())).toList();
    }
}
