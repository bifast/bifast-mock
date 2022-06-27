package bifast.mock.inbound.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InboundMockWrapper {

	@JsonProperty("AccountEnquiryRequest")
	private AERequestPojo aeRequest;
	@JsonProperty("CreditTransferRequest")
	private CTRequestPojo ctRequest;
	@JsonProperty("PaymentRequest")
	private PaymentRequestPojo paymentRequest;
	
	public AERequestPojo getAeRequest() {
		return aeRequest;
	}
	public void setAeRequest(AERequestPojo aeRequest) {
		this.aeRequest = aeRequest;
	}
	public CTRequestPojo getCtRequest() {
		return ctRequest;
	}
	public void setCtRequest(CTRequestPojo ctRequest) {
		this.ctRequest = ctRequest;
	}
	public PaymentRequestPojo getPaymentRequest() {
		return paymentRequest;
	}
	public void setPaymentRequest(PaymentRequestPojo paymentRequest) {
		this.paymentRequest = paymentRequest;
	}
	
	
}
