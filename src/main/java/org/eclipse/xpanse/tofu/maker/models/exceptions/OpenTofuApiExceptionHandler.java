/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.response.Response;
import org.eclipse.xpanse.tofu.maker.models.response.ResultType;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Exception handler for exceptions thrown by the methods called by the API controller. */
@Slf4j
@RestControllerAdvice
public class OpenTofuApiExceptionHandler {

    /** Exception handler for IllegalArgumentException. */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public Response handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("handleIllegalArgumentException: ", ex);
        return Response.errorResponse(
                ResultType.UNPROCESSABLE_ENTITY, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for OpenTofuExecutorException. */
    @ExceptionHandler({OpenTofuExecutorException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public Response handleOpenTofuExecutorException(OpenTofuExecutorException ex) {
        log.error("handleOpenTofuExecutorException: ", ex);
        return Response.errorResponse(
                ResultType.OPENTOFU_EXECUTION_FAILED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for UnsupportedEnumValueException. */
    @ExceptionHandler({UnsupportedEnumValueException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public Response handleUnsupportedEnumValueException(UnsupportedEnumValueException ex) {
        log.error("handleUnsupportedEnumValueException: ", ex);
        return Response.errorResponse(
                ResultType.UNSUPPORTED_ENUM_VALUE, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for MethodArgumentTypeMismatchException. */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public Response handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.error("handleMethodArgumentTypeMismatchException: ", ex);
        return Response.errorResponse(
                ResultType.UNPROCESSABLE_ENTITY, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for MethodArgumentNotValidException. */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValidException: ", ex);
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.add(fieldError.getField() + ":" + fieldError.getDefaultMessage());
        }
        return Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY, errors);
    }

    /** Exception handler for HttpMessageConversionException. */
    @ExceptionHandler({HttpMessageConversionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleHttpMessageConversionException(HttpMessageConversionException ex) {
        log.error("handleHttpMessageConversionException: ", ex);
        String failMessage = ex.getMessage();
        return Response.errorResponse(
                ResultType.BAD_PARAMETERS, Collections.singletonList(failMessage));
    }

    /** Exception handler for GitRepoCloneException. */
    @ExceptionHandler({GitRepoCloneException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleGitRepoCloneException(GitRepoCloneException ex) {
        log.error("GitRepoCloneException: ", ex);
        String failMessage = ex.getMessage();
        return Response.errorResponse(
                ResultType.INVALID_GIT_REPO_DETAILS, Collections.singletonList(failMessage));
    }

    /** Exception handler for InvalidOpenTofuToolException. */
    @ExceptionHandler({InvalidOpenTofuToolException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleInvalidOpenTofuToolException(InvalidOpenTofuToolException ex) {
        log.error("handleInvalidOpenTofuToolException: ", ex);
        return Response.errorResponse(
                ResultType.INVALID_OPENTOFU_TOOL, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for InvalidOpenTofuScriptsException. */
    @ExceptionHandler({InvalidOpenTofuScriptsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleInvalidOpenTofuScriptsException(InvalidOpenTofuScriptsException ex) {
        log.error("handleInvalidOpenTofuScriptsException: ", ex);
        return Response.errorResponse(
                ResultType.INVALID_OPENTOFU_SCRIPTS, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for InvalidOpenTofuRequestException. */
    @ExceptionHandler({InvalidOpenTofuRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleInvalidOpenTofuRequestException(InvalidOpenTofuRequestException ex) {
        log.error("handleInvalidOpenTofuRequestException: ", ex);
        return Response.errorResponse(
                ResultType.INVALID_OPENTOFU_REQUEST, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for AmqpException. */
    @ExceptionHandler({AmqpException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleAmqpException(AmqpException ex) {
        log.error("handleAmqpException: ", ex);
        return Response.errorResponse(
                ResultType.SEND_AMQP_MESSAGE_FAILED, Collections.singletonList(ex.getMessage()));
    }
}
