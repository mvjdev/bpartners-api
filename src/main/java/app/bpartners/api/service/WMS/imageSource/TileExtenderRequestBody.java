package app.bpartners.api.service.WMS.imageSource;

import static app.bpartners.api.service.WMS.Tile.from;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import app.bpartners.api.model.AreaPicture;
import app.bpartners.api.model.AreaPictureMapLayer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@JsonAutoDetect(fieldVisibility = ANY)
@Data
@Builder
public class TileExtenderRequestBody implements Serializable {
  public static final String OPENSTREETMAP_SERVER_NAME = "openstreetmap";
  public static final String GEOSERVER_SERVER_NAME = "geoserver";
  public static final String GEOSERVER_IGN_NAME = "geoserver_ign";
  private int x;
  private int y;
  private int z;
  private String server;
  private String layer;

  private static String getSource(AreaPictureMapLayer areaPictureMapLayer) {
    return switch (areaPictureMapLayer.getSource()) {
      case OPENSTREETMAP -> OPENSTREETMAP_SERVER_NAME;
      case GEOSERVER -> GEOSERVER_SERVER_NAME;
      case GEOSERVER_IGN -> GEOSERVER_IGN_NAME;
    };
  }

  public static TileExtenderRequestBody fromAreaPicture(AreaPicture areaPicture) {
    double currentGeoPositionLongitude =
        areaPicture.getCurrentGeoPosition().getLongitude() != null
            ? areaPicture.getCurrentGeoPosition().getLongitude()
            : areaPicture.getCurrentTile().getLongitude();
    double currentGeoPositionLatitude =
        areaPicture.getCurrentGeoPosition().getLatitude() != null
            ? areaPicture.getCurrentGeoPosition().getLatitude()
            : areaPicture.getCurrentTile().getLatitude();
    var tile =
        from(currentGeoPositionLongitude, currentGeoPositionLatitude, areaPicture.getArcgisZoom());
    var currentLayer = areaPicture.getCurrentLayer();
    return TileExtenderRequestBody.builder()
        .x(tile.getX())
        .y(tile.getY())
        .z(tile.getArcgisZoom().getZoomLevel())
        .layer(currentLayer.getName())
        .server(getSource(currentLayer))
        .build();
  }
}
