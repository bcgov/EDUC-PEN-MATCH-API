package ca.bc.gov.educ.api.penmatch.service.v1.match;

import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class SurnameFrequencyService {
  private final Executor bgTaskExecutor = new EnhancedQueueExecutor.Builder()
    .setCorePoolSize(1).setMaximumPoolSize(1).setKeepAliveTime(Duration.ofSeconds(60)).build();
  private final SurnameFrequencyRepository surnameFrequencyRepository;

  private final ReadWriteLock surnameFreqMapLock = new ReentrantReadWriteLock();


  @Getter
  private final Map<String, Integer> surnameFreqMap = new ConcurrentHashMap<>();

  @Value("${initialization.background.enabled}")
  private Boolean isBackgroundInitializationEnabled;


  @Autowired
  public SurnameFrequencyService(final SurnameFrequencyRepository surnameFrequencyRepository) {
    this.surnameFrequencyRepository = surnameFrequencyRepository;
  }

  @PostConstruct
  public void init() {
    this.loadSurnameFreqDataIntoMemory();
  }

  private void loadSurnameFreqDataIntoMemory() {
    if (this.isBackgroundInitializationEnabled != null && this.isBackgroundInitializationEnabled) {
      this.bgTaskExecutor.execute(this::populateSurnameFreqMap);
    } else {
      this.populateSurnameFreqMap();
    }
  }

  public void populateSurnameFreqMap() {
    for (val item : this.surnameFrequencyRepository.findAll()) {
      this.surnameFreqMap.put(StringUtils.trim(item.getSurname()), Integer.parseInt(item.getSurnameFrequency()));
    }

  }

  @Scheduled(cron = "${schedule.jobs.load.surname.frequency.cron}") // 0 0 0/6 * * * every 6 hours
  public void scheduled() {
    final Lock writeLock = this.surnameFreqMapLock.writeLock();
    try {
      writeLock.lock();
      this.loadSurnameFreqDataIntoMemory();
    } finally {
      writeLock.unlock();
    }
  }
}
