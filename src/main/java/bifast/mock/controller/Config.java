package bifast.mock.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

@Configuration
public class Config {

	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		Jackson2ObjectMapperBuilder map = new Jackson2ObjectMapperBuilder();
		map.modulesToInstall(new JaxbAnnotationModule());
		map.serializationInclusion(JsonInclude.Include.NON_NULL);
		map.featuresToEnable(SerializationFeature.WRAP_ROOT_VALUE);
		map.featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		return map;
	}

}
