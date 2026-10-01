package com.cropdeal.authentication.controller;

import com.cropdeal.authentication.dto.AdminUserResponse;
import com.cropdeal.authentication.dto.UpdateUserStatusRequest;
import com.cropdeal.authentication.dto.AdminActivityRequest;
import com.cropdeal.authentication.service.AdminActivityService;
import com.cropdeal.authentication.service.AuthenticationService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AuthenticationService authenticationService;
    private final AdminActivityService activityService;

    public AdminUserController(AuthenticationService authenticationService, AdminActivityService activityService) {
        this.authenticationService = authenticationService;
        this.activityService = activityService;
    }

    @GetMapping
    public Page<AdminUserResponse> list(@RequestParam(required = false) String search,
                                        @RequestParam(required = false) String role,
                                        @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return authenticationService.searchUsers(search, role, pageable);
    }

    @GetMapping("/{id}")
    public AdminUserResponse get(@PathVariable Long id) {
        return authenticationService.getUser(id);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponse updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateUserStatusRequest request,
                                          Principal principal) {
        AdminUserResponse updated = authenticationService.updateUserStatus(id, request.active(), principal.getName());
        activityService.record(principal.getName(), new AdminActivityRequest(
            request.active() ? "ADMIN_USER_REACTIVATED" : "ADMIN_USER_SUSPENDED", "USER", id.toString()));
        return updated;
    }
}