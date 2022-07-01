package bifast.mock.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bifast.library.iso20022.custom.BusinessMessage;

@RestController
@RequestMapping(value="/mock")
public class CiConnController {

	@Autowired private ProducerTemplate template;

	@PostMapping(value = "/ciconn", consumes=MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public BusinessMessage getMessage(@RequestBody String request) throws Exception {
		BusinessMessage response = (BusinessMessage) template.requestBody("direct:transaction", request);
		return response;
	}
}
