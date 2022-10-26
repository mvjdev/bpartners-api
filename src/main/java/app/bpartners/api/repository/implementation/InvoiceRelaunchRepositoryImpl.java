package app.bpartners.api.repository.implementation;

import app.bpartners.api.model.InvoiceRelaunchConf;
import app.bpartners.api.model.exception.NotFoundException;
import app.bpartners.api.model.mapper.InvoiceRelaunchMapper;
import app.bpartners.api.repository.InvoiceRelaunchRepository;
import app.bpartners.api.repository.jpa.InvoiceRelaunchJpaRepository;
import app.bpartners.api.repository.jpa.model.HInvoiceRelaunchConf;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class InvoiceRelaunchRepositoryImpl implements InvoiceRelaunchRepository {
  private final InvoiceRelaunchJpaRepository jpaRepository;
  private final InvoiceRelaunchMapper mapper;

  @Override
  public InvoiceRelaunchConf save(InvoiceRelaunchConf invoiceRelaunch, String accountId) {
    Optional<HInvoiceRelaunchConf> optionalHInvoiceRelaunch =
        jpaRepository.getByAccountId(accountId);
    if (optionalHInvoiceRelaunch.isPresent()) {
      HInvoiceRelaunchConf persisted = optionalHInvoiceRelaunch.get();
      invoiceRelaunch.setId(persisted.getId());
    }
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(invoiceRelaunch)));
  }

  @Override
  public InvoiceRelaunchConf getByAccountId(String accountId) {
    return mapper.toDomain(
        jpaRepository.getByAccountId(accountId).orElseThrow(
            () -> new NotFoundException(
                "There is no existing invoice relaunch config for account." + accountId)
        )
    );
  }
}
