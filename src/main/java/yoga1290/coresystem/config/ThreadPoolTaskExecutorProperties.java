package yoga1290.coresystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("yoga1290.coresystem.thread-pool")
public class ThreadPoolTaskExecutorProperties {

    int corePoolSize = 50;
    int maxPoolSize = 50;
    int queueCapacity = 10;
    String threadNamePrefix = "thread-";
}
