package cz.uhk.mois.financialplanning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
//@EnableEurekaClient
public class FinancialPlanningApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialPlanningApplication.class, args);
    }

}
