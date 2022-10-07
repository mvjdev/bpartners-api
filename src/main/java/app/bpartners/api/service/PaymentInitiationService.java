package app.bpartners.api.service;

import app.bpartners.api.model.Fraction;
import app.bpartners.api.model.Invoice;
import app.bpartners.api.model.PaymentInitiation;
import app.bpartners.api.model.PaymentRedirection;
import app.bpartners.api.model.exception.NotImplementedException;
import app.bpartners.api.repository.PaymentInitiationRepository;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apfloat.Aprational;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentInitiationService {
  private final PaymentInitiationRepository repository;

  public List<PaymentRedirection> createPaymentReq(List<PaymentInitiation> paymentReqs) {
    if (paymentReqs.size() > 1) {
      throw new NotImplementedException("Only one payment request is supported.");
    }
    PaymentInitiation paymentReq = paymentReqs.get(0);
    return repository.save(paymentReq);
  }

  public PaymentRedirection initiateInvoicePayment(Invoice invoice) {
    if (Objects.equals(invoice.getTotalPriceWithVat(), new Fraction())) {
      return new PaymentRedirection();
    }
    PaymentInitiation paymentInitiation = PaymentInitiation.builder()
        .reference(invoice.getRef())
        .label(invoice.getTitle())
        .amount(invoice.getTotalPriceWithVat()
            .operate(new Fraction(BigInteger.valueOf(100)), Aprational::divide))
        .payerName(invoice.getInvoiceCustomer().getName())
        .payerEmail(invoice.getInvoiceCustomer().getEmail())
        .successUrl("https://dashboard-dev.bpartners.app") //TODO: to change
        .failureUrl("https://dashboard-dev.bpartners.app") //TODO: to change
        .build();
    return repository.save(paymentInitiation).get(0);
  }
}
