package com.multistorecommerce.store.api;

import com.multistorecommerce.store.application.StoreService;
import com.multistorecommerce.store.application.StoreSummary;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {
    private final StoreService service;
    public StoreController(StoreService service) { this.service = service; }
    @GetMapping
    public List<StoreSummary> list() { return service.listActive(); }
}
