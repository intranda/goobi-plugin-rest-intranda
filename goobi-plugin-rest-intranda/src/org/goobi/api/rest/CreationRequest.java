package org.goobi.api.rest;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import org.apache.commons.lang.StringUtils;

@XmlRootElement
public @Data class CreationRequest {

    private int process_template;
    private String identifier;
    private String signature; // optional
    private String lastname;
    private String salutation;
    private String email;
    private int order_number;
    private int item_in_order;
    private int btw_number; // optional
    private boolean all_pages;
    private String page_numbers;
    private String client_instructions;

    public String validateRequest(CreationRequest req) {
        String text = "";

        if (req.getProcess_template() == 0) {
            text = addErrorMessage(text, "process_template");
        }

        if (StringUtils.isBlank(req.getIdentifier())) {
            text = addErrorMessage(text, "identifier");
        }

        if (StringUtils.isBlank(req.getLastname())) {
            text = addErrorMessage(text, "lastname");
        }

        if (StringUtils.isBlank(req.getSalutation())) {
            text = addErrorMessage(text, "salutation");
        }

        if (StringUtils.isBlank(req.getEmail())) {
            text = addErrorMessage(text, "email");
        }

        if (StringUtils.isBlank(req.getPage_numbers())) {
            text = addErrorMessage(text, "page_numbers");
        }
        if (req.getOrder_number() == 0) {
            text = addErrorMessage(text, "order_number");
        }

        if (req.getItem_in_order() == 0) {
            text = addErrorMessage(text, "item_in_order");
        }
        return text;
    }

    private String addErrorMessage(String text, String field) {
        if (StringUtils.isBlank(text)) {
            text = "field '" + field + "' is missing or empty";
        } else {
            text = text + "; field '" + field + "' is missing or empty";
        }
        return text;
    }
}
