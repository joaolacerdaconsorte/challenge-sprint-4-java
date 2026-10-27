package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.AuditoriaPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaPetRepository extends JpaRepository<AuditoriaPet, UUID> {

    List<AuditoriaPet> findAllByOrderByDataEventoDesc();

    List<AuditoriaPet> findByPetIdOrderByDataEventoDesc(UUID petId);
}
