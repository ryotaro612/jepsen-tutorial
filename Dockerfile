# How to install etcd
# https://github.com/etcd-io/etcd/releases
# jepsen runs the shell commands after sudo.
FROM debian:12-slim
RUN apt update && \
    apt upgrade -y && \
    apt install -y openssh-server curl sudo && \
    ssh-keygen -A && \
    mkdir -p /run/sshd
COPY entrypoint.sh id_rsa.pub /
ENV ETCD_VER=v3.5.17
ENV ETCD_DIR=/root/etcd
RUN cat id_rsa.pub > /root/.ssh/authorized_keys && \
    rm id_rsa.pub && \
    rm -f /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz && \
    mkdir -p $ETCD_DIR && \
    curl -L https://github.com/etcd-io/etcd/releases/download/${ETCD_VER}/etcd-${ETCD_VER}-linux-amd64.tar.gz -o /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz && \
    tar xzvf /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz -C $ETCD_DIR --strip-components=1 && \
    rm -f /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz
ENTRYPOINT ["/entrypoint.sh"]
