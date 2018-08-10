package com.groupdocs.ui.common.exception;

import com.groupdocs.ui.common.entity.web.ExceptionEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map application's exceptions into responses
 */
@Provider
public class TotalGroupDocsExceptionMapper implements ExceptionMapper<TotalGroupDocsException> {
    @Override
    public Response toResponse(TotalGroupDocsException exception) {
        return Response
                .serverError()
                .entity(new ExceptionEntity(exception.getMessage(), (Exception) exception.getCause()))
                .build();
    }
}
