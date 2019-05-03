package org.knowm.xchange.b2c2.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import si.mazi.rescu.HttpStatusExceptionSupport;

public class B2C2Exception extends HttpStatusExceptionSupport {

  public final List<Error> errors;

  public B2C2Exception(@JsonProperty("errors") List<Error> errors) {
    super();
    this.errors = errors;
  }

  public static class Error {
    public final String message;
    public final Integer code;

    public Error(@JsonProperty("message") String message, @JsonProperty("code") Integer code) {
      this.message = message;
      this.code = code;
    }
  }
}
