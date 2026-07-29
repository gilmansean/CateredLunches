package com.example.cateredlunches;

import com.example.cateredlunches.services.CommandLineService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Smoke test verifying the full Spring context wires up correctly:
 * all @Component/@Repository beans resolve, constructor injection succeeds,
 * and the conditional MenuRepository bean (file vs. mysql) is selected without conflict.
 */
@SpringBootTest(properties = "CALENDAR_DATA_PATH=target/test-data/calendar-contextload-test.json")
class CateredLunchesApplicationTests {

    // CommandLineService implements CommandLineRunner, which Spring Boot invokes
    // automatically once the context loads - including during @SpringBootTest.
    // Its real run() loop blocks on console input (Scanner.nextLine()), which would
    // hang this test indefinitely. Mocking it out keeps the test focused on verifying
    // that the full application context wires up successfully, without executing
    // the CLI loop.
    @MockBean
    private CommandLineService commandLineService;

    @Test
    void contextLoads() {
    }

}
