# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM swr.cn-north-4.myhuaweicloud.com/eg-common/openjdk:8u201-jdk-alpine

# Define all environment variable here
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
# # CREATE APP USER ##
# Set umask
RUN sed -i "s|umask 022|umask 027|g" /etc/profile

# Create the home directory for the new app user.
RUN mkdir -p /usr/app
RUN mkdir -p /usr/app/bin

# Set the home directory to our app user's home.
ENV APP_HOME=/usr/app
ENV UID=166
ENV GID=166
ENV USER_NAME=eguser
ENV GROUP_NAME=eggroup
ENV ENV="/etc/profile"

# Create an app user so our program doesn't run as root.
RUN apk update &&\
    apk add shadow &&\
    groupadd -r -g $GID $GROUP_NAME &&\
    useradd -r -u $UID -g $GID -d $APP_HOME -s /sbin/nologin -c "Docker image user" $USER_NAME

# Set the working directory.
WORKDIR $APP_HOME

RUN chmod -R 750 $APP_HOME &&\
    chmod -R 550 $APP_HOME/bin &&\
    mkdir -p -m 750 $APP_HOME/config &&\
    mkdir -p -m 750 $APP_HOME/log &&\
    mkdir -p -m 700 $APP_HOME/ssl &&\
    mkdir -p -m 700 $APP_HOME/.postgresql &&\
    mkdir -p -m 750 $APP_HOME/packages &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME

# Copy the application & scripts
COPY --chown=$USER_NAME:$GROUP_NAME target/appo-0.0.1-SNAPSHOT.jar $APP_HOME/bin
COPY --chown=$USER_NAME:$GROUP_NAME configs/*.sh $APP_HOME/bin

# Exposed port
EXPOSE 8091

# Change to the app user.
USER $USER_NAME

# Execute script & application
ENTRYPOINT ["sh", "./bin/start.sh"]