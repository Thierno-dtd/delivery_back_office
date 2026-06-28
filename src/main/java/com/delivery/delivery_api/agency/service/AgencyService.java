package com.delivery.delivery_api.agency.service;

import com.delivery.delivery_api.agency.dto.request.CreateAgencyRequest;
import com.delivery.delivery_api.agency.dto.request.UpdateAgencyRequest;
import com.delivery.delivery_api.agency.dto.response.AgencyResponse;
import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.agency.repository.AgencyRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AuditClient auditClient;

    @Transactional
    public AgencyResponse create(CreateAgencyRequest request) {

        if (agencyRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Agence", "email", request.getEmail());
        }

        if (agencyRepository.existsByName(request.getName())) {
            throw new ConflictException("Agence", "nom", request.getName());
        }

        Agency agency = Agency.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .name(request.getName())
                .slogan(request.getSlogan())
                .address(request.getAddress())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .logoUrl(request.getLogoUrl())
                .active(true)
                .build();

        agency = agencyRepository.save(agency);

        auditClient.log(
                "AGENCY_CREATED",
                null,
                "Nouvelle agence créée : " + agency.getName(),
                null
        );

        log.info("Agence créée : {} ({})", agency.getName(), agency.getUuid());

        return toResponse(agency, 0L);
    }

    @Transactional(readOnly = true)
    public AgencyResponse findByUuid(String uuid) {
        Agency agency = getByUuidOrThrow(uuid);
        return toResponse(agency, 0L);
    }

    @Transactional(readOnly = true)
    public PageResponse<AgencyResponse> findAll(int page, int size) {
        Page<Agency> agencies = agencyRepository.findAll(PaginationUtil.build(page, size));
        List<AgencyResponse> content = agencies.getContent().stream()
                .map(a -> toResponse(a, 0L))
                .toList();
        return PageResponse.from(agencies, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<AgencyResponse> findActive(int page, int size) {
        Page<Agency> agencies = agencyRepository.findByActive(
                true, PaginationUtil.build(page, size));
        List<AgencyResponse> content = agencies.getContent().stream()
                .map(a -> toResponse(a, 0L))
                .toList();
        return PageResponse.from(agencies, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<AgencyResponse> search(String name, int page, int size) {
        Page<Agency> agencies = agencyRepository.findByNameContainingIgnoreCase(
                name, PaginationUtil.build(page, size));
        List<AgencyResponse> content = agencies.getContent().stream()
                .map(a -> toResponse(a, 0L))
                .toList();
        return PageResponse.from(agencies, content);
    }

    @Transactional
    public AgencyResponse update(String uuid, UpdateAgencyRequest request) {
        Agency agency = getByUuidOrThrow(uuid);

        if (request.getEmail() != null
                && !request.getEmail().equals(agency.getEmail())
                && agencyRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Agence", "email", request.getEmail());
        }

        if (request.getName() != null
                && !request.getName().equals(agency.getName())
                && agencyRepository.existsByName(request.getName())) {
            throw new ConflictException("Agence", "nom", request.getName());
        }

        if (request.getName() != null) agency.setName(request.getName());
        if (request.getSlogan() != null) agency.setSlogan(request.getSlogan());
        if (request.getAddress() != null) agency.setAddress(request.getAddress());
        if (request.getTelephone() != null) agency.setTelephone(request.getTelephone());
        if (request.getEmail() != null) agency.setEmail(request.getEmail());
        if (request.getLogoUrl() != null) agency.setLogoUrl(request.getLogoUrl());

        agency = agencyRepository.save(agency);

        auditClient.log("AGENCY_UPDATED", null,
                "Agence mise à jour : " + agency.getName(), null);

        log.info("Agence mise à jour : {}", uuid);

        return toResponse(agency, 0L);
    }

    // ===== ACTIVATION / DÉSACTIVATION =====

    @Transactional
    public void toggleStatus(String uuid, boolean active) {
        if (!agencyRepository.existsByUuid(uuid)) {
            throw new ResourceNotFoundException("Agence", "uuid", uuid);
        }

        agencyRepository.updateActiveStatus(uuid, active);

        auditClient.log(
                active ? "AGENCY_ACTIVATED" : "AGENCY_DEACTIVATED",
                null,
                "Agence " + (active ? "activée" : "désactivée") + " : " + uuid,
                null
        );

        log.info("Agence {} → active={}", uuid, active);
    }

    @Transactional(readOnly = true)
    public Agency getByUuidOrThrow(String uuid) {
        return agencyRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", "uuid", uuid));
    }

    @Transactional(readOnly = true)
    public Agency getByIdOrThrow(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", "id", id));
    }

    @Transactional(readOnly = true)
    public void validateAgencyExists(Long agencyId) {
        if (!agencyRepository.existsById(agencyId)) {
            throw new ResourceNotFoundException("Agence", "id", agencyId);
        }
    }

    @Transactional(readOnly = true)
    public void validateAgencyActive(Long agencyId) {
        Agency agency = getByIdOrThrow(agencyId);
        if (!agency.isActive()) {
            throw new BusinessException(
                    "Cette agence est désactivée",
                    "AGENCY_INACTIVE",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return agencyRepository.countByActive(true);
    }

    private AgencyResponse toResponse(Agency agency, Long totalDrivers) {
        return AgencyResponse.builder()
                .uuid(agency.getUuid())
                .name(agency.getName())
                .slogan(agency.getSlogan())
                .address(agency.getAddress())
                .telephone(agency.getTelephone())
                .email(agency.getEmail())
                .logoUrl(agency.getLogoUrl())
                .active(agency.isActive())
                .totalDrivers(totalDrivers)
                .createdAt(agency.getCreatedAt())
                .build();
    }
}