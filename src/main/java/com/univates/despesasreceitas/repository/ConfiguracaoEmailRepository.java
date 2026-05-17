package com.univates.despesasreceitas.repository;

import com.univates.despesasreceitas.entity.ConfiguracaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracaoEmailRepository extends JpaRepository<ConfiguracaoEmail, Long> {
}
