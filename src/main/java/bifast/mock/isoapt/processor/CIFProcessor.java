package bifast.mock.isoapt.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import bifast.mock.isoapt.pojo.CIFRequest;
import bifast.mock.isoapt.pojo.CIFResponse;

@Component
public class CIFProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {

		CIFRequest req = exchange.getMessage().getBody(CIFRequest.class);
		
		CIFResponse resp = new CIFResponse();

		resp.setAccountType("SAVG");
		resp.setCustomerId("02004444");
		resp.setCustomerIdType("01");
		resp.setCustomerName("ANDRE");
		resp.setCustomerType("01");
		
		List<String> email = new ArrayList<>();
		email.add("frans@yahoo.com");
		email.add("frans@yihaa.com");
		resp.setEmailAddressList(email);
		List<String> phone = new ArrayList<>();
		phone.add("6281818181");
		resp.setPhoneNumberList(phone);
		
		
		resp.setAccountNumber(req.getAccountNumber());
		resp.setTownName("0300");
		resp.setResidentStatus("01");

		resp.setNoRef("ACTC");
		resp.setDateTime("");
		resp.setMerchantType("6010");
		resp.setTerminalId(req.getTerminalId());
		resp.setTransactionId(req.getTransactionId());

		resp.setReason("U000");
		resp.setStatus("ACTC");

		if (req.getNoRef().contains("TO")) 
			exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, "504");

		exchange.getMessage().setBody(resp);
		
	}

}
