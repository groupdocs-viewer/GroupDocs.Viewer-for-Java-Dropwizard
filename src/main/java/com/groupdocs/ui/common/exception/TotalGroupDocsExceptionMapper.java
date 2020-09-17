package com.groupdocs.ui.common.exception;

import com.groupdocs.ui.viewer.model.response.ExceptionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.groupdocs.ui.common.exception.PasswordExceptions.INCORRECT_PASSWORD;
import static com.groupdocs.ui.common.exception.PasswordExceptions.PASSWORD_REQUIRED;

/**
 * Map application's exceptions into responses
 */
@Provider
public class TotalGroupDocsExceptionMapper implements ExceptionMapper<TotalGroupDocsException> {
    private static final Logger logger = LoggerFactory.getLogger(TotalGroupDocsExceptionMapper.class);
    @Override
    public Response toResponse(TotalGroupDocsException exception) {
        ExceptionEntity exceptionEntity = new ExceptionEntity();
        String message = exception.getMessage();
        exceptionEntity.setMessage(message);
        if (PASSWORD_REQUIRED.equals(message) || INCORRECT_PASSWORD.equals(message)) {
            return Response.status(Response.Status.FORBIDDEN).entity(exceptionEntity).build();
        }
        if (logger.isDebugEnabled()) {
            exception.printStackTrace();
            exceptionEntity.setException(exception);
        }
        logger.error(exception.getCause() != null? exception.getCause().getLocalizedMessage() : message);
        return Response
                .serverError()
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(exceptionEntity)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
