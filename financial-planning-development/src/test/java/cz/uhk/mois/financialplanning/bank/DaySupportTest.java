package cz.uhk.mois.financialplanning.bank;

import cz.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class DaySupportTest extends AbsTestConfiguration {

    @Autowired
    private DaySupport daySupport;

    private static ZonedDateTime createZonedDateTime(int year, int month) {
        return ZonedDateTime.of(year, month, 15, 0, 0, 0, 0, ZoneId.systemDefault());
    }

    @Test
    void getCountOfDaysInLastMonth_Hds_February() {
        log.info("Test of getting days in last month. February in 2020 will be used for testing.");

        // Data preparation
        ZonedDateTime zonedDateTime = createZonedDateTime(2020, 3);

        // Execution
        int countOfDaysInLastMonth = daySupport.getCountOfDaysInLastMonth(zonedDateTime);

        // Verification
        assertEquals(29, countOfDaysInLastMonth);
    }

    @Test
    void getCountOfDaysInLastMonth_Hds_December() {
        log.info("Test of getting days in last month. December in 2019 will be used for testing.");

        // Data preparation
        ZonedDateTime zonedDateTime = createZonedDateTime(2020, 1);

        // Execution
        int countOfDaysInLastMonth = daySupport.getCountOfDaysInLastMonth(zonedDateTime);

        // Verification
        assertEquals(31, countOfDaysInLastMonth);
    }
}
