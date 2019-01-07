FROM alpine:3.7

WORKDIR app/

RUN apk add --no-cache \
  bash \
  openjdk8 \
  unzip \
  wget

ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/bin/

ARG JANUS_VERSION=0.2.0
ARG JANUS_ARTIFACT=janusgraph-${JANUS_VERSION}-hadoop2

RUN wget -q --show-progress --progress=bar:force:noscroll \
  http://github.com/JanusGraph/janusgraph/releases/download/v${JANUS_VERSION}/${JANUS_ARTIFACT}.zip && \
  unzip ${JANUS_ARTIFACT}.zip && \
  rm ${JANUS_ARTIFACT}.zip && \
  mv ${JANUS_ARTIFACT} janusgraph

# WARN: Instruction above takes long time. Make best effort to insert additional commands below this comment

CMD ["./entrypoint.sh"]

COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

ARG USER=janusz
ARG GROUP=aai

RUN addgroup ${GROUP} && adduser -D ${USER} ${GROUP} && chown -R ${USER}:${GROUP} .

USER ${USER}:${GROUP}

HEALTHCHECK --interval=40s --timeout=10s --retries=3 CMD janusgraph/bin/janusgraph.sh status