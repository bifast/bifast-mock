package bifast.mock.isoapt.processor;

import java.util.Optional;

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
		
		String pymtInfo = Optional.ofNullable(req.getPaymentInformation()).orElse("").toLowerCase();
		if ((pymtInfo.contains("cbtimeout")) ||
			(pymtInfo.contains("credittimeout")) ) {
			exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, "504");
			resp.setReason("U900");
			resp.setStatus("RJCT");			
		}
		else {
			resp.setReason("U000");
			resp.setStatus("ACTC");
		}

		resp.setTerminalId(req.getTerminalId());
		resp.setTransactionId(req.getTransactionId());
		
		exchange.getMessage().setBody(resp);
		
	}

}
