package bifast.mock.route;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


@Component
public class PortalRoute extends RouteBuilder {
		
	@Override
	public void configure() throws Exception {
		
		restConfiguration()
			.component("servlet")
		;
		
		rest("/")
			.post("/portalapi")
				.consumes("application/json")
				.to("direct:portal")

			.post("/notifcustomer")
				.consumes("application/json")
				.to("direct:portal")

			.post("/notifadmin")
				.consumes("application/json")
				.to("direct:portal")
		;
	

		from("direct:portal").routeId("portalapi")

			.setExchangePattern(ExchangePattern.InOnly)
			.convertBodyTo(String.class)
//			.log("Terima di portal-api")
//			.log("${body}")
			.setBody(constant(null))
			
			.removeHeaders("*")
		;
		
	}

}
