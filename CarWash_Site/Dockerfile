#FROM node:14-alpine
#WORKDIR /app
#COPY site/package*.json ./
#RUN npm install
#COPY site/ .
#EXPOSE 3000
#CMD ["npm", "start"]


# Этап сборки
FROM node:14-alpine AS build-stage
WORKDIR /app
COPY site/package*.json ./
RUN npm install
COPY site/ ./
RUN npm run build

# Этап production
FROM nginx:stable-alpine
COPY --from=build-stage /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]


## Этап 1: Сборка приложения
#FROM node:16-alpine as build
#WORKDIR /app
#COPY site/package*.json ./
#RUN npm ci
#COPY site/ ./
#RUN npm run build
#
## Этап 2: Готовый образ для продакшена
#FROM nginx:alpine
#COPY --from=build /app/build /usr/share/nginx/html
#EXPOSE 80
#CMD ["nginx", "-g", "daemon off;"]