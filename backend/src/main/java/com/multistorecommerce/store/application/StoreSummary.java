package com.multistorecommerce.store.application;

import java.util.UUID;

public record StoreSummary(UUID id, String slug, String name) {}
