package com.groupdocs.ui.common.exception;

import com.groupdocs.ui.common.entity.web.ExceptionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map application's exceptions into responses
 */
@Provider
public class TotalGroupDocsExceptionMapper implements ExceptionMapper<TotalGroupDocsException> {
    private static final Logger logger = LoggerFactory.getLogger(TotalGroupDocsExceptionMapper.class);
    @Override
    public Response toResponse(TotalGroupDocsException exception) {
        ExceptionEntity exceptionEntity = new ExceptionEntity();
        exceptionEntity.setMessage(exception.getMessage());
        if (logger.isDebugEnabled()) {
            exception.printStackTrace();
            exceptionEntity.setException(exception);
        }
        logger.error(exception.getCause() != null? exception.getCause().getLocalizedMessage() : exception.getMessage());
        return Response
                .serverError()
                .entity(exceptionEntity)
                .build();
    }
}
