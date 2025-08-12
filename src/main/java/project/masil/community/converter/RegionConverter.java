package project.masil.community.converter;

import project.masil.community.dto.response.RegionResponse;
import project.masil.community.entity.Region;

public class RegionConverter {

  public static RegionResponse toRegionResponse(Region region) {
    return RegionResponse.builder()
        .sido(region.getSido())
        .sigungu(region.getSigungu())
        .build();
  }
}
