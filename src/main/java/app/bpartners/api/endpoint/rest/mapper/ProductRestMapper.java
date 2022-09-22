package app.bpartners.api.endpoint.rest.mapper;

import app.bpartners.api.endpoint.rest.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductRestMapper {
  public Product toRest(app.bpartners.api.model.Product domain) {
    return new Product()
        .id(domain.getId())
        .description(domain.getDescription())
        .quantity(domain.getQuantity())
        .unitPrice(domain.getUnitPrice())
        .vatPercent(domain.getVatPercent())
        .totalVat(domain.getTotalVat())
        .totalPriceWithVat(domain.getTotalPriceWithVat());
  }
}
