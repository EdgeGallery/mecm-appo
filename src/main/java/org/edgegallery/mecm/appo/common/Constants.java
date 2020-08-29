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

package org.edgegallery.mecm.appo.common;

public final class Constants {

    public static final String HOST_IP_REGX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)"
            + "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public static final String ID_REGX = "^[a-zA-Z0-9]$|^[a-zA-Z0-9][a-zA-Z0-9\\-][a-zA-Z0-9]$";
    public static final String APP_INST_ID_REGX
            = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";
    public static final String APPD_ID_REGEX = "[0-9a-f]{32}";
    public static final String APP_PKG_ID_REGX = APPD_ID_REGEX;
    public static final String TENENT_ID_REGEX = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";
    public static final String APP_NAME_REGEX = "^[a-zA-Z0-9]*$|^[a-zA-Z0-9][a-zA-Z0-9_\\-]*[a-zA-Z0-9]$";
    public static final String RECORD_NOT_FOUND = "Record not found";
    public static final String EMPTY_STRING = "";
    public static final String APP_INSTANCE_NOT_FOUND = "App instance not found";
    public static final String OPER_STATUS_INVALID_STATE = "Invalid state";

    public static final String REQUEST_ID = "request_id";
    public static final String TENANT_ID = "tenant_id";
    public static final String APP_PACKAGE_ID = "app_package_id";
    public static final String APP_NAME = "app_name";
    public static final String APP_DESCR = "app_instance_description";
    public static final String MEC_HOST = "mec_host";
    public static final String APP_INSTANCE_ID = "app_instance_id";
    public static final String APPD_ID = "appd_id";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String MEC_HOST_IP = "host_ip";
    public static final String APPLCM_IP = "applcm_ip";
    public static final String APPLCM_PORT = "applcm_port";
    public static final String APP_INSTANCE_INFO = "app_instance_info";

    public static final String APM_DOWNLOAD_URI = "/apm/v1/tenants/{tenant_id}/packages/{app_package_id}/download";
    public static final String INVENTORY_MEC_HOST_URI = "/inventory/v1/tenants/{tenant_id}/mechosts/{mec_host}";
    public static final String INVENTORY_APPLCM_URI = "/inventory/v1/tenants/{tenant_id}/applcms/{applcm_ip}";

    public static final String APPLCM_INSTANTIATE_URI = "/lcmbroker/v1/app_instances/{app_instance_id}";
    public static final String APPLCM_QUERY_URI = "/lcmbroker/v1/app_instances/{app_instance_id}";
    public static final String APPLCM_TERMINATE_URI = "/lcmbroker/v1/app_instances/{app_instance_id}";
    public static final String APPLCM_QUERY_KPI_URI = "/lcmbroker/v1/kpi";
    public static final String APPLCM_QUERY_CAPABILITY_URI = "/lcmbroker/v1/mep_capabilities";

    public static final String PROCESS_FLOW_SUCCESS = "200";
    public static final String PROCESS_FLOW_ERROR = "500";
    public static final String PROCESS_FLOW_RESP_CODE = "ProcessflowResponseCode";
    public static final String PROCESS_FLOW_RESP = "ProcessflowResponse";
    public static final String PROCESS_FLOW_ERR_RESP = "ProcessflowErrResponse";
    public static final String PROCESS_FLOW_EXCEPTION = "ProcessflowException";

    public static final Integer HTTP_STATUS_CODE_200 = 200;
    public static final Integer HTTP_STATUS_CODE_299 = 299;
    private Constants() {
    }
}
