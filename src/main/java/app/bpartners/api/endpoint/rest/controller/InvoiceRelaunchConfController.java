package app.bpartners.api.endpoint.rest.controller;

import app.bpartners.api.endpoint.rest.mapper.InvoiceRelaunchRestMapper;
import app.bpartners.api.endpoint.rest.model.AccountInvoiceRelaunchConf;
import app.bpartners.api.endpoint.rest.model.CreateAccountInvoiceRelaunchConf;
import app.bpartners.api.endpoint.rest.model.InvoiceRelaunchConf;
import app.bpartners.api.service.InvoiceRelaunchConfService;
import app.bpartners.api.service.InvoiceRelaunchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class InvoiceRelaunchConfController {
  private final InvoiceRelaunchService accountInvoiceRelaunchConfservice;
  private final InvoiceRelaunchRestMapper mapper;
  private final InvoiceRelaunchConfService invoiceRelaunchConfService;

  @GetMapping("/accounts/{aId}/invoiceRelaunchConf")
  public AccountInvoiceRelaunchConf getAccountInvoiceRelaunch(
      @PathVariable("aId") String accountId) {
    return mapper.toRest(accountInvoiceRelaunchConfservice.getByAccountId(accountId));
  }

  @PutMapping("/accounts/{aId}/invoiceRelaunchConf")
  public AccountInvoiceRelaunchConf configureAccountInvoiceRelaunch(
      @PathVariable("aId") String accountId,
      @RequestBody CreateAccountInvoiceRelaunchConf toCreate) {
    return mapper.toRest(
        accountInvoiceRelaunchConfservice.saveConf(accountId, mapper.toDomain(toCreate)));
  }

  @GetMapping("/accounts/{aId}/invoices/{iId}/relaunchConf")
  public InvoiceRelaunchConf getInvoiceRelaunchConf(
      @PathVariable("aId") String accountId,
      @PathVariable("iId") String invoiceId
  ) {
    return mapper.toRest(invoiceRelaunchConfService.findByIdInvoice(invoiceId));
  }

  @PutMapping("/accounts/{aId}/invoices/{iId}/relaunchConf")
  public InvoiceRelaunchConf crupdateRelaunchConf(
      @PathVariable("aId") String accountId,
      @PathVariable("iId") String invoiceId,
      @RequestBody InvoiceRelaunchConf invoiceRelaunchConf
  ) {
    return mapper.toRest(
        invoiceRelaunchConfService.save(mapper.toDomain(invoiceRelaunchConf, invoiceId))
    );
  }

}
