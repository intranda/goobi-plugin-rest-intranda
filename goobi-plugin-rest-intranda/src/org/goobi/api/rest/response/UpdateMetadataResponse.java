package org.goobi.api.rest.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UpdateMetadataResponse {
	private List<String> errors = new ArrayList<>();
	private boolean error;

	public void addErrorMessage(String string) {
		this.errors.add(string);
	}
}
