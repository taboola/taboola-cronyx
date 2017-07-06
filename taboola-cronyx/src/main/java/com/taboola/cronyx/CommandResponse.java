package com.taboola.cronyx;

public abstract class CommandResponse {

    public final ResponseType responseType;
    public final String message;

    protected CommandResponse(ResponseType responseType, String message) {
        this.message = message;
        this.responseType = responseType;
    }

    public static class SuccessfulResponse extends CommandResponse {
        public final Object result;

        public SuccessfulResponse(Object result) {
            super(ResponseType.SUCCESS, String.valueOf(result));
            this.result = result;
        }

        @Override
        public String toString() {
            return "SuccessfulResponse{" +
                    "responseType=" + responseType +
                    "message=" + "\'" + message + "\'" +
                    "} ";
        }
    }

    public static class FailedResponse extends CommandResponse {
        public final Throwable exception;

        public FailedResponse(Throwable exception, String errorMessage) {
            super(ResponseType.FAILURE, errorMessage);
            this.exception = exception;
        }

        @Override
        public String toString() {
            return "FailedResponse{" +
                    "responseType=" + responseType +
                    "message=" + "\'" + message + "\'" +
                    "exception=" + exception +
                    "} ";
        }


    }

    public enum ResponseType {SUCCESS, FAILURE}
}
