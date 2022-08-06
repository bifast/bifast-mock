package bifast.mock;

import org.apache.camel.EndpointInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import bifast.mock.isoapt.pojo.CreditResponse;

@CamelSpringBootTest
@EnableAutoConfiguration
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@MockEndpoints("direct:debit")
public class EDebitTest {
	@Autowired ProducerTemplate producerTemplate;
	@Autowired TestUtilService utilService;

	@EndpointInject(value = "mock:direct:debit")
	MockEndpoint mock;

	@Test
    @Order(1)    
	public void DbTest1() throws Exception {
		String str = "{\"transactionId\":\"000001\",\"noRef\":\"MOB220805000317539\",\"merchantType\":\"6017\",\"terminalId\":\"MOBILE0001\",\"dateTime\":\"2022-08-05T00:03:18.162\",\"originalNoRef\":\"MOB220805000317539\",\"originalDateTime\":\"2022-08-05T00:03:18.162\",\"categoryPurpose\":\"02\",\"debtorName\":\"DWI ZULMI PERMANA\",\"debtorType\":\"01\",\"debtorId\":\"3328140109900001\",\"debtorAccountNumber\":\"2652107662336\",\"debtorAccountType\":\"SVGS\",\"debtorResidentStatus\":\"01\",\"debtorTownName\":\"0906\",\"amount\":\"1500000.00\",\"feeTransfer\":\"0.00\",\"recipientBank\":\"CENAIDJA\",\"creditorName\":\"DWI ZULMI PERMANA\",\"creditorType\":\"01\",\"creditorId\":\"3328140109900001\",\"creditorAccountNumber\":\"0990413371\",\"creditorAccountType\":\"SVGS\",\"creditorResidentStatus\":\"01\",\"creditorTownName\":\"0300\"}";
		
		mock.expectedMessageCount(1);
		Object ret = producerTemplate.sendBody("direct:debit", ExchangePattern.InOut, str);
		mock.assertIsSatisfied();
		
		String resp = (String) ret;
		Assertions.assertDoesNotThrow(() -> {
			@SuppressWarnings("unused")
			CreditResponse bm = utilService.deserializeCreditResponse(resp);
		});
		CreditResponse cr = utilService.deserializeCreditResponse(resp);
		Assertions.assertEquals("ACTC", cr.getStatus());
		Assertions.assertEquals("U000", cr.getReason());
	}

}
