package bifast.mock.inbound.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("PaymentRequest")
public class PaymentRequestPojo {

	private String amount;
	private String debtorName;
	
	private String debtorAccountNo;
	private String creditorName;
	private String creditorAccountNo;
	private String paymentInfo;
	
	@JsonCreator
	public PaymentRequestPojo(
			@JsonProperty(value="amount", required=true) String amount,
			@JsonProperty(value="debtorName") String debtorName,
			@JsonProperty(value="debtorAccountNo") String debtorAccountNo,
			@JsonProperty(value="creditorName") String creditorName,
			@JsonProperty("creditorAccountNo") String creditorAccountNo,
			@JsonProperty("paymentInfo") String paymentInfo)
	{
		this.amount = amount;
		this.debtorName = debtorName;
		this.debtorAccountNo = debtorAccountNo;
		this.creditorName = creditorName;
		this.creditorAccountNo = creditorAccountNo;
		this.paymentInfo = paymentInfo;
	}

	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDebtorName() {
		return debtorName;
	}
	public void setDebtorName(String debtorName) {
		this.debtorName = debtorName;
	}
	public String getDebtorAccountNo() {
		return debtorAccountNo;
	}
	public void setDebtorAccountNo(String debtorAccountNo) {
		this.debtorAccountNo = debtorAccountNo;
	}
	public String getCreditorName() {
		return creditorName;
	}
	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
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
