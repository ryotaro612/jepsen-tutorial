FROM ubuntu:25.04
RUN apt update && \
    apt upgrade -y && \
    apt install -y openssh-server
COPY entrypoint.sh entrypoint.sh
