package bifast.mock.isoapt.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import bifast.mock.isoapt.pojo.AccountEnquiryRequest;
import bifast.mock.isoapt.pojo.AccountEnquiryResponse;

@Component
public class AccountEnquiryProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {

		AccountEnquiryRequest req = exchange.getMessage().getBody(AccountEnquiryRequest.class);
		
		AccountEnquiryResponse resp = new AccountEnquiryResponse();
		

		resp.setAccountNumber(req.getAccountNumber());
		resp.setAccountType("CACC");
		resp.setCreditorId("111");
		resp.setCreditorName("as");
		resp.setCreditorType("01");
		resp.setDateTime("");
		resp.setMerchantType("6010");
		resp.setNoRef("ACTC");
		resp.setReason("U000");
		resp.setResidentStatus("");
		resp.setStatus("ACTC");
		resp.setTerminalId(req.getTerminalId());
		resp.setTransactionId(req.getTransactionId());
		resp.setTownName("");

		exchange.getMessage().setBody(resp);
		
	}

}
