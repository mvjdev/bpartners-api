package app.bpartners.api.repository;

import app.bpartners.api.model.InvoiceRelaunchConf;

public interface InvoiceRelaunchRepository {
  InvoiceRelaunchConf save(InvoiceRelaunchConf invoiceRelaunch, String accountId);

  InvoiceRelaunchConf getByAccountId(String accountId);
}
