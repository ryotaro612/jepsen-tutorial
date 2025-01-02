FROM debian:12-slim
RUN apt update && \
    apt upgrade -y && \
    apt install -y openssh-server && \
    ssh-keygen -A && \
    mkdir -p /run/sshd

