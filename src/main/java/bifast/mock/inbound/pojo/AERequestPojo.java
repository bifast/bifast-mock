package bifast.mock.inbound.pojo;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("AccountEnquiryRequest")
public class AERequestPojo {

	private String amount;
	private String creditorAccountNo;
	private String paymentInfo;
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCreditorAccountNo() {
		return creditorAccountNo;
	}
	public void setCreditorAccountNo(String creditorAccountNo) {
		this.creditorAccountNo = creditorAccountNo;
	}
	public String getPaymentInfo() {
		return paymentInfo;
	}
	public void setPaymentInfo(String paymentInfo) {
		this.paymentInfo = paymentInfo;
	}
	

	
	
}
