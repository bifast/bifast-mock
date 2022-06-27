package bifast.mock.isoapt.pojo;

import java.util.List;

public class CIFResponse extends BaseResponseDTO {

    private List<String> emailAddressList;

    private List<String> phoneNumberList;

    private String accountNumber;

    private String accountType;

    private String customerName;

    private String customerType;

    private String customerId;

    private String customerIdType;

    private String residentStatus;

    private String townName;

	public List<String> getEmailAddressList() {
		return emailAddressList;
	}

	public void setEmailAddressList(List<String> emailAddressList) {
		this.emailAddressList = emailAddressList;
	}

	public List<String> getPhoneNumberList() {
		return phoneNumberList;
	}

	public void setPhoneNumberList(List<String> phoneNumberList) {
		this.phoneNumberList = phoneNumberList;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerIdType() {
		return customerIdType;
	}

	public void setCustomerIdType(String customerIdType) {
		this.customerIdType = customerIdType;
	}

	public String getResidentStatus() {
		return residentStatus;
	}

	public void setResidentStatus(String residentStatus) {
		this.residentStatus = residentStatus;
	}

	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}

    
}
