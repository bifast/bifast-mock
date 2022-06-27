package bifast.mock.persist;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Alias {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String registerBank;
    private String accountNumber;
    private String accountType;
    private String accountName;
    private String proxyType;
    private String proxyId;
    private String displayName;
    private String registrationId;
    private String customerId;
    private String customerIdType;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRegisterBank() {
        return registerBank;
    }
    public void setRegisterBank(String registerBank) {
        this.registerBank = registerBank;
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
    public String getAccountName() {
        return accountName;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    public String getProxyType() {
        return proxyType;
    }
    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }
    public String getProxyId() {
        return proxyId;
    }
    public void setProxyId(String proxyId) {
        this.proxyId = proxyId;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getRegistrationId() {
        return registrationId;
    }
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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

    
}
