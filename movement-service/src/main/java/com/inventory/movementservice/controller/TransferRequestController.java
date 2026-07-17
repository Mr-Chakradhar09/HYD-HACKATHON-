package com.inventory.movementservice.controller;

import com.inventory.movementservice.dto.request.ApproveTransferRequest;
import com.inventory.movementservice.dto.request.CreateTransferRequestRequest;
import com.inventory.movementservice.dto.request.DispatchTransferRequest;
import com.inventory.movementservice.dto.request.ReceiveTransferRequest;
import com.inventory.movementservice.dto.request.RejectTransferRequest;
import com.inventory.movementservice.dto.response.ApiResponse;
import com.inventory.movementservice.dto.response.TransferRequestResponse;
import com.inventory.movementservice.service.TransferRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transfer-requests")
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    public TransferRequestController(TransferRequestService transferRequestService) {
        this.transferRequestService = transferRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> createTransferRequest(
            @Valid @RequestBody CreateTransferRequestRequest request,
            Authentication authentication) {
        TransferRequestResponse response = transferRequestService.createTransferRequest(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Transfer request created successfully. Awaiting approval.", response, HttpStatus.CREATED.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> getTransferRequest(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer request fetched", transferRequestService.getTransferRequest(id), HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TransferRequestResponse>>> getAllTransferRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer requests fetched", transferRequestService.getAllTransferRequests(pageable), HttpStatus.OK.value()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<TransferRequestResponse>>> getPendingTransferRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending transfer requests fetched", transferRequestService.getPendingTransferRequests(pageable), HttpStatus.OK.value()));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TransferRequestResponse>>> getMyTransferRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "My transfer requests fetched", transferRequestService.getMyTransferRequests(authentication.getName(), pageable), HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> approveTransferRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApproveTransferRequest request,
            Authentication authentication) {
        if (request == null) request = new ApproveTransferRequest();
        TransferRequestResponse response = transferRequestService.approveTransferRequest(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer request approved. Movement created.", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> rejectTransferRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectTransferRequest request,
            Authentication authentication) {
        TransferRequestResponse response = transferRequestService.rejectTransferRequest(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer request rejected.", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> cancelTransferRequest(
            @PathVariable Long id,
            Authentication authentication) {
        TransferRequestResponse response = transferRequestService.cancelTransferRequest(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer request cancelled.", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> dispatchTransferRequest(
            @PathVariable Long id,
            @Valid @RequestBody DispatchTransferRequest request,
            Authentication authentication) {
        TransferRequestResponse response = transferRequestService.dispatchTransferRequest(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer dispatched. Stock moved out.", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> receiveTransferRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveTransferRequest request,
            Authentication authentication) {
        TransferRequestResponse response = transferRequestService.receiveTransferRequest(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer received. Stock moved in.", response, HttpStatus.OK.value()));
    }

    @GetMapping("/for-dispatch")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<Page<TransferRequestResponse>>> getTransfersForDispatch(
            @RequestParam Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfers for dispatch fetched", transferRequestService.getTransfersForDispatch(warehouseId, pageable), HttpStatus.OK.value()));
    }

    @GetMapping("/for-receipt")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<Page<TransferRequestResponse>>> getTransfersForReceipt(
            @RequestParam Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfers for receipt fetched", transferRequestService.getTransfersForReceipt(warehouseId, pageable), HttpStatus.OK.value()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Stats fetched",
                Map.of("pendingCount", transferRequestService.getPendingCount()), HttpStatus.OK.value()));
    }
}
