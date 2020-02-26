FROM node:12.14.1 as BUILD-STAGE
LABEL maintainer="Osipiuk Bartosz <osipiuk.bartosz@gmail.com>"
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npm install -g envsub
COPY . .
RUN chmod +x /app/docker-entrypoint.sh
ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["npm", "run", "prod"]

#
#FROM nginx:1.13.12-alpine as PRODUCTION-STAGE
#LABEL maintainer="Osipiuk Bartosz <osipiuk.bartosz@gmail.com>"
#COPY --from=BUILD-STAGE /app/dist /usr/share/nginx/html
#EXPOSE 80
#CMD ["nginx", "-g", "daemon off;"]
