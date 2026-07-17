package com.inventory.movementservice.service.impl;

import com.inventory.movementservice.dto.request.ApproveTransferRequest;
import com.inventory.movementservice.dto.request.CreateTransferRequestRequest;
import com.inventory.movementservice.dto.request.DispatchTransferRequest;
import com.inventory.movementservice.dto.request.ReceiveTransferRequest;
import com.inventory.movementservice.dto.request.RejectTransferRequest;
import com.inventory.movementservice.dto.response.ApiResponse;
import com.inventory.movementservice.dto.response.TransferRequestResponse;
import com.inventory.movementservice.entity.InventoryMovement;
import com.inventory.movementservice.entity.TransferRequest;
import com.inventory.movementservice.enums.MovementStatus;
import com.inventory.movementservice.enums.MovementType;
import com.inventory.movementservice.enums.ReferenceType;
import com.inventory.movementservice.enums.TransferRequestStatus;
import com.inventory.movementservice.event.KafkaEventPublisher;
import com.inventory.movementservice.event.MovementEvent;
import com.inventory.movementservice.exception.InvalidMovementException;
import com.inventory.movementservice.exception.ResourceNotFoundException;
import com.inventory.movementservice.feign.ProductFeignClient;
import com.inventory.movementservice.feign.WarehouseFeignClient;
import com.inventory.movementservice.repository.InventoryMovementRepository;
import com.inventory.movementservice.repository.TransferRequestRepository;
import com.inventory.movementservice.service.MovementService;
import com.inventory.movementservice.service.TransferRequestService;
import com.inventory.movementservice.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransferRequestServiceImpl implements TransferRequestService {

    private static final Logger log = LoggerFactory.getLogger(TransferRequestServiceImpl.class);
    private static final AtomicLong counter = new AtomicLong(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TransferRequestRepository transferRequestRepository;
    private final InventoryMovementRepository movementRepository;
    private final WarehouseFeignClient warehouseFeignClient;
    private final KafkaEventPublisher eventPublisher;

    public TransferRequestServiceImpl(TransferRequestRepository transferRequestRepository,
                                       InventoryMovementRepository movementRepository,
                                       WarehouseFeignClient warehouseFeignClient,
                                       KafkaEventPublisher eventPublisher) {
        this.transferRequestRepository = transferRequestRepository;
        this.movementRepository = movementRepository;
        this.warehouseFeignClient = warehouseFeignClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransferRequestResponse createTransferRequest(CreateTransferRequestRequest request, String requestedBy) {
        // Only INVENTORY_MANAGER and PROCUREMENT_MANAGER can create transfers
        if (!SecurityUtil.hasRole("INVENTORY_MANAGER") && !SecurityUtil.hasRole("PROCUREMENT_MANAGER")) {
            throw new InvalidMovementException("Only Inventory Manager and Procurement Manager can create transfer requests.");
        }

        // Validate source != destination
        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new InvalidMovementException("Source and destination warehouses must be different.");
        }

        // Validate warehouses exist
        validateWarehouse(request.getSourceWarehouseId());
        validateWarehouse(request.getDestinationWarehouseId());

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setRequestNumber(generateRequestNumber());
        transferRequest.setProductId(request.getProductId());
        transferRequest.setSourceWarehouseId(request.getSourceWarehouseId());
        transferRequest.setDestinationWarehouseId(request.getDestinationWarehouseId());
        transferRequest.setQuantity(request.getQuantity());
        transferRequest.setReason(request.getReason());
        transferRequest.setRemarks(request.getRemarks());
        transferRequest.setRequestedBy(requestedBy);
        transferRequest.setStatus(TransferRequestStatus.PENDING);

        transferRequest = transferRequestRepository.save(transferRequest);
        return mapToResponse(transferRequest);
    }

    @Override
    @Transactional
    public TransferRequestResponse approveTransferRequest(Long requestId, ApproveTransferRequest request, String approvedBy) {
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found with id: " + requestId));

        if (transferRequest.getStatus() != TransferRequestStatus.PENDING) {
            throw new InvalidMovementException("Only PENDING requests can be approved. Current status: " + transferRequest.getStatus());
        }

        // Check approval authority
        validateApprovalAuthority(approvedBy, transferRequest);

        // Approve
        transferRequest.setStatus(TransferRequestStatus.APPROVED);
        transferRequest.setApprovedBy(approvedBy);
        transferRequest.setApprovedAt(LocalDateTime.now());
        if (request.getRemarks() != null) {
            transferRequest.setRemarks(request.getRemarks());
        }
        transferRequest = transferRequestRepository.save(transferRequest);

        return mapToResponse(transferRequest);
    }

    @Override
    @Transactional
    public TransferRequestResponse rejectTransferRequest(Long requestId, RejectTransferRequest request, String rejectedBy) {
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found with id: " + requestId));

        if (transferRequest.getStatus() != TransferRequestStatus.PENDING) {
            throw new InvalidMovementException("Only PENDING requests can be rejected. Current status: " + transferRequest.getStatus());
        }

        // Check rejection authority (same as approval)
        validateApprovalAuthority(rejectedBy, transferRequest);

        transferRequest.setStatus(TransferRequestStatus.REJECTED);
        transferRequest.setApprovedBy(rejectedBy);
        transferRequest.setApprovedAt(LocalDateTime.now());
        transferRequest.setRejectionReason(request.getRejectionReason());
        transferRequest = transferRequestRepository.save(transferRequest);

        return mapToResponse(transferRequest);
    }

    @Override
    @Transactional
    public TransferRequestResponse cancelTransferRequest(Long requestId, String cancelledBy) {
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found with id: " + requestId));

        if (transferRequest.getStatus() != TransferRequestStatus.PENDING) {
            throw new InvalidMovementException("Only PENDING requests can be cancelled.");
        }

        if (!transferRequest.getRequestedBy().equals(cancelledBy)) {
            throw new InvalidMovementException("Only the requester can cancel their transfer request.");
        }

        transferRequest.setStatus(TransferRequestStatus.CANCELLED);
        transferRequest = transferRequestRepository.save(transferRequest);

        return mapToResponse(transferRequest);
    }

    @Override
    @Transactional
    public TransferRequestResponse dispatchTransferRequest(Long requestId, DispatchTransferRequest request, String dispatchedBy) {
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found with id: " + requestId));

        if (transferRequest.getStatus() != TransferRequestStatus.APPROVED) {
            throw new InvalidMovementException("Only APPROVED requests can be dispatched. Current status: " + transferRequest.getStatus());
        }

        if (request.getDispatchedQuantity() > transferRequest.getQuantity()) {
            throw new InvalidMovementException("Dispatched quantity (" + request.getDispatchedQuantity() + ") cannot exceed requested quantity (" + transferRequest.getQuantity() + ").");
        }

        // Validate dispatch authority: WAREHOUSE_OPERATOR or INVENTORY_MANAGER at SOURCE warehouse
        boolean canDispatch = SecurityUtil.hasRole("SYSTEM_ADMIN")
                || isEmployeeAssignedToWarehouse(dispatchedBy, transferRequest.getSourceWarehouseId());
        if (!canDispatch) {
            throw new InvalidMovementException("You are not authorized to dispatch from this warehouse.");
        }

        transferRequest.setStatus(TransferRequestStatus.DISPATCHED);
        transferRequest.setDispatchedQuantity(request.getDispatchedQuantity());
        transferRequest.setDispatchedAt(LocalDateTime.now());
        transferRequest.setDispatchedBy(dispatchedBy);
        if (request.getRemarks() != null) {
            transferRequest.setRemarks(request.getRemarks());
        }
        transferRequest = transferRequestRepository.save(transferRequest);

        // Create TRANSFER_OUT movement
        InventoryMovement movement = createDispatchMovement(transferRequest);
        transferRequest.setDispatchMovementId(movement.getId());
        transferRequest.setMovementId(movement.getId());
        transferRequest = transferRequestRepository.save(transferRequest);

        return mapToResponse(transferRequest);
    }

    @Override
    @Transactional
    public TransferRequestResponse receiveTransferRequest(Long requestId, ReceiveTransferRequest request, String receivedBy) {
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found with id: " + requestId));

        if (transferRequest.getStatus() != TransferRequestStatus.DISPATCHED) {
            throw new InvalidMovementException("Only DISPATCHED requests can be received. Current status: " + transferRequest.getStatus());
        }

        if (request.getReceivedQuantity() > transferRequest.getDispatchedQuantity()) {
            throw new InvalidMovementException("Received quantity (" + request.getReceivedQuantity() + ") cannot exceed dispatched quantity (" + transferRequest.getDispatchedQuantity() + ").");
        }

        // Validate receive authority: at DESTINATION warehouse
        boolean canReceive = SecurityUtil.hasRole("SYSTEM_ADMIN")
                || isEmployeeAssignedToWarehouse(receivedBy, transferRequest.getDestinationWarehouseId());
        if (!canReceive) {
            throw new InvalidMovementException("You are not authorized to receive at this warehouse.");
        }

        transferRequest.setStatus(TransferRequestStatus.COMPLETED);
        transferRequest.setReceivedQuantity(request.getReceivedQuantity());
        transferRequest.setReceivedAt(LocalDateTime.now());
        transferRequest.setReceivedBy(receivedBy);
        if (request.getRemarks() != null) {
            transferRequest.setRemarks(request.getRemarks());
        }
        transferRequest = transferRequestRepository.save(transferRequest);

        // Create TRANSFER_IN movement
        InventoryMovement movement = createReceiveMovement(transferRequest);
        transferRequest.setReceiveMovementId(movement.getId());
        transferRequest = transferRequestRepository.save(transferRequest);

        return mapToResponse(transferRequest);
    }

    @Override
    public Page<TransferRequestResponse> getTransfersForDispatch(Long warehouseId, Pageable pageable) {
        return transferRequestRepository.findBySourceWarehouseIdAndStatus(warehouseId, TransferRequestStatus.APPROVED, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<TransferRequestResponse> getTransfersForReceipt(Long warehouseId, Pageable pageable) {
        return transferRequestRepository.findByDestinationWarehouseIdAndStatus(warehouseId, TransferRequestStatus.DISPATCHED, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public TransferRequestResponse getTransferRequest(Long requestId) {
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found with id: " + requestId));
        return mapToResponse(transferRequest);
    }

    @Override
    public Page<TransferRequestResponse> getAllTransferRequests(Pageable pageable) {
        return transferRequestRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<TransferRequestResponse> getPendingTransferRequests(Pageable pageable) {
        return transferRequestRepository.findByStatus(TransferRequestStatus.PENDING, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<TransferRequestResponse> getMyTransferRequests(String requestedBy, Pageable pageable) {
        return transferRequestRepository.findByRequestedBy(requestedBy, pageable).map(this::mapToResponse);
    }

    @Override
    public long getPendingCount() {
        return transferRequestRepository.countByStatus(TransferRequestStatus.PENDING);
    }

    @Override
    public long getDispatchCount(Long warehouseId) {
        return transferRequestRepository.findBySourceWarehouseIdAndStatus(warehouseId, TransferRequestStatus.APPROVED, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public long getReceiptCount(Long warehouseId) {
        return transferRequestRepository.findByDestinationWarehouseIdAndStatus(warehouseId, TransferRequestStatus.DISPATCHED, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
    }

    private void validateApprovalAuthority(String approverCode, TransferRequest transferRequest) {
        // SYSTEM_ADMIN can always approve
        if (SecurityUtil.hasRole("SYSTEM_ADMIN")) {
            return;
        }

        // PROCUREMENT_MANAGER can always approve (central approval)
        if (SecurityUtil.hasRole("PROCUREMENT_MANAGER")) {
            return;
        }

        // INVENTORY_MANAGER can approve only if they are assigned to the DESTINATION warehouse
        if (SecurityUtil.hasRole("INVENTORY_MANAGER")) {
            boolean assignedToDestination = isEmployeeAssignedToWarehouse(approverCode, transferRequest.getDestinationWarehouseId());
            if (assignedToDestination) {
                return;
            }
            // INVENTORY_MANAGER assigned to source warehouse CANNOT approve (they are the requester)
            throw new InvalidMovementException(
                    "Inventory Manager assigned to the source warehouse cannot approve their own transfer request. "
                    + "Approval must come from the destination warehouse manager, a Procurement Manager, or System Admin.");
        }

        throw new InvalidMovementException("You do not have authority to approve this transfer request.");
    }

    private boolean isEmployeeAssignedToWarehouse(String employeeCode, Long warehouseId) {
        try {
            ApiResponse<List<Map<String, Object>>> response = warehouseFeignClient.getAssignmentsByWarehouse(warehouseId);
            if (response == null || response.getData() == null) return false;
            return response.getData().stream()
                    .filter(a -> employeeCode.equals(a.get("employeeCode")))
                    .anyMatch(a -> "ACTIVE".equals(a.get("status")));
        } catch (Exception e) {
            log.warn("Could not verify warehouse assignment for {} in warehouse {}: {}", employeeCode, warehouseId, e.getMessage());
            return false;
        }
    }

    private InventoryMovement createMovementFromTransfer(TransferRequest transferRequest) {
        InventoryMovement movement = new InventoryMovement();
        movement.setMovementNumber(generateMovementNumber());
        movement.setMovementType(MovementType.TRANSFER_OUT);
        movement.setProductId(transferRequest.getProductId());
        movement.setSourceWarehouseId(transferRequest.getSourceWarehouseId());
        movement.setDestinationWarehouseId(transferRequest.getDestinationWarehouseId());
        movement.setQuantity(transferRequest.getQuantity());
        movement.setReferenceType(ReferenceType.TRANSFER_ORDER);
        movement.setReferenceNumber(transferRequest.getRequestNumber());
        movement.setRemarks(transferRequest.getReason());
        movement.setPerformedBy(transferRequest.getApprovedBy());
        movement.setMovementStatus(MovementStatus.COMPLETED);

        movement = movementRepository.save(movement);

        // Publish Kafka event
        MovementEvent event = new MovementEvent("StockTransferredOut", String.valueOf(movement.getId()), movement.getMovementNumber());
        event.setMovementType(MovementType.TRANSFER_OUT.name());
        event.setProductId(movement.getProductId());
        event.setSourceWarehouseId(movement.getSourceWarehouseId());
        event.setDestinationWarehouseId(movement.getDestinationWarehouseId());
        event.setQuantity(movement.getQuantity());
        event.setPerformedBy(movement.getPerformedBy());
        eventPublisher.publishMovementEvent(event);

        return movement;
    }

    private InventoryMovement createDispatchMovement(TransferRequest transferRequest) {
        InventoryMovement movement = new InventoryMovement();
        movement.setMovementNumber(generateMovementNumber());
        movement.setMovementType(MovementType.TRANSFER_OUT);
        movement.setProductId(transferRequest.getProductId());
        movement.setSourceWarehouseId(transferRequest.getSourceWarehouseId());
        movement.setDestinationWarehouseId(transferRequest.getDestinationWarehouseId());
        movement.setQuantity(transferRequest.getDispatchedQuantity());
        movement.setReferenceType(ReferenceType.TRANSFER_ORDER);
        movement.setReferenceNumber(transferRequest.getRequestNumber());
        movement.setRemarks("Dispatched for transfer " + transferRequest.getRequestNumber());
        movement.setPerformedBy(transferRequest.getDispatchedBy());
        movement.setMovementStatus(MovementStatus.COMPLETED);

        movement = movementRepository.save(movement);

        MovementEvent event = new MovementEvent("StockTransferredOut", String.valueOf(movement.getId()), movement.getMovementNumber());
        event.setMovementType(MovementType.TRANSFER_OUT.name());
        event.setProductId(movement.getProductId());
        event.setSourceWarehouseId(movement.getSourceWarehouseId());
        event.setDestinationWarehouseId(movement.getDestinationWarehouseId());
        event.setQuantity(movement.getQuantity());
        event.setPerformedBy(movement.getPerformedBy());
        eventPublisher.publishMovementEvent(event);

        return movement;
    }

    private InventoryMovement createReceiveMovement(TransferRequest transferRequest) {
        InventoryMovement movement = new InventoryMovement();
        movement.setMovementNumber(generateMovementNumber());
        movement.setMovementType(MovementType.TRANSFER_IN);
        movement.setProductId(transferRequest.getProductId());
        movement.setSourceWarehouseId(transferRequest.getSourceWarehouseId());
        movement.setDestinationWarehouseId(transferRequest.getDestinationWarehouseId());
        movement.setQuantity(transferRequest.getReceivedQuantity());
        movement.setReferenceType(ReferenceType.TRANSFER_ORDER);
        movement.setReferenceNumber(transferRequest.getRequestNumber());
        movement.setRemarks("Received for transfer " + transferRequest.getRequestNumber());
        movement.setPerformedBy(transferRequest.getReceivedBy());
        movement.setMovementStatus(MovementStatus.COMPLETED);

        movement = movementRepository.save(movement);

        MovementEvent event = new MovementEvent("StockTransferredIn", String.valueOf(movement.getId()), movement.getMovementNumber());
        event.setMovementType(MovementType.TRANSFER_IN.name());
        event.setProductId(movement.getProductId());
        event.setSourceWarehouseId(movement.getSourceWarehouseId());
        event.setDestinationWarehouseId(movement.getDestinationWarehouseId());
        event.setQuantity(movement.getQuantity());
        event.setPerformedBy(movement.getPerformedBy());
        eventPublisher.publishMovementEvent(event);

        return movement;
    }

    private void validateWarehouse(Long warehouseId) {
        try {
            warehouseFeignClient.getWarehouseById(warehouseId);
        } catch (Exception e) {
            log.warn("Could not validate warehouse {} via Feign, proceeding anyway: {}", warehouseId, e.getMessage());
        }
    }

    private String generateRequestNumber() {
        String dateStr = LocalDate.now().format(formatter);
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("TRQ-%s-%s", dateStr, uniqueId);
    }

    private String generateMovementNumber() {
        String dateStr = LocalDate.now().format(formatter);
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("MOV-%s-%s", dateStr, uniqueId);
    }

    private TransferRequestResponse mapToResponse(TransferRequest entity) {
        TransferRequestResponse response = new TransferRequestResponse();
        response.setId(entity.getId());
        response.setRequestNumber(entity.getRequestNumber());
        response.setProductId(entity.getProductId());
        response.setSourceWarehouseId(entity.getSourceWarehouseId());
        response.setDestinationWarehouseId(entity.getDestinationWarehouseId());
        response.setQuantity(entity.getQuantity());
        response.setStatus(entity.getStatus());
        response.setReason(entity.getReason());
        response.setRemarks(entity.getRemarks());
        response.setRequestedBy(entity.getRequestedBy());
        response.setApprovedBy(entity.getApprovedBy());
        response.setApprovedAt(entity.getApprovedAt());
        response.setRejectionReason(entity.getRejectionReason());
        response.setMovementId(entity.getMovementId());
        response.setDispatchedQuantity(entity.getDispatchedQuantity());
        response.setDispatchedAt(entity.getDispatchedAt());
        response.setDispatchedBy(entity.getDispatchedBy());
        response.setReceivedQuantity(entity.getReceivedQuantity());
        response.setReceivedAt(entity.getReceivedAt());
        response.setReceivedBy(entity.getReceivedBy());
        response.setDispatchMovementId(entity.getDispatchMovementId());
        response.setReceiveMovementId(entity.getReceiveMovementId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
