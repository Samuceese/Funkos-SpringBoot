package org.example.storage.config;

import lombok.extern.slf4j.Slf4j;
import org.example.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StorageConfig {
    @Bean
    public CommandLineRunner init(StorageService storageService, @Value("true") String deleteAll){
        return args -> {
            if(deleteAll.equals("true")){
                log.info("Borrando almacenamiento");
                storageService.deleteAll();
            }
            storageService.init();
        };
    }
}
