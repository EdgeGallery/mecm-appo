#!/bin/bash
# Copyright 2020 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Contain at most 63 characters
# Contain only lowercase alphanumeric characters or '-'
# Start with an alphanumeric character
# End with an alphanumeric character
validate_host_name()
{
 hostname="$1"
 len="${#hostname}"
 if [ "${len}" -gt "253" ] ; then
   return 1
 fi
 if ! echo "$hostname" | grep -qE '^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])(\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9]))*$' ; then
   return 1
 fi
 return 0
}

validate_name()
{
 hostname="$1"
 len="${#hostname}"
 if [ "${len}" -gt "64" ] ; then
   return 1
 fi
 if ! echo "$hostname" | grep -qE '^[a-zA-Z0-9]*$|^[a-zA-Z0-9][a-zA-Z0-9_\-]*[a-zA-Z0-9]$' ; then
   return 1
 fi
 return 0
}

# Validating if port is > 1 and < 65535 , not validating reserved port.
validate_port_num()
{
 portnum="$1"
 len="${#portnum}"
 if [ "${len}" -gt "5" ] ; then
   return 1
 fi
 if ! echo "$portnum" | grep -qE '^-?[0-9]+$' ; then
   return 1
 fi
 if [ "$portnum" -gt "65535" ] || [ "$portnum" -lt "1" ] ; then
   return 1
 fi
 return 0
}

# Validating password.
# 1. password length should be more than 8 and less than 16
# 2. password must contain at least two types of the either one lowercase " +
#		 "character, one uppercase character, one digit or one special character
validate_password()
{
 password="$1"
 len="${#password}"
 if [ "${len}" -gt "16" ] || [ "${len}" -lt "8" ] ; then
   echo "password must not be less than 8 characters and more than 16 characters"
   return 1
 fi

 count=0
 if [[ "$password" =~ [A-Z] ]] ; then
   ((++count))
 fi

 if [[ "$password" =~ [a-z] ]] ; then
   ((++count))
 fi

 if [[ "$password" =~ [0-9] ]] ; then
   ((++count))
 fi

 if [[ "$password" =~ [@#$%^'&'-+='('')'] ]] ; then
   ((++count))
 fi

 if [ "${count}" -lt "2" ] ; then
   echo "password must have atleast one uppercase character, lowercase character, digit or special character"
   return 1
 fi

 return 0
}

# validates whether file exist
validate_file_exists()
{
   file_path="$1"

   # checks variable is unset
   if [ -z "$file_path" ] ; then
     echo "file path variable is not set"
     return 1
   fi

   # checks if file exists
   if [ ! -f "$file_path" ] ; then
     echo "file does not exist"
     return 1
   fi

   return 0
}

# Validates if dir exists
validate_dir_exists()
{
   dir_path="$1"

   # checks if dir path var is unset
   if [ -z "$dir_path" ] ; then
     echo "dir path variable is not set"
     return 1
   fi

   # checks if dir exists
   if [ ! -d "$dir_path" ] ; then
     echo "dir does not exist"
     return 1
   fi

   return 0
}

# Validates if variable is set
validate_var_not_empty()
{
   env_var="$1"

   # checks if variable is unset
   if [ -z "$env_var" ] ; then
     return 1
   fi

   return 0
}

# ssl parameters validation
validate_file_exists "/usr/app/ssl/keystore.p12"
valid_ssl_key_store_path="$?"
if [ ! "$valid_ssl_key_store_path" -eq "0" ] ; then
   echo "invalid ssl key store path"
   exit 1
fi

validate_password "$SSL_KEY_STORE_PASSWORD"
valid_ssl_key_store_password="$?"
if [ ! "$valid_ssl_key_store_password" -eq "0" ] ; then
   echo "invalid ssl key store password, complexity validation failed"
   exit 1
fi

if [ ! -z "$SSL_KEY_STORE_TYPE" ] ; then
   validate_name "$SSL_KEY_STORE_TYPE"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid ssl key store type"
      exit 1
   fi
fi

if [ ! -z "$SSL_KEY_ALIAS" ] ; then
   validate_name "$SSL_KEY_ALIAS"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid ssl key alias"
      exit 1
   fi
fi

validate_file_exists "/usr/app/ssl/keystore.jks"
valid_ssl_trust_store="$?"
if [ ! "$valid_ssl_trust_store" -eq "0" ] ; then
   echo "ssl trust store does not exist"
   exit 1
fi

validate_password "$SSL_TRUST_PASSWORD"
valid_ssl_trust_password="$?"
if [ ! "$valid_ssl_trust_password" -eq "0" ] ; then
   echo "invalid ssl trust store password, complexity validation failed"
   exit 1
fi

# db parameters validation
if [ ! -z "$APPO_DB" ] ; then
   validate_name "$APPO_DB"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid DB name"
      exit 1
   fi
fi

# db parameters validation
if [ ! -z "$APPO_DB_USER" ] ; then
   validate_name "$APPO_DB_USER"
   valid_name="$?"
   if [ ! "$valid_name" -eq "0" ] ; then
      echo "invalid DB user name"
      exit 1
   fi
fi

if [ ! -z "$APPO_DB_HOST" ] ; then
   validate_host_name "$APPO_DB_HOST"
   valid_db_host_name="$?"
   if [ ! "$valid_db_host_name" -eq "0" ] ; then
      echo "invalid db host name"
      exit 1
   fi
fi

if [ ! -z "$APPO_DB_PORT" ] ; then
   validate_port_num "$APPO_DB_PORT"
   valid_APPO_db_port="$?"
   if [ ! "$valid_APPO_db_port" -eq "0" ] ; then
      echo "invalid APPO db port number"
      exit 1
   fi
fi

validate_password "$APPO_DB_PASSWORD"
valid_appodb_password="$?"
if [ ! "$valid_appodb_password" -eq "0" ] ; then
   echo "invalid appodb password, complexity validation failed"
   exit 1
fi

if [ ! -z "$INVENTORY_ENDPOINT" ] ; then
   validate_host_name "$INVENTORY_ENDPOINT"
   valid_inventory_endpoint_name="$?"
   if [ ! "$valid_inventory_endpoint_name" -eq "0" ] ; then
      echo "invalid inventory service name"
      exit 1
   fi
fi

if [ ! -z "$INVENTORY_PORT" ] ; then
   validate_port_num "$INVENTORY_PORT"
   valid_INVENTORY_port="$?"
   if [ ! "$valid_INVENTORY_port" -eq "0" ] ; then
      echo "invalid INVENTORY port number"
      exit 1
   fi
fi

if [ ! -z "$APM_ENDPOINT" ] ; then
   validate_host_name "$APM_ENDPOINT"
   valid_apm_endpoint_name="$?"
   if [ ! "$valid_apm_endpoint_name" -eq "0" ] ; then
      echo "invalid apm service name"
      exit 1
   fi
fi

if [ ! -z "$APM_PORT" ] ; then
   validate_port_num "$APM_PORT"
   valid_APM_port="$?"
   if [ ! "$valid_APM_port" -eq "0" ] ; then
      echo "invalid APM port number"
      exit 1
   fi
fi

echo "Running APPO"
umask 0027
cd /usr/app || exit
java -jar appo-0.0.1-SNAPSHOT.jar
