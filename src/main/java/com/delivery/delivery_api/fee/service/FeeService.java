package com.delivery.delivery_api.fee.service;

import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.agency.service.AgencyService;
import com.delivery.delivery_api.fee.dto.request.CreateFeeRequest;
import com.delivery.delivery_api.fee.dto.request.UpdateFeeRequest;
import com.delivery.delivery_api.fee.dto.response.FeeResponse;
import com.delivery.delivery_api.fee.entity.Fee;
import com.delivery.delivery_api.fee.repository.FeeRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeService implements IFeeService {

    private final FeeRepository feeRepository;
    private final AgencyService agencyService;
    private final AuditClient auditClient;

    // ===== CRÉATION =====

    @Override
    @Transactional
    public FeeResponse create(CreateFeeRequest request) {

        // Valider que l'agence existe et est active
        agencyService.validateAgencyActive(request.getAgencyId());

        // Une agence ne peut avoir qu'une seule configuration de frais
        if (feeRepository.existsByAgencyId(request.getAgencyId())) {
            throw new ConflictException(
                    "Cette agence a déjà une configuration de frais. " +
                            "Utilisez la mise à jour pour la modifier.");
        }

        // Valider la cohérence : minFee ne doit pas dépasser flatFee
        if (request.getMinFee() > request.getFlatFee()) {
            throw new BusinessException(
                    "La commission minimale ne peut pas dépasser le montant fixe",
                    "INVALID_FEE_CONFIG",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Valider que maxFee > minFee si maxFee est défini
        if (request.getMaxFee() != null && request.getMaxFee() < request.getMinFee()) {
            throw new BusinessException(
                    "Le plafond de commission doit être supérieur à la commission minimale",
                    "INVALID_FEE_CONFIG",
                    HttpStatus.BAD_REQUEST
            );
        }

        Agency agency = agencyService.getByIdOrThrow(request.getAgencyId());

        Fee fee = Fee.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .agency(agency)
                .thresholdAmount(request.getThresholdAmount())
                .flatFee(request.getFlatFee())
                .commissionRate(request.getCommissionRate())
                .minFee(request.getMinFee())
                .maxFee(request.getMaxFee())
                .active(true)
                .build();

        fee = feeRepository.save(fee);

        auditClient.log(
                "FEE_CREATED",
                getConnectedUserEmail(),
                String.format("Configuration de frais créée pour l'agence %s — " +
                                "Seuil: %.0f FCFA, Fixe: %.0f FCFA, Taux: %.1f%%",
                        agency.getName(),
                        request.getThresholdAmount(),
                        request.getFlatFee(),
                        request.getCommissionRate()),
                null
        );

        log.info("Fee créée pour agence {} : seuil={}",
                agency.getName(), request.getThresholdAmount());

        return toResponse(fee);
    }

    // ===== LECTURE =====

    @Override
    @Transactional(readOnly = true)
    public FeeResponse findByUuid(String uuid) {
        Fee fee = getByUuidOrThrow(uuid);
        return toResponse(fee);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeResponse findByAgencyUuid(String agencyUuid) {
        Fee fee = feeRepository.findByAgencyUuid(agencyUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configuration de frais", "agencyUuid", agencyUuid));
        return toResponse(fee);
    }

    // ===== MISE À JOUR =====

    @Override
    @Transactional
    public FeeResponse update(String uuid, UpdateFeeRequest request) {
        Fee fee = getByUuidOrThrow(uuid);

        // Récupérer les valeurs finales pour validation croisée
        Double finalFlatFee = request.getFlatFee() != null
                ? request.getFlatFee() : fee.getFlatFee();
        Double finalMinFee = request.getMinFee() != null
                ? request.getMinFee() : fee.getMinFee();
        Double finalMaxFee = request.getMaxFee() != null
                ? request.getMaxFee() : fee.getMaxFee();

        // Valider cohérence
        if (finalMinFee > finalFlatFee) {
            throw new BusinessException(
                    "La commission minimale ne peut pas dépasser le montant fixe",
                    "INVALID_FEE_CONFIG",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (finalMaxFee != null && finalMaxFee < finalMinFee) {
            throw new BusinessException(
                    "Le plafond de commission doit être supérieur à la commission minimale",
                    "INVALID_FEE_CONFIG",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getThresholdAmount() != null)
            fee.setThresholdAmount(request.getThresholdAmount());
        if (request.getFlatFee() != null)
            fee.setFlatFee(request.getFlatFee());
        if (request.getCommissionRate() != null)
            fee.setCommissionRate(request.getCommissionRate());
        if (request.getMinFee() != null)
            fee.setMinFee(request.getMinFee());
        if (request.getMaxFee() != null)
            fee.setMaxFee(request.getMaxFee());

        fee = feeRepository.save(fee);

        auditClient.log(
                "FEE_UPDATED",
                getConnectedUserEmail(),
                "Configuration de frais mise à jour pour agence : "
                        + fee.getAgency().getName(),
                null
        );

        log.info("Fee mise à jour : {}", uuid);

        return toResponse(fee);
    }

    // ===== ACTIVATION / DÉSACTIVATION =====

    @Override
    @Transactional
    public void toggleStatus(String uuid, boolean active) {
        if (!feeRepository.existsByUuid(uuid)) {
            throw new ResourceNotFoundException("Configuration de frais", "uuid", uuid);
        }
        feeRepository.updateActiveStatus(uuid, active);

        auditClient.log(
                active ? "FEE_ACTIVATED" : "FEE_DEACTIVATED",
                getConnectedUserEmail(),
                "Configuration de frais " + (active ? "activée" : "désactivée")
                        + " : " + uuid,
                null
        );

        log.info("Fee {} → active={}", uuid, active);
    }

    // ===== CALCUL DE COMMISSION =====

    @Override
    @Transactional(readOnly = true)
    public Double calculateCommission(Long agencyId, Double orderAmount) {
        Fee fee = feeRepository.findByAgencyId(agencyId)
                .orElseThrow(() -> new BusinessException(
                        "Aucune configuration de frais trouvée pour cette agence",
                        "FEE_NOT_FOUND",
                        HttpStatus.BAD_REQUEST
                ));

        if (!fee.isActive()) {
            throw new BusinessException(
                    "La configuration de frais de cette agence est désactivée",
                    "FEE_INACTIVE",
                    HttpStatus.BAD_REQUEST
            );
        }

        double commission;

        // Règle hybride : fixe si petite course, pourcentage sinon
        if (orderAmount < fee.getThresholdAmount()) {
            commission = fee.getFlatFee();
        } else {
            commission = orderAmount * (fee.getCommissionRate() / 100.0);
        }

        // Appliquer le minimum garanti
        commission = Math.max(commission, fee.getMinFee());

        // Appliquer le plafond si défini
        if (fee.getMaxFee() != null) {
            commission = Math.min(commission, fee.getMaxFee());
        }

        log.debug("Commission calculée pour agence {} : {} FCFA (course: {} FCFA)",
                agencyId, commission, orderAmount);

        return commission;
    }

    // ===== PRIVÉ =====

    private Fee getByUuidOrThrow(String uuid) {
        return feeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configuration de frais", "uuid", uuid));
    }

    private String getConnectedUserEmail() {
        try {
            return SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private FeeResponse toResponse(Fee fee) {
        return FeeResponse.builder()
                .uuid(fee.getUuid())
                .agencyUuid(fee.getAgency().getUuid())
                .agencyName(fee.getAgency().getName())
                .thresholdAmount(fee.getThresholdAmount())
                .flatFee(fee.getFlatFee())
                .commissionRate(fee.getCommissionRate())
                .minFee(fee.getMinFee())
                .maxFee(fee.getMaxFee())
                .active(fee.isActive())
                .createdAt(fee.getCreatedAt())
                .updatedAt(fee.getUpdatedAt())
                .build();
    }
}