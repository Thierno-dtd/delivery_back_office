package com.delivery.delivery_api.payment.dto.request;

import com.delivery.delivery_api.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePaymentRequest {

    @NotBlank(message = "L'uuid de la commande est obligatoire")
    private String orderUuid;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private PaymentMethod method;

    // Référence fournie par le client (ex: numéro de transaction Airtel/Moov)
    // Null pour CASH
    private String transactionReference;
}