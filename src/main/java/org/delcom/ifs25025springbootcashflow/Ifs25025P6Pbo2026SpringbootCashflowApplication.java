package org.delcom.ifs25025springbootcashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Ifs25025P6Pbo2026SpringbootCashflowApplication {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25025P6Pbo2026SpringbootCashflowApplication.class, args);
    }
}