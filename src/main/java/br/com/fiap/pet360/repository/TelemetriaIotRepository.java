package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.TelemetriaIot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelemetriaIotRepository extends JpaRepository<TelemetriaIot, UUID> {

    List<TelemetriaIot> findAllByOrderByDataLeituraDesc();

    List<TelemetriaIot> findByPetIdOrderByDataLeituraDesc(UUID petId);

    Optional<TelemetriaIot> findFirstByPetIdOrderByDataLeituraDesc(UUID petId);
}
