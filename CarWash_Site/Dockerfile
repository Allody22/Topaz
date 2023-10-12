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
COPY nginx-custom.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]