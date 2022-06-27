package bifast.mock.isoapt.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import bifast.mock.isoapt.pojo.CreditRequest;
import bifast.mock.isoapt.pojo.CreditResponse;

@Component
public class CreditRequestProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {

		CreditRequest req = exchange.getMessage().getBody(CreditRequest.class);
		
		CreditResponse resp = new CreditResponse();
		
		resp.setAccountNumber(req.getCreditorAccountNumber());
		resp.setAdditionalInfo("");
		resp.setDateTime(req.getDateTime());
		resp.setMerchantType(req.getMerchantType());
		resp.setNoRef(req.getNoRef());
		resp.setReason("U000");
		resp.setStatus("ACTC");
		resp.setTerminalId(req.getTerminalId());
		resp.setTransactionId(req.getTransactionId());
		
		exchange.getMessage().setBody(resp);
		
	}

}
