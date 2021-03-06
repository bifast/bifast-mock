package bifast.mock.isoapt.processor;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import bifast.mock.isoapt.pojo.DebitReversalRequestPojo;
import bifast.mock.isoapt.pojo.DebitReversalResponsePojo;

@Component
public class DebitReversalProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {

		DebitReversalRequestPojo req = exchange.getMessage().getBody(DebitReversalRequestPojo.class);
		
		DebitReversalResponsePojo resp = new DebitReversalResponsePojo();
		
		resp.setAccountNumber(req.getCreditorAccountNumber());
		resp.setAdditionalInfo(req.getPaymentInformation());
		
		resp.setDateTime(req.getDateTime());
		resp.setMerchantType(req.getMerchantType());
		resp.setNoRef(req.getNoRef());

		String pymtInfo = Optional.ofNullable(req.getPaymentInformation()).orElse("").toLowerCase();
		
		if (pymtInfo.contains("rev904")) {
			resp.setReason("U904");
			resp.setStatus("RJCT");			
		}

		else if ( (pymtInfo.contains("reversaltimeout")) || (pymtInfo.contains("cb-timeout"))) {
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
