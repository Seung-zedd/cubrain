package com.cubrain.springboot_starter_auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestVectorStoreConfig.class)
class SpringbootStarterAuthApplicationTests {

	@Test
	void contextLoads() {
	}

}
