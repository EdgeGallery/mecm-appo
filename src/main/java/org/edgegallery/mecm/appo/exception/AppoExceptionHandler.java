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

import static org.edgegallery.mecm.appo.common.Constants.RECORD_NOT_FOUND;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AppoExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = AppoException.class)
    public ResponseEntity<Object> exception(AppoException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for DB operations.
     *
     * @param exception exception
     * @return return response
     */
    @ExceptionHandler(value = AppoDbException.class)
    public ResponseEntity<Object> exception(AppoDbException exception) {
        if (exception.getMessage().equals(RECORD_NOT_FOUND)) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        AppoExceptionResponse appoExceptionResponse = new AppoExceptionResponse("Validation Failed",
                ex.getMessage());
        return new ResponseEntity(appoExceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
