package com.multistorecommerce.store.infrastructure;

import com.multistorecommerce.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    List<Store> findByActiveTrueOrderBySlugAsc();
}
