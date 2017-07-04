FROM debian:sid
LABEL mantainer mikel.asla@keensoft.es

ENV DEBIAN_FRONTEND noninteractive
ENV HOME /usr/src/sdk
ENV HOTSWAP_AGENT_VERSION 1.1.0-SNAPSHOT
ENV HOTSWAP_AGENT hotswap-agent-$HOTSWAP_AGENT_VERSION.jar
ENV HOTSWAP_AGENT_URL https://github.com/HotswapProjects/HotswapAgent/releases/download/$HOTSWAP_AGENT_VERSION/$HOTSWAP_AGENT
ENV MAVEN_OPTS "$MAVEN_OPTS -Xms256m -Xmx2G -dcevm -javaagent:/opt/hotswap-agent.jar \
	-agentlib:jdwp=transport=dt_socket,address=9999,server=y,suspend=n"

RUN set -x \
	&& apt-get update \
	&& apt install -y \
		maven \
		openjdk-8-jdk-headless \
		openjdk-8-jre-dcevm \
 		wget \
	&& rm -rf /var/lib/apt/lists/*
ENV USER mikel
RUN set -x \
	&& wget $HOTSWAP_AGENT_URL -O /opt/hotswap-agent.jar \
	&& mkdir -p $HOME \
	&& useradd -ms /bin/false $USER \
	&& chown -R $USER:$USER $HOME
ADD . $HOME
WORKDIR $HOME
USER $USER
EXPOSE 8080 9999
CMD ["mvn", "clean", "install", "alfresco:run"]


