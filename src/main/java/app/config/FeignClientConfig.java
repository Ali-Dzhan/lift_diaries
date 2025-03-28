package app.config;

import app.exception.NotificationServiceFeignCallException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public ErrorDecoder customErrorDecoder() {
        return (methodKey, response) -> new NotificationServiceFeignCallException
                ("Notification service error: " + response.status());
    }
}
