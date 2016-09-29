package pro.hirooka.chukasa.api.v1.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pro.hirooka.chukasa.domain.chukasa.ChukasaExceptionModel;

@RestControllerAdvice
public class ChukasaExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers, HttpStatus status, WebRequest request) {
        ChukasaExceptionModel chukasaExceptionModel = new ChukasaExceptionModel();
        chukasaExceptionModel.setMessage(ex.getMessage());
        if(ex instanceof HttpRequestMethodNotSupportedException){
            //
        }else if(ex instanceof HttpMessageNotReadableException){
            chukasaExceptionModel.setMessage("Request body is invalid.");
        }

        return super.handleExceptionInternal(ex, chukasaExceptionModel, headers, status, request);
    }

    @ExceptionHandler(value={ChukasaBadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ChukasaExceptionModel handleBadRequest(ChukasaException ex) {
        ChukasaExceptionModel chukasaExceptionModel = new ChukasaExceptionModel();
        chukasaExceptionModel.setMessage(ex.getMessage());
        return chukasaExceptionModel;
    }

    @ExceptionHandler(value={ChukasaInternalServerErrorException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ChukasaExceptionModel handleInternalServerError(ChukasaException ex) {
        ChukasaExceptionModel chukasaExceptionModel = new ChukasaExceptionModel();
        chukasaExceptionModel.setMessage(ex.getMessage());
        return chukasaExceptionModel;
    }
}
