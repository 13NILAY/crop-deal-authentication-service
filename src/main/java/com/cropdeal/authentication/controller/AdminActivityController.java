package com.cropdeal.authentication.controller;

import com.cropdeal.authentication.dto.AdminActivityRequest;
import com.cropdeal.authentication.model.AdminActivityLog;
import com.cropdeal.authentication.service.AdminActivityService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminActivityController {
    private final AdminActivityService service;

    public AdminActivityController(AdminActivityService service) {
        this.service = service;
    }

    @GetMapping("/admin/activity")
    public Page<AdminActivityLog> list(@RequestParam(required = false) String adminId,
                                       @RequestParam(required = false) String action,
                                       @RequestParam(required = false) String entityType,
                                       @RequestParam(required = false) LocalDateTime from,
                                       @RequestParam(required = false) LocalDateTime to,
                                       @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
                                       Pageable pageable) {
        return service.search(adminId, action, entityType, from, to, pageable);
    }

    @PostMapping("/internal/admin/activity")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminActivityLog record(@Valid @RequestBody AdminActivityRequest request, Principal principal) {
        return service.record(principal.getName(), request);
    }
}