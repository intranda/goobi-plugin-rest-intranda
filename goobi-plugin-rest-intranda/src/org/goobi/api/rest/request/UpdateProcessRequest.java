package org.goobi.api.rest.request;

import lombok.Data;

@Data
public class UpdateProcessRequest {
    private Action action;
    private long otherProcessId;

    public enum Action {
        LINKWITHOTHER("=");

        private final String sqlStr;

        private Action(String value) {
            this.sqlStr = value;
        }

        @Override
        public String toString() {
            return sqlStr;
        }
    }

    public void apply(Process p) {
        switch (this.action) {
            case LINKWITHOTHER:
                break;
            default:
                break;

        }
    }
}
