FROM ubuntu:24.04
RUN apt-get update && apt-get install -y openjdk-21-jdk maven rpm && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN mvn clean package rpm:rpm
RUN mkdir -p /output && cp target/rpm/analyzerfiles/RPMS/x86_64/*.rpm /output/
CMD ["/bin/bash"]