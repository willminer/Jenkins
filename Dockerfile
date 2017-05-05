FROM jenkins:2.32.3

#docker run -d -p 8080:8080 -p 50000:50000 -p 2376:2377 --add-host cchsreg.com:192.168.99.101 -v ~/docker_vols/jenkins:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock --name jenkins custom_jenkins 

USER root

RUN apt-get update && apt-get install -y --no-install-recommends \
    apt-transport-https \
    ca-certificates  \
    curl \
    software-properties-common && \
    rm -rf /var/lib/apt/lists/*
	
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - && \
    add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable" && \
    curl -L https://github.com/docker/compose/releases/download/1.11.2/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose 

RUN apt-get update && apt-get install -y --no-install-recommends \
    docker-ce && \
    rm -rf /var/lib/apt/lists/*

COPY plugins.txt /usr/share/jenkins/ref/

RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

ENTRYPOINT ["/bin/tini", "--", "/usr/local/bin/jenkins.sh"]
