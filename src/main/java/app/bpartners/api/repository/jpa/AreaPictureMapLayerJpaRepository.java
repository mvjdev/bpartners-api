package app.bpartners.api.repository.jpa;

import app.bpartners.api.repository.jpa.model.HAreaPictureMapLayer;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaPictureMapLayerJpaRepository
    extends JpaRepository<HAreaPictureMapLayer, String> {
  List<HAreaPictureMapLayer> findAllByDepartementNameInIgnoreCase(
      Collection<String> departementNames);
}
