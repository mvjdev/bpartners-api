package app.bpartners.api.service;

import app.bpartners.api.endpoint.rest.model.OrderDirection;
import app.bpartners.api.model.Product;
import app.bpartners.api.repository.ProductRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
  private final ProductRepository repository;

  public List<Product> getProductsByAccount(
      String accountId, int page, int pageSize,
      OrderDirection descriptionOrder,
      OrderDirection unitPriceOrder,
      OrderDirection createdAtOrder) {
    return repository.findAllByIdAccount(accountId, page, pageSize,
        descriptionOrder, unitPriceOrder, createdAtOrder);
  }

  public List<Product> crupdate(String accountId, List<Product> toCreate) {
    return repository.saveAll(accountId, toCreate);
  }
}
