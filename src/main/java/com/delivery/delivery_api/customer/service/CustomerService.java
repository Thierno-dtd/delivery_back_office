package com.delivery.delivery_api.customer.service;

import com.delivery.delivery_api.customer.dto.request.CompleteProfileRequest;
import com.delivery.delivery_api.customer.dto.request.UpdateCustomerRequest;
import com.delivery.delivery_api.customer.dto.response.CustomerResponse;
import com.delivery.delivery_api.customer.entity.Customer;
import com.delivery.delivery_api.customer.repository.CustomerRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final UserService userService;
    private final AuditClient auditClient;

    // ===== COMPLÉTER LE PROFIL =====

    @Override
    @Transactional
    public CustomerResponse completeProfile(CompleteProfileRequest request) {
        User user = userService.getAuthenticatedUser();

        // Vérifier que le profil n'existe pas déjà
        if (customerRepository.existsByUserId(user.getId())) {
            throw new ConflictException(
                    "Le profil de ce client est déjà complété");
        }

        // Vérifier unicité du téléphone
        if (customerRepository.existsByTelephone(request.getTelephone())) {
            throw new ConflictException("Client", "téléphone", request.getTelephone());
        }

        Customer customer = Customer.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .telephone(request.getTelephone())
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .address(request.getAddress())
                .phoneVerified(false)
                .build();

        customer = customerRepository.save(customer);

        // S'assurer que le User a bien le role CUSTOMER
        if (!"CUSTOMER".equals(user.getRole())) {
            user.setRole("CUSTOMER");
            // Le UserRepository.save n'est pas exposé ici volontairement —
            // userService gère la persistence via ses propres méthodes si besoin
        }

        auditClient.log(
                "CUSTOMER_PROFILE_COMPLETED",
                user.getEmail(),
                "Profil client complété : " + request.getFirstName() + " " + request.getLastName(),
                null
        );

        log.info("Profil client complété : {}", user.getEmail());

        return toResponse(customer, user);
    }

    // ===== LECTURE =====

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCurrentProfile() {
        Customer customer = getAuthenticatedCustomer();
        return toResponse(customer, customer.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByUuid(String uuid) {
        Customer customer = getByUuidOrThrow(uuid);
        return toResponse(customer, customer.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(String query, int page, int size) {
        Page<Customer> customers = customerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        query, query, PaginationUtil.build(page, size));
        List<CustomerResponse> content = customers.getContent().stream()
                .map(c -> toResponse(c, c.getUser()))
                .toList();
        return PageResponse.from(customers, content);
    }

    // ===== MISE À JOUR =====

    @Override
    @Transactional
    public CustomerResponse updateProfile(UpdateCustomerRequest request) {
        Customer customer = getAuthenticatedCustomer();

        if (request.getTelephone() != null
                && !request.getTelephone().equals(customer.getTelephone())) {

            if (customerRepository.existsByTelephone(request.getTelephone())) {
                throw new ConflictException("Client", "téléphone", request.getTelephone());
            }
            customer.setTelephone(request.getTelephone());
            customer.setPhoneVerified(false); // nouveau numéro → re-vérification requise
        }

        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getBirthday() != null) customer.setBirthday(request.getBirthday());
        if (request.getGender() != null) customer.setGender(request.getGender());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());

        customer = customerRepository.save(customer);

        log.info("Profil client mis à jour : {}", customer.getUser().getEmail());

        return toResponse(customer, customer.getUser());
    }

    // ===== VÉRIFICATION TÉLÉPHONE =====

    @Override
    @Transactional
    public void verifyPhone(String telephone) {
        Customer customer = customerRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client", "téléphone", telephone));

        customerRepository.verifyPhone(telephone);

        auditClient.log(
                "PHONE_VERIFIED",
                customer.getUser().getEmail(),
                "Téléphone vérifié : " + telephone,
                null
        );

        log.info("Téléphone vérifié : {}", telephone);
    }

    // ===== UTILITAIRES =====

    @Override
    @Transactional(readOnly = true)
    public Customer getAuthenticatedCustomer() {
        User user = userService.getAuthenticatedUser();
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        "Profil client non complété. Veuillez compléter votre profil.",
                        "PROFILE_INCOMPLETE",
                        HttpStatus.BAD_REQUEST
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getByUuidOrThrow(String uuid) {
        return customerRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "uuid", uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return customerRepository.count();
    }

    // ===== PRIVÉ =====

    private CustomerResponse toResponse(Customer customer, User user) {
        return CustomerResponse.builder()
                .uuid(customer.getUuid())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(user.getEmail())
                .telephone(customer.getTelephone())
                .phoneVerified(customer.isPhoneVerified())
                .birthday(customer.getBirthday())
                .gender(customer.getGender())
                .address(customer.getAddress())
                .profileComplete(customer.isProfileComplete())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}