package bifast.mock.isoapt;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import bifast.mock.isoapt.pojo.SettlementRequest;
import bifast.mock.isoapt.pojo.SettlementResponse;

@Component
public class SettlementRequestProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		SettlementRequest req = exchange.getMessage().getBody(SettlementRequest.class);
		
		SettlementResponse resp = new SettlementResponse();
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
