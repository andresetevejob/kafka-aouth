FROM quay.io/strimzi/kafka:0.29.0-kafka-3.1.0

COPY libs/* /opt/kafka/libs/
COPY config/* /opt/kafka/config/
COPY start.sh /opt/kafka/
COPY simple_kafka_config.sh /opt/kafka/

USER root
RUN chmod +x /opt/kafka/start.sh /opt/kafka/simple_kafka_config.sh
USER kafka

CMD ["/bin/bash", "/opt/kafka/start.sh"]