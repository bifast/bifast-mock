package bifast.mock.isoapt.pojo;

public class CreditResponse extends BaseResponseDTO {

    private String additionalInfo;
    
    private String accountNumber;

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}


}
