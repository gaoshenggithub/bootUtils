package com.pay;

import com.pay.model.AliConfig;
import com.pay.model.People;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PayApplicationTests {
	@Autowired
	 People aliConfig;


	@Test
	public void contextLoads() {
		System.out.println(aliConfig);
	}

}
