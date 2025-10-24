package org.example.staystylish.common.initializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.repository.RegionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        // DB에 데이터가 있는지 확인
        if (regionRepository.count() > 0) {
            log.info("Region 데이터가 이미 존재하므로 초기화 하지 않습니다.");
            return;
        }

        log.info("Region 데이터 초기화를 시작합니다.");

        ClassPathResource resource = new ClassPathResource("region_grid.csv");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            List<Region> regions = new ArrayList<>();
            br.readLine(); // 첫 줄(헤더) 건너뛰기
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // CSV 파싱

                // CSV 데이터 순서 region_id, province, city, district, longitude, latitude
                try {
                    // city나 district가 비어있을 수 있으므로 trim() 후 비어있는지 확인
                    String city = (data.length > 2 && !data[2].trim().isEmpty()) ? data[2].trim() : null;
                    String district = (data.length > 3 && !data[3].trim().isEmpty()) ? data[3].trim() : null;

                    Region region = Region.builder()
                            .id(Long.parseLong(data[0].trim()))
                            .province(data[1].trim())
                            .city(city)
                            .district(district)
                            .longitude(Double.parseDouble(data[4].trim()))
                            .latitude(Double.parseDouble(data[5].trim()))
                            .build();
                    regions.add(region);
                } catch (NumberFormatException e) {
                    log.error("CSV 파싱 오류 발생 (숫자 변환 실패): {}", line, e);
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("CSV 파싱 오류 발생 (데이터 부족): {}", line, e);
                } catch (Exception e) { // 그 외 예외 처리
                    log.error("CSV 처리 중 예외 발생: {}", line, e);
                }
            }

            if (!regions.isEmpty()) {
                regionRepository.saveAll(regions);
            }
        }
        log.info("Region 데이터 초기화 완료.");
    }
}
