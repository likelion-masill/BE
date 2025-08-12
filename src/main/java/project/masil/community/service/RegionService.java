package project.masil.community.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.masil.community.dto.response.RegionIdResponse;
import project.masil.community.dto.response.RegionResponse;
import project.masil.community.dto.response.SidoResponse;
import project.masil.community.dto.response.SigunguResponse;
import project.masil.community.entity.Region;
import project.masil.community.exception.RegionErrorCode;
import project.masil.community.repository.RegionRepository;
import project.masil.global.exception.CustomException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionService {

  private final RegionRepository regionRepository;

  /**
   * 시/도 목록을 조회한다.
   * <p>
   * 메서드는 데이터베이스에서 시/도 목록을 조회하고, 각 시/도를 {@link SidoResponse} 객체로 변환하여 리스트로 반환한다.
   * </p>
   *
   * @return 시/도 목록을 담은 {@link SidoResponse} 리스트
   */
  public List<SidoResponse> getSidoList() {
    log.info("[서비스] 시/도 조회 시도");
    return regionRepository.findSidoList()
        .stream()
        .map(SidoResponse::new)
        .toList();
  }

  /**
   * 주어진 시/도(sido)에 해당하는 시/군/구 목록을 조회한다.
   * <p>
   * 메서드는 먼저 해당 시/도가 데이터베이스에 존재하는지 확인하며, 존재하지 않는 경우 {@link CustomException}을 발생시킨다. 존재하는 경우에는 해당
   * 시/도에 속하는 시/군/구 명칭 목록을 조회하여 {@link SigunguResponse} 리스트로 변환하여 반환한다.
   * </p>
   *
   * @param sido 조회할 시/도의 명칭 (예: "서울특별시", "경기도")
   * @return 해당 시/도의 시/군/구 목록을 담은 {@link SigunguResponse} 리스트
   * @throws CustomException 시/도가 존재하지 않을 경우 {@link RegionErrorCode#SIDO_NOT_FOUND} 에러 코드와 함께 예외 발생
   * @see RegionRepository#existsBySido(String)
   * @see RegionRepository#findSigunguList(String)
   */
  public List<SigunguResponse> getSigunguList(String sido) {
    log.info("[서비스] 시/군/구 조회 시도: sido = {}", sido);
    if (!regionRepository.existsBySido(sido)) {
      log.warn("[서비스] 존재하지 않는 시/도: sido = {}", sido);
      throw new CustomException(RegionErrorCode.SIDO_NOT_FOUND);
    }
    return regionRepository.findSigunguList(sido)
        .stream()
        .map(SigunguResponse::new)
        .toList();
  }

  /**
   * 주어진 시/도(sido)와 시/군/구(sigungu)에 해당하는 행정구역의 지역 ID를 조회한다.
   * <p>
   * 메서드는 먼저 해당 시/도와 시/군/구가 데이터베이스에 존재하는지 확인하며, 존재하지 않는 경우 {@link CustomException}을 발생시킨다. 존재하는 경우에는
   * 해당 행정구역의 ID를 {@link RegionIdResponse} 객체로 반환한다.
   * </p>
   *
   * @param sido    조회할 시/도의 명칭 (예: "서울특별시", "경기도")
   * @param sigungu 조회할 시/군/구의 명칭 (예: "강남구", "수원시")
   * @return 해당 행정구역의 지역 ID를 담은 {@link RegionIdResponse} 객체
   * @throws CustomException 시/도 또는 시/군/구가 존재하지 않을 경우 {@link RegionErrorCode#REGION_NOT_FOUND} 에러
   *                         코드와 함께 예외 발생
   */
  public RegionIdResponse getRegionId(String sido, String sigungu) {
    log.info("[서비스] 지역 ID 조회 시도: sido = {}, sigungu = {}", sido, sigungu);
    Region region = regionRepository.findBySidoAndSigungu(sido, sigungu)
        .orElseThrow(() -> new CustomException(RegionErrorCode.REGION_NOT_FOUND)
        );

    return new RegionIdResponse(
        region.getId()
    );
  }

  public RegionResponse getRegionById(Long id) {
    log.info("[서비스] 지역 조회 시도: id = {}", id);
    return regionRepository.findById(id)
        .map(region -> new RegionResponse(region.getSido(), region.getSigungu()))
        .orElseThrow(() -> new CustomException(RegionErrorCode.REGION_NOT_FOUND));
  }

}
