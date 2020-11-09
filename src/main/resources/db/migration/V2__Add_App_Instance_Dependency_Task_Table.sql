create table appinstancedependency (
        id varchar(64) not null,
        tenant varchar(64)  not null,
        app_instance_id varchar(64) not null,
        dependency_app_instance_id varchar(64) not null,
        primary key (id)
    );