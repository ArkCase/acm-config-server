#!/bin/sh

set -eu

repodir=/app/data
search_locations="file:$repodir,file:$repodir/labels,file:$repodir/ldap,file:$repodir/lookups,file:$repodir/rules"

cat > application.yml << EOF
server.port: "9999"
spring.profiles.active: native
spring.cloud.config.server.native.searchLocations: "$search_locations"
spring.jms.pub-sub-domain: "true"
properties.folder.path: "$repodir"
branding.files.folder.path: "$repodir/branding"
logging.file: /dev/stdout
logging.level.com.armedia.acm.configserver: debug
logging.level.org.springframework.cloud.config: debug
jms.message.buffer.window: "1"
spring.activemq.broker-url: "$ACTIVEMQ_URL"
acm.activemq.broker-url: "$ACTIVEMQ_URL"
acm.activemq.default-destination: configuration.changed
acm.activemq.labels-destination: labels.changed
acm.activemq.ldap-destination: ldap.changed
acm.activemq.lookups-destination: lookups.changed
acm.activemq.rules-destination: rules.changed
acm.activemq.timeout: "10"
arkcase.languages: "-de,-en,-en-in,-es,-fr,-hi,-ja,-pt,-ru,-zh-cn,-zh-tw"
logging.pattern.file: "%d{yyyy-MM-dd HH:mm:ss,SSS} [%thread] %-5level %logger.%M - %msg%n"
java.io.tmpdir: /app/tmp
EOF

# Run it!
exec java -jar /app/config-server.jar --spring.config.location=file:application.yml
