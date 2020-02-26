FROM nginx:1.17.8-alpine

COPY proxy.conf /etc/nginx/proxy.conf.template
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
