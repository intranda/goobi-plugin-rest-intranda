package org.goobi.api.rest.request;

import lombok.Data;

@Data
public class UpdateProcessRequest {
	private Integer id;
	private String titel;
	private String ausgabename;
	private Integer projectId;
	private Integer rulesetId;
	private Integer batchID;
}
