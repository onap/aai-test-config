FROM haproxy:1.6-alpine

WORKDIR app/

COPY haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg

COPY aai.pem /etc/ssl/private/aai.pem

RUN chmod 640 /etc/ssl/private/aai.pem && chown root:root /etc/ssl/private/aai.pem