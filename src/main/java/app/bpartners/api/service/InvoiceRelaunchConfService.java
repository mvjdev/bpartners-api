package app.bpartners.api.service;

import app.bpartners.api.model.Invoice;
import app.bpartners.api.model.InvoiceRelaunchConf;
import app.bpartners.api.repository.InvoiceRelaunchConfRepository;
import app.bpartners.api.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class InvoiceRelaunchConfService {
  private final InvoiceRelaunchConfRepository repository;
  private final InvoiceRepository invoiceRepository;

  public InvoiceRelaunchConf findByIdInvoice(String idInvoice) {
    return repository.findByInvoiceId(idInvoice);
  }

  @Transactional
  public InvoiceRelaunchConf save(InvoiceRelaunchConf invoiceRelaunchConf) {
    Invoice toConfigure = invoiceRepository.getById(invoiceRelaunchConf.getIdInvoice());
    toConfigure.setToBeRelaunched(true);
    invoiceRepository.save(toConfigure);
    return repository.save(invoiceRelaunchConf);
  }
}
