package bifast.mock.inbound.pojo;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("CreditTransferRequest")
public class CTRequestPojo {

	private String amount;
	
	private String debtorName;
	
	private String debtorAccountNo;
	private String debtorAccountType;
	private String debtorAgentId;
	private String creditorAgentId;
	private String creditorName;
	private String creditorPrvId;
	private String creditorOrgId;
	
	private String creditorAccountNo;
	private String creditorAccountType;
	
	private String paymentInfo;
	

	
}
