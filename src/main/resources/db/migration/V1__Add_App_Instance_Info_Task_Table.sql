
    create table appinstanceinfo (
       app_instance_id  varchar(64) not null,
        app_package_id varchar(64) not null,
        app_id varchar(64) not null,
        tenant varchar(64) not null,
        app_name varchar(128) not null,
        app_descriptor varchar(256) not null,
        mec_host varchar(15) not null,
        applcm_host varchar(15),
        operational_status varchar(128) not null,
        operation_info varchar(256),
        create_time timestamp default current_timestamp,
        update_time timestamp default current_timestamp,
        primary key (app_instance_id)
    );

    create table appruletask (
       app_rule_task_id  varchar(64) not null,
        tenant varchar(64) not null,
        task_id varchar(64),
        app_instance_id varchar(64),
        app_rules varchar(2097154),
        detail varchar(256),
        config_result varchar(256),
        create_time timestamp default current_timestamp,
        update_time timestamp default current_timestamp,
        primary key (app_rule_task_id)
    );

    create table appotenant (
        tenant  varchar(255) not null,
        primary key (tenant)
    );