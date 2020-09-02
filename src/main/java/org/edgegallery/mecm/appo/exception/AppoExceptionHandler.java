/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.mecm.appo.exception;

import static org.edgegallery.mecm.appo.utils.Constants.RECORD_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppoExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppoExceptionHandler.class);

    @ExceptionHandler(value = AppoException.class)
    public ResponseEntity<String> exception(AppoException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for DB operations.
     *
     * @param exception exception
     * @return return response
     */
    @ExceptionHandler(value = AppoProcessflowException.class)
    public ResponseEntity<String> exception(AppoProcessflowException exception) {
        if (exception.getMessage().equals(RECORD_NOT_FOUND)) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Returns response entity with error details when input validation is failed.
     *
     * @param ex exception while validating input
     * @return response entity with error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppoExceptionResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errorMsg = new ArrayList<>();
        if (ex.getBindingResult().hasErrors()) {
            ex.getBindingResult().getAllErrors().forEach(error -> errorMsg.add(error.getDefaultMessage()));
        }
        AppoExceptionResponse response = new AppoExceptionResponse(LocalDateTime.now(),
                "input validation failed", errorMsg);
        LOGGER.info("Method argument error: {}", response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Returns response entity with error details when input validation is failed.
     *
     * @param ex exception while validating input
     * @return response entity with error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AppoExceptionResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        AppoExceptionResponse response = new AppoExceptionResponse(LocalDateTime.now(),
            "input validation failed", Collections.singletonList("URL parameter validation failed"));
        LOGGER.info("Constraint violation error: {}", response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Returns error code and message when argument is illegal.
     *
     * @param ex exception while processing request
     * @return response entity with error code and message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AppoExceptionResponse> handleIllegalArgException(IllegalArgumentException ex) {

        AppoExceptionResponse response = new AppoExceptionResponse(LocalDateTime.now(),
                "Illegal argument", Collections.singletonList(ex.getMessage()));
        LOGGER.info("Illegal argument error: {}", response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Returns error code and message when record not found.
     *
     * @param ex exception while processing request
     * @return response entity with error code and message
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<AppoExceptionResponse> handleNoSuchElementException(NoSuchElementException ex) {
        AppoExceptionResponse response = new AppoExceptionResponse(LocalDateTime.now(),
                "No such element", Collections.singletonList(ex.getMessage()));
        LOGGER.info("No such element error: {}", response);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Returns error when runtime error happen.
     *
     * @param ex exception while processing request
     * @return response entity with error code and message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppoExceptionResponse> handleRuntimeException(RuntimeException ex) {
        AppoExceptionResponse response = new AppoExceptionResponse(LocalDateTime.now(),
                "Error while processing request", Collections.singletonList("Error while process request"));
        LOGGER.info("Internal server error: {}", response);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
