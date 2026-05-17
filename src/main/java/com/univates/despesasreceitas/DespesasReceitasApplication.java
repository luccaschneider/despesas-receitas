package com.univates.despesasreceitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DespesasReceitasApplication {
    public static void main(String[] args) {
        SpringApplication.run(DespesasReceitasApplication.class, args);
    }
}
