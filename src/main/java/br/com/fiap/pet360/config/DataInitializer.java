package br.com.fiap.pet360.config;

import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TelemetriaIotRepository;
import br.com.fiap.pet360.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner logStartupStatus(UsuarioRepository usuarioRepository,
                                       PetRepository petRepository,
                                       TelemetriaIotRepository telemetriaIotRepository) {
        return args -> log.info(
                "PetCare 360 (CLYVO VET Sprint 4) inicializado com sucesso | Usuarios={} | Pets={} | Leituras IoT/IA={}",
                usuarioRepository.count(),
                petRepository.count(),
                telemetriaIotRepository.count()
        );
    }
}
