package com.multistorecommerce.store.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stores")
public class Store {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 80) private String slug;
    @Column(nullable = false, length = 160) private String name;
    @Column(nullable = false) private boolean active;

    protected Store() {}
    public Store(UUID id, String slug, String name, boolean active) {
        this.id = id; this.slug = slug; this.name = name; this.active = active;
    }
    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
