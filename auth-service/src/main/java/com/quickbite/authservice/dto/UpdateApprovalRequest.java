package com.quickbite.authservice.dto;

import com.quickbite.authservice.model.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApprovalRequest(
        @NotNull ApprovalStatus approvalStatus,
        String rejectionReason
) {}
