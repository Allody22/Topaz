# Этап 1: Сборка приложения
FROM node:16-alpine as build
WORKDIR /app
COPY site/package*.json ./
RUN npm install
COPY site/ ./
RUN npm run build

# Этап 2: Готовый образ для продакшена
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
