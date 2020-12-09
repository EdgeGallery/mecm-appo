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

package org.edgegallery.mecm.appo.utils;

public final class Constants {

    public static final String HOST_IP_REGX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)"
            + "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public static final String ID_REGX = "^[a-zA-Z0-9]$|^[a-zA-Z0-9][a-zA-Z0-9\\-][a-zA-Z0-9]$";
    public static final String APP_INST_ID_REGX
            = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";

    public static final String IP_CIRD_REGX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)"
            + "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/([0-9]|[1-2][0-9]|3[0-2]))?$";
    public static final String MAC_ADDRESS_REGX = "^(?:[0-9A-Fa-f]{2}([-: ]?))(?:[0-9A-Fa-f]{2}\1){4}[0-9A-Fa-f]{2}|"
            + "([0-9A-Fa-f]{4}\\.){2}[0-9A-Fa-f]{4}$";
    public static final String PORT_REGEX = "^([1-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d"
            + "|6553[0-5])$";
    public static final String APPD_ID_REGEX = "[0-9a-f]{32}";
    public static final String APP_PKG_ID_REGX = APPD_ID_REGEX;
    public static final String TENENT_ID_REGEX = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";
    public static final String APP_NAME_REGEX = "^[a-zA-Z0-9]*$|^[a-zA-Z0-9][a-zA-Z0-9_\\-]*[a-zA-Z0-9]$";
    public static final String RECORD_NOT_FOUND = "Record not found";
    public static final String APP_INSTANCE_NOT_FOUND = "Application instance id does not exist ";

    public static final String TENANT_ID = "tenant_id";
    public static final String APP_PACKAGE_ID = "app_package_id";
    public static final String APP_NAME = "app_name";
    public static final String APP_DESCR = "app_instance_description";
    public static final String MEC_HOST = "mec_host";
    public static final String MEC_HOSTS = "mec_hosts";
    public static final String APP_REQ_CNT = "app_req_cnt";
    public static final String APP_INSTANCE_ID = "app_instance_id";
    public static final String APP_INSTANCE_IDS = "app_instance_ids";
    public static final String APP_ID = "app_id";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String MEC_HOST_IP = "host_ip";
    public static final String APPLCM_IP = "applcm_ip";
    public static final String APPRULE_IP = "app_rule_manager_ip";
    public static final String APPLCM_PORT = "applcm_port";
    public static final String APPRULE_PORT = "apprule_port";
    public static final String APP_INSTANCE_INFO = "app_instance_info";
    public static final String MEP_CAPABILITY_ID = "capability_id";
    public static final String HW_CAPABILITIES = "hw_capabilities";
    public static final String APP_RULES = "app_rules";
    public static final String APP_RULE_CFG_STATUS = "app_rule_status";
    public static final String INVENTORY_APP_RULES = "inventory_app_rules";
    public static final String UPDATED_APP_RULES = "updated_app_rules";
    public static final String APP_RULE_ACTION = "app_rule_action";
    public static final String APPRULE_TASK_ID = "apprule_task_id";

    public static final String APM_DOWNLOAD_URI = "/apm/v1/tenants/{tenant_id}/packages/{app_package_id}/download";
    public static final String INVENTORY_MEC_HOST_URI = "/inventory/v1/tenants/{tenant_id}/mechosts/{mec_host}";
    public static final String INVENTORY_APPLCM_URI = "/inventory/v1/tenants/{tenant_id}/applcms/{applcm_ip}";
    public static final String INVENTORY_APPLICATIONS_URI = "/inventory/v1/tenants/{tenant_id}"
            + "/mechosts/{mec_host}/apps";
    public static final String INVENTORY_APPRULECFG_URI = "/inventory/v1/tenants/{tenant_id}/apprulemanagers"
            + "/{app_rule_manager_ip}";
    public static final String INVENTORY_APPLICATION_URI = "/inventory/v1/tenants/{tenant_id}"
            + "/mechosts/{mec_host}/apps/{app_instance_id}";

    public static final String APPLCM_INSTANTIATE_URI =
            "/lcmcontroller/v1/tenants/{tenant_id}/app_instances/{app_instance_id}/instantiate";
    public static final String APPLCM_QUERY_URI = "/lcmcontroller/v1/tenants/{tenant_id}/app_instances"
            + "/{app_instance_id}";
    public static final String APPLCM_TERMINATE_URI =
            "/lcmcontroller/v1/tenants/{tenant_id}/app_instances/{app_instance_id}/terminate";
    public static final String APPLCM_QUERY_KPI_URI = "/lcmcontroller/v1/tenants/{tenant_id}/hosts/{mec_host}/kpi";
    public static final String APPLCM_QUERY_CAPABILITIES_URI = "/lcmcontroller/v1/tenants/{tenant_id}/hosts/{mec_host"
            + "}/mep_capabilities";
    public static final String APPLCM_QUERY_CAPABILITY_URI = "/lcmcontroller/v1/tenants/{tenant_id}/hosts/{mec_host"
            + "}/mep_capabilities/{capability_id}";
    public static final String APPRULE_URI =
            "/apprulemgr/v1/tenants/{tenant_id}/app_instances/{app_instance_id}/appd_configuration";
    public static final String INVENTORY_APPRULE_URI =
            "/inventory/v1/tenants/{tenant_id}/app_instances/{app_instance_id}/appd_configuration";

    public static final String PROCESS_FLOW_SUCCESS = "200";
    public static final String PROCESS_FLOW_ERROR = "500";
    public static final String PROCESS_FLOW_ERROR_400 = "400";
    public static final String PROCESS_RECORD_NOT_FOUND = "404";
    public static final String PROCESS_FLOW_RESP_CODE = "ProcessflowResponseCode";
    public static final String PROCESS_FLOW_RESP = "ProcessflowResponse";
    public static final String PROCESS_FLOW_ERR_RESP = "ProcessflowErrResponse";
    public static final String PROCESS_FLOW_EXCEPTION = "ProcessflowException";

    public static final Integer HTTP_STATUS_CODE_200 = 200;
    public static final Integer HTTP_STATUS_CODE_299 = 299;
    public static final String SUCCESS = "success";

    public static final int MAX_ENTRY_PER_TENANT_PER_MODEL = 50;
    public static final int MAX_TENANTS = 10;
    public static final String MAX_LIMIT_REACHED_ERROR = "Max record limit exceeded";

    public static final String SLASH = "/";
    public static final String APP_PKG_EXT = ".csar";

    public static final String CSAR_DOWNLOAD_FAILED = "failed to download app package for package {}";
    public static final String FAILED_TO_READ_INPUTSTREAM = "failed to read input stream from app store for package {}";
    public static final String FAILED_TO_CONNECT_APM = "failed to connect to APM";
    public static final String GET_INPUTSTREAM_FAILED = "failed to get input stream from app store response for "
            + "package {}";

    public static final String FAILED_TO_CONNECT_INVENTORY = "failed to connect to inventory {}";
    public static final String FAILED_TO_CONNECT_APPLCM = "failed to connect to applcm {}";
    public static final String FAILED_TO_CONNECT_APPRULE = "failed to connect to apprule {}";
    public static final String APPLCM_RETURN_FAILURE = "applcm return failure {}";
    public static final String APPRULE_RETURN_FAILURE = "apprule return failure {}";
    public static final String INVENTORY_RETURN_FAILURE = "inventory return failure {}";
    public static final String APM_RETURN_FAILURE = "apm return failure {}";


    private Constants() {
    }
}
