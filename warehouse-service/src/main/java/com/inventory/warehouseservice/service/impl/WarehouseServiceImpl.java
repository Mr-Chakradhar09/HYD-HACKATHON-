package com.inventory.warehouseservice.service.impl;

import com.inventory.warehouseservice.config.WarehouseSpecification;
import com.inventory.warehouseservice.dto.request.CreateWarehouseRequest;
import com.inventory.warehouseservice.dto.request.UpdateWarehouseRequest;
import com.inventory.warehouseservice.dto.response.AddressResponse;
import com.inventory.warehouseservice.dto.response.WarehouseResponse;
import com.inventory.warehouseservice.dto.response.WarehouseSummaryResponse;
import com.inventory.warehouseservice.entity.Address;
import com.inventory.warehouseservice.entity.Warehouse;
import com.inventory.warehouseservice.entity.WarehouseAssignment;
import com.inventory.warehouseservice.enums.CapacityUnit;
import com.inventory.warehouseservice.enums.WarehouseRole;
import com.inventory.warehouseservice.enums.WarehouseStatus;
import com.inventory.warehouseservice.event.KafkaEventPublisher;
import com.inventory.warehouseservice.event.WarehouseEvent;
import com.inventory.warehouseservice.exception.DuplicateResourceException;
import com.inventory.warehouseservice.exception.InvalidOperationException;
import com.inventory.warehouseservice.exception.ResourceNotFoundException;
import com.inventory.warehouseservice.repository.WarehouseAssignmentRepository;
import com.inventory.warehouseservice.repository.WarehouseRepository;
import com.inventory.warehouseservice.service.interfaces.WarehouseService;
import com.inventory.warehouseservice.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseAssignmentRepository assignmentRepository;
    private final KafkaEventPublisher eventPublisher;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository, WarehouseAssignmentRepository assignmentRepository, KafkaEventPublisher eventPublisher) {
        this.warehouseRepository = warehouseRepository;
        this.assignmentRepository = assignmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByWarehouseCode(request.getWarehouseCode())) {
            throw new DuplicateResourceException("Warehouse code already exists");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(request.getWarehouseCode());
        warehouse.setWarehouseName(request.getWarehouseName());
        warehouse.setEmail(request.getEmail());
        warehouse.setPhone(request.getPhone());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setCapacityUnit(request.getCapacityUnit() != null ? request.getCapacityUnit() : CapacityUnit.SQ_FT);
        
        Address address = new Address(
                request.getAddress().getAddressLine1(),
                request.getAddress().getAddressLine2(),
                request.getAddress().getCity(),
                request.getAddress().getState(),
                request.getAddress().getCountry(),
                request.getAddress().getPostalCode()
        );
        warehouse.setAddress(address);
        warehouse.setStatus(WarehouseStatus.ACTIVE);
        warehouse.setCreatedBy(SecurityUtil.getCurrentEmployeeCode());

        warehouse = warehouseRepository.save(warehouse);

        WarehouseEvent event = new WarehouseEvent("WarehouseCreated", warehouse.getId(), warehouse.getWarehouseCode());
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishWarehouseEvent(event);

        return mapToResponse(warehouse);
    }

    @Override
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        return mapToResponse(warehouse);
    }

    @Override
    public WarehouseResponse getWarehouseByCode(String code) {
        Warehouse warehouse = warehouseRepository.findByWarehouseCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        return mapToResponse(warehouse);
    }

    @Override
    public Page<WarehouseSummaryResponse> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(this::mapToSummaryResponse);
    }

    @Override
    public Page<WarehouseSummaryResponse> searchWarehouses(String keyword, WarehouseStatus status, Pageable pageable) {
        Specification<Warehouse> spec = Specification.allOf(
                WarehouseSpecification.hasKeyword(keyword),
                WarehouseSpecification.hasStatus(status)
        );
        return warehouseRepository.findAll(spec, pageable).map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        WarehouseRole highestRole = SecurityUtil.getHighestRole();
        if (highestRole == WarehouseRole.INVENTORY_MANAGER) {
            String employeeCode = SecurityUtil.getCurrentEmployeeCode();
            WarehouseAssignment myAssignment = assignmentRepository.findByEmployeeCodeAndActiveTrue(employeeCode)
                    .orElseThrow(() -> new InvalidOperationException("You are not assigned to any warehouse"));
            if (!myAssignment.getWarehouse().getId().equals(id)) {
                throw new InvalidOperationException("You can only update your own assigned warehouse");
            }
        }

        warehouse.setWarehouseName(request.getWarehouseName());
        warehouse.setEmail(request.getEmail());
        warehouse.setPhone(request.getPhone());
        warehouse.setCapacity(request.getCapacity());
        if (request.getCapacityUnit() != null) {
            warehouse.setCapacityUnit(request.getCapacityUnit());
        }

        Address address = warehouse.getAddress();
        if (address == null) {
            address = new Address();
        }
        address.setAddressLine1(request.getAddress().getAddressLine1());
        address.setAddressLine2(request.getAddress().getAddressLine2());
        address.setCity(request.getAddress().getCity());
        address.setState(request.getAddress().getState());
        address.setCountry(request.getAddress().getCountry());
        address.setPostalCode(request.getAddress().getPostalCode());
        warehouse.setAddress(address);

        warehouse.setUpdatedBy(SecurityUtil.getCurrentEmployeeCode());

        warehouse = warehouseRepository.save(warehouse);

        WarehouseEvent event = new WarehouseEvent("WarehouseUpdated", warehouse.getId(), warehouse.getWarehouseCode());
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishWarehouseEvent(event);

        return mapToResponse(warehouse);
    }

    @Override
    @Transactional
    public void activateWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (warehouse.getStatus() == WarehouseStatus.ACTIVE) {
            throw new InvalidOperationException("Warehouse is already active");
        }

        warehouse.setStatus(WarehouseStatus.ACTIVE);
        warehouse.setUpdatedBy(SecurityUtil.getCurrentEmployeeCode());
        warehouseRepository.save(warehouse);

        WarehouseEvent event = new WarehouseEvent("WarehouseActivated", warehouse.getId(), warehouse.getWarehouseCode());
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishWarehouseEvent(event);
    }

    @Override
    @Transactional
    public void deactivateWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (warehouse.getStatus() == WarehouseStatus.INACTIVE) {
            throw new InvalidOperationException("Warehouse is already inactive");
        }

        if (!assignmentRepository.findByWarehouseIdAndActiveTrue(id).isEmpty()) {
            throw new InvalidOperationException("Cannot deactivate warehouse with active employee assignments");
        }

        warehouse.setStatus(WarehouseStatus.INACTIVE);
        warehouse.setUpdatedBy(SecurityUtil.getCurrentEmployeeCode());
        warehouseRepository.save(warehouse);

        WarehouseEvent event = new WarehouseEvent("WarehouseDeactivated", warehouse.getId(), warehouse.getWarehouseCode());
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishWarehouseEvent(event);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (warehouse.getStatus() == WarehouseStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot delete an active warehouse. Deactivate it first.");
        }

        if (!assignmentRepository.findByWarehouseIdAndActiveTrue(id).isEmpty()) {
            throw new InvalidOperationException("Cannot delete warehouse with active employee assignments");
        }

        warehouseRepository.deleteById(id);

        WarehouseEvent event = new WarehouseEvent("WarehouseDeleted", warehouse.getId(), warehouse.getWarehouseCode());
        event.setPerformedBy(SecurityUtil.getCurrentEmployeeCode());
        eventPublisher.publishWarehouseEvent(event);
    }

    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setWarehouseCode(warehouse.getWarehouseCode());
        response.setWarehouseName(warehouse.getWarehouseName());
        response.setEmail(warehouse.getEmail());
        response.setPhone(warehouse.getPhone());
        response.setCapacity(warehouse.getCapacity());
        response.setCapacityUnit(warehouse.getCapacityUnit());
        response.setStatus(warehouse.getStatus());
        response.setCreatedBy(warehouse.getCreatedBy());
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedBy(warehouse.getUpdatedBy());
        response.setUpdatedAt(warehouse.getUpdatedAt());
        response.setVersion(warehouse.getVersion());

        if (warehouse.getAddress() != null) {
            response.setAddress(new AddressResponse(
                    warehouse.getAddress().getAddressLine1(),
                    warehouse.getAddress().getPostalCode(),
                    warehouse.getAddress().getState(),
                    warehouse.getAddress().getCountry(),
                    warehouse.getAddress().getCity(),
                    warehouse.getAddress().getAddressLine2()
            ));
        }
        return response;
    }

    private WarehouseSummaryResponse mapToSummaryResponse(Warehouse warehouse) {
        WarehouseSummaryResponse response = new WarehouseSummaryResponse();
        response.setId(warehouse.getId());
        response.setWarehouseCode(warehouse.getWarehouseCode());
        response.setWarehouseName(warehouse.getWarehouseName());
        response.setStatus(warehouse.getStatus());
        response.setCapacity(warehouse.getCapacity());
        response.setCapacityUnit(warehouse.getCapacityUnit());
        if (warehouse.getAddress() != null) {
            response.setCity(warehouse.getAddress().getCity());
            AddressResponse addr = new AddressResponse();
            addr.setAddressLine1(warehouse.getAddress().getAddressLine1());
            addr.setAddressLine2(warehouse.getAddress().getAddressLine2());
            addr.setCity(warehouse.getAddress().getCity());
            addr.setState(warehouse.getAddress().getState());
            addr.setCountry(warehouse.getAddress().getCountry());
            addr.setPostalCode(warehouse.getAddress().getPostalCode());
            response.setAddress(addr);
        }
        return response;
    }
}
