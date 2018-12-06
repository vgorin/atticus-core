package one.atticus.core.config;

import one.atticus.core.resources.ErrorMessage;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException e) {
        int statusCode = e.getResponse().getStatus();
        return Response.status(statusCode).entity(
                new ErrorMessage() {{
                    status = statusCode;
                    message = e.getMessage();
                }}
        ).type(MediaType.APPLICATION_JSON).build();
    }
}
