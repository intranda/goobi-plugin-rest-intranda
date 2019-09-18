package org.goobi.api.rest.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpacConfig {
    private String opacName;
    private String searchField;
}
