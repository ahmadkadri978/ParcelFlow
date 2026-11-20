package com.parcelflow;

import com.parcelflow.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "DB_PASSWORD=unused")
@Import(PostgresTestConfiguration.class)
class ParcelflowApplicationTests {

	@Test
	void contextLoads() {
	}

}
