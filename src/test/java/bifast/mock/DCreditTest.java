package bifast.mock;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import bifast.mock.isoapt.pojo.CreditResponse;
import bifast.mock.persist.AccountProxyRepository;

@CamelSpringBootTest
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class DCreditTest {
	@Autowired AccountProxyRepository proxyRepo;
	@Autowired ProducerTemplate producerTemplate;
	@Autowired CamelContext camelContext;
	@Autowired TestUtilService utilService;

	@Test
    @Order(1)    
	public void CrTest1() throws Exception {
		String str = "{\"transactionId\":\"000001\",\"noRef\":\"KOM22080502126018419\",\"merchantType\":\"6666\",\"terminalId\":\"KOMI000001\",\"dateTime\":\"2022-08-05T00:12:50.353\",\"originalNoRef\":\"KOM22080500125018419\",\"originalDateTime\":\"2022-08-05T00:12:50.353\",\"categoryPurpose\":\"99\",\"debtorName\":\"PUTU KUSALIA PUCANGA\",\"debtorId\":\"5108042103650002\",\"debtorAccountNumber\":\"1450011680796\",\"debtorAccountType\":\"CACC\",\"amount\":\"600000.00\",\"feeTransfer\":\"0.00\",\"recipientBank\":\"SIHBIDJ1\",\"creditorName\":\"PUTU KUSALIA PUCANGAN\",\"creditorAccountNumber\":\"0112109307961\",\"creditorAccountType\":\"CACC\",\"paymentInformation\":\"Others\"}";
		Object ret = producerTemplate.sendBody("direct:credit", ExchangePattern.InOut, str);

		String resp = (String) ret;
		Assertions.assertDoesNotThrow(() -> {
			@SuppressWarnings("unused")
			CreditResponse bm = utilService.deserializeCreditResponse(resp);
		});
		CreditResponse cr = utilService.deserializeCreditResponse(resp);
		Assertions.assertEquals("ACTC", cr.getStatus());
		Assertions.assertEquals("U000", cr.getReason());
	}

	@Test
    @Order(1)    
	public void CrTimeoutTest() throws Exception {
		String str = "{\"transactionId\":\"000001\",\"noRef\":\"KOM22080502126018420\",\"merchantType\":\"6666\",\"terminalId\":\"KOMI000001\",\"dateTime\":\"2022-08-05T00:12:50.353\",\"originalNoRef\":\"KOM22080500125018419\",\"originalDateTime\":\"2022-08-05T00:12:50.353\",\"categoryPurpose\":\"99\",\"debtorName\":\"PUTU KUSALIA PUCANGA\",\"debtorId\":\"5108042103650002\",\"debtorAccountNumber\":\"1450011680796\",\"debtorAccountType\":\"CACC\",\"amount\":\"600000.00\",\"feeTransfer\":\"0.00\",\"recipientBank\":\"SIHBIDJ1\",\"creditorName\":\"PUTU KUSALIA PUCANGAN\",\"creditorAccountNumber\":\"0112109307961\",\"creditorAccountType\":\"CACC\",\"paymentInformation\":\"credittimeout\"}";
		Object ret = producerTemplate.sendBody("direct:credit", ExchangePattern.InOut, str);
		String resp = (String) ret;

		Assertions.assertDoesNotThrow(() -> {
			@SuppressWarnings("unused")
			CreditResponse bm = utilService.deserializeCreditResponse(resp);
		});
		CreditResponse cr = utilService.deserializeCreditResponse(resp);
		Assertions.assertEquals("RJCT", cr.getStatus());
		Assertions.assertEquals("U900", cr.getReason());
	}
   
}
