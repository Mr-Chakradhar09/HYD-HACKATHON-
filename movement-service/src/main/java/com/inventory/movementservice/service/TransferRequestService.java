package com.inventory.movementservice.service;

import com.inventory.movementservice.dto.request.ApproveTransferRequest;
import com.inventory.movementservice.dto.request.CreateTransferRequestRequest;
import com.inventory.movementservice.dto.request.DispatchTransferRequest;
import com.inventory.movementservice.dto.request.ReceiveTransferRequest;
import com.inventory.movementservice.dto.request.RejectTransferRequest;
import com.inventory.movementservice.dto.response.TransferRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferRequestService {

    TransferRequestResponse createTransferRequest(CreateTransferRequestRequest request, String requestedBy);

    TransferRequestResponse approveTransferRequest(Long requestId, ApproveTransferRequest request, String approvedBy);

    TransferRequestResponse rejectTransferRequest(Long requestId, RejectTransferRequest request, String rejectedBy);

    TransferRequestResponse cancelTransferRequest(Long requestId, String cancelledBy);

    TransferRequestResponse dispatchTransferRequest(Long requestId, DispatchTransferRequest request, String dispatchedBy);

    TransferRequestResponse receiveTransferRequest(Long requestId, ReceiveTransferRequest request, String receivedBy);

    TransferRequestResponse getTransferRequest(Long requestId);

    Page<TransferRequestResponse> getAllTransferRequests(Pageable pageable);

    Page<TransferRequestResponse> getPendingTransferRequests(Pageable pageable);

    Page<TransferRequestResponse> getMyTransferRequests(String requestedBy, Pageable pageable);

    Page<TransferRequestResponse> getTransfersForDispatch(Long warehouseId, Pageable pageable);

    Page<TransferRequestResponse> getTransfersForReceipt(Long warehouseId, Pageable pageable);

    long getPendingCount();

    long getDispatchCount(Long warehouseId);

    long getReceiptCount(Long warehouseId);
}
