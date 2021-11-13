/*
 *  Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.mecm.appo.utils;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AppoV2Response {

    private Object data;

    private int retCode;

    private List<String> params;

    private String message;

    /**
     * construct.
     *
     */
    public AppoV2Response(Object result, ErrorMessage errorMsg, String detailMsg) {
        this.data = result;
        this.retCode = errorMsg.getRetCode();
        this.params = errorMsg.getParams();
        this.message = detailMsg;
    }
}
