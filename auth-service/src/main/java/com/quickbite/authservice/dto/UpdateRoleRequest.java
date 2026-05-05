package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {
}
