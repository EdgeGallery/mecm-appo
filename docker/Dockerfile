# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM openjdk:8u201-jdk-alpine

# Define all environment variable here
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
# # CREATE APP USER ##
# Set umask
RUN sed -i "s|umask 022|umask 027|g" /etc/profile

# Create the home directory for the new app user.
RUN mkdir -p /usr/app

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

# Copy the application & scripts
ADD target/appo-0.0.1-SNAPSHOT.jar $APP_HOME/
ADD configs/*.sh $APP_HOME/

RUN chmod 750 $APP_HOME &&\
    chmod 550 $APP_HOME/*.sh &&\
    chmod 550 $APP_HOME/appo-0.0.1-SNAPSHOT.jar &&\
    chown -R $USER_NAME:$GROUP_NAME /var/log &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME

# Exposed port
EXPOSE 8282

# Change to the app user.
USER $USER_NAME

# Execute script & application
ENTRYPOINT ["sh", "start.sh"]