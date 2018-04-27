package org.goobi.api.rest.response;

import java.util.Map;

import lombok.Data;

@Data
public class RestMetadata {
	private String value;
	private Map<String, String> labels;
	private String authorityID;
	private String authorityValue;
	private String authorityURI;
}
