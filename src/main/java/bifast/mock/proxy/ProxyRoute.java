package bifast.mock.proxy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;

@Component
public class ProxyRoute extends RouteBuilder {

    @Autowired
	private ProxyRegistrationProcessor proxyRegistrationPrc;
	@Autowired
	private ProxyResolutionResponseProc proxyResolutionResponseProcessor;
	@Autowired
	private ProxyRegistrationInquiryProcessor proxyRegistrationInquiryProcessor;
	
	JacksonDataFormat jsonBusinessMessageDataFormat = new JacksonDataFormat(BusinessMessage.class);

	private void configureJson() {
		jsonBusinessMessageDataFormat.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		jsonBusinessMessageDataFormat.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		jsonBusinessMessageDataFormat.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		jsonBusinessMessageDataFormat.setInclude("NON_NULL");
		jsonBusinessMessageDataFormat.setInclude("NON_EMPTY");
	}
	
    @Override
    public void configure() throws Exception {

    	configureJson();

        from("direct:prxyregn").routeId("proxyregistration")
        	
            .log("Terima di mock Regis")
            .process(proxyRegistrationPrc)
            .log("End Process")
            
        ;
        
        
        from("direct:prxyreso").routeId("proxyresolution")
        
	        .log("Terima di mock")
	        .log("${body}")
	        .delay(500)
	        .log("end-delay")
	        .process(proxyResolutionResponseProcessor)
	        .log("End Process")
	        
	    ;
        
        from("direct:prxyregninquiry").routeId("proxyregistrationinquiry")
        
	        .log("Terima di mock")
	        .log("${body}")
	        .log("Run Processor Inquiry")
	        .process(proxyRegistrationInquiryProcessor)
	        .log("End Process")
	        
	    ;
    
    }
    
}
