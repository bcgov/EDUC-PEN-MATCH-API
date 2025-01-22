package ca.bc.gov.educ.api.penmatch.service.v1.match;

import ca.bc.gov.educ.api.penmatch.model.v1.FrequencySurnameEntity;
import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SurnameFrequencyService {
  /**
   * The constant VERY_FREQUENT.
   */
  public static final Integer VERY_FREQUENT = 500;

  @Getter
  private final SurnameFrequencyRepository surnameFrequencyRepository;

  public SurnameFrequencyService(SurnameFrequencyRepository surnameFrequencyRepository) {
    this.surnameFrequencyRepository = surnameFrequencyRepository;
  }

  @Scheduled(fixedRate = 300000)
  @CacheEvict(value="surnameFrequency", allEntries=true)
  public void evictAllcachesAtIntervals() {
    log.debug("Evicting surnameFrequency cache");
  }

  @Cacheable("surnameFrequency")
  public Integer lookupSurnameFrequency(String fullStudentSurname) {
    if (fullStudentSurname == null) {
      return 0;
    }
    // Note this returns in two different places
    Integer surnameFrequency = 0;
    List<FrequencySurnameEntity> surnameFreqEntityList = getSurnameFrequencyRepository().findAllBySurnameStartingWith(fullStudentSurname);

    for (FrequencySurnameEntity surnameFreqEntity : surnameFreqEntityList) {
      surnameFrequency = surnameFrequency + Integer.valueOf(surnameFreqEntity.getSurnameFrequency());

      if (surnameFrequency >= VERY_FREQUENT) {
        break;
      }
    }

    return surnameFrequency;
  }
}
