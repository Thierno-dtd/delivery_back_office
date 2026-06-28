package com.delivery.delivery_api.zone.service;

import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.agency.service.AgencyService;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import com.delivery.delivery_api.zone.dto.request.CreateZoneRequest;
import com.delivery.delivery_api.zone.dto.request.UpdateZoneRequest;
import com.delivery.delivery_api.zone.dto.response.ZoneResponse;
import com.delivery.delivery_api.zone.entity.Zone;
import com.delivery.delivery_api.zone.repository.ZoneRepository;
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
public class ZoneService implements IZoneService {

    private final ZoneRepository zoneRepository;
    private final AgencyService agencyService;
    private final AuditClient auditClient;

    @Transactional
    public ZoneResponse create(CreateZoneRequest request) {

        agencyService.validateAgencyActive(request.getAgencyId());

        if (zoneRepository.existsByNameAndAgencyId(request.getName(), request.getAgencyId())) {
            throw new ConflictException(
                    "Une zone avec ce nom existe déjà pour cette agence");
        }

        Agency agency = agencyService.getByIdOrThrow(request.getAgencyId());

        Zone zone = Zone.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .agency(agency)
                .name(request.getName())
                .city(request.getCity())
                .baseFee(request.getBaseFee())
                .active(true)
                .build();

        zone = zoneRepository.save(zone);

        auditClient.log(
                "ZONE_CREATED",
                null,
                String.format("Nouvelle zone créée : %s (%s) — agence : %s",
                        zone.getName(), zone.getCity(), agency.getName()),
                null
        );

        log.info("Zone créée : {} pour agence {}", zone.getName(), agency.getName());

        return toResponse(zone);
    }

    @Transactional(readOnly = true)
    public ZoneResponse findByUuid(String uuid) {
        Zone zone = getByUuidOrThrow(uuid);
        return toResponse(zone);
    }

    @Transactional(readOnly = true)
    public PageResponse<ZoneResponse> findByAgency(Long agencyId, int page, int size) {
        agencyService.validateAgencyExists(agencyId);
        Page<Zone> zones = zoneRepository.findByAgencyId(
                agencyId, PaginationUtil.build(page, size));
        List<ZoneResponse> content = zones.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(zones, content);
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> findActiveByAgency(Long agencyId) {
        agencyService.validateAgencyExists(agencyId);
        return zoneRepository.findByAgencyIdAndActive(agencyId, true).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ZoneResponse> findAll(int page, int size) {
        Page<Zone> zones = zoneRepository.findAll(PaginationUtil.build(page, size));
        List<ZoneResponse> content = zones.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(zones, content);
    }

    @Transactional
    public ZoneResponse update(String uuid, UpdateZoneRequest request) {
        Zone zone = getByUuidOrThrow(uuid);

        if (request.getName() != null
                && !request.getName().equals(zone.getName())
                && zoneRepository.existsByNameAndAgencyId(
                request.getName(), zone.getAgency().getId())) {
            throw new ConflictException(
                    "Une zone avec ce nom existe déjà pour cette agence");
        }

        if (request.getName() != null) zone.setName(request.getName());
        if (request.getCity() != null) zone.setCity(request.getCity());
        if (request.getBaseFee() != null) zone.setBaseFee(request.getBaseFee());

        zone = zoneRepository.save(zone);

        log.info("Zone mise à jour : {}", uuid);

        return toResponse(zone);
    }

    @Transactional
    public void toggleStatus(String uuid, boolean active) {
        if (!zoneRepository.existsByUuid(uuid)) {
            throw new ResourceNotFoundException("Zone", "uuid", uuid);
        }
        zoneRepository.updateActiveStatus(uuid, active);
        log.info("Zone {} → active={}", uuid, active);
    }

    @Transactional(readOnly = true)
    public Zone getByUuidOrThrow(String uuid) {
        return zoneRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "uuid", uuid));
    }

    @Transactional(readOnly = true)
    public Zone getByIdOrThrow(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));
    }

    @Transactional(readOnly = true)
    public void validateZoneActive(Long zoneId) {
        Zone zone = getByIdOrThrow(zoneId);
        if (!zone.isActive()) {
            throw new BusinessException(
                    "Cette zone est désactivée",
                    "ZONE_INACTIVE",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
                .uuid(zone.getUuid())
                .agencyUuid(zone.getAgency().getUuid())
                .agencyName(zone.getAgency().getName())
                .name(zone.getName())
                .city(zone.getCity())
                .baseFee(zone.getBaseFee())
                .active(zone.isActive())
                .createdAt(zone.getCreatedAt())
                .build();
    }
}