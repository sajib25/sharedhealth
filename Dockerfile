FROM centos:6.6

ENV JAVA_VERSION_MAJOR=8 \
    JAVA_VERSION_MINOR=171 \
    JAVA_VERSION_BUILD=11 \
    JAVA_URL_HASH=512cd62ec5174c3487ac17c61aaa89e8

RUN yum install -y wget && \
    wget -q --no-cookies --no-check-certificate \
      --header 'Cookie:oraclelicense=accept-securebackup-cookie' \
      "http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/${JAVA_URL_HASH}/jre-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.rpm" && \
    yum install -y jre-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.rpm && rm -f jre-*.rpm &&  yum clean all

COPY healthId-api/build/distributions/healthId-*.noarch.rpm /tmp/healthId.rpm
RUN yum install -y /tmp/healthId.rpm && rm -f /tmp/healthId.rpm && yum clean all
COPY env/docker_healthid /etc/default/healthid
ENTRYPOINT . /etc/default/healthid && java -Dserver.port=$HEALTH_ID_SERVICE_PORT -DHID_LOG_LEVEL=$HID_LOG_LEVEL -jar  /opt/healthid/lib/healthId-api.war

