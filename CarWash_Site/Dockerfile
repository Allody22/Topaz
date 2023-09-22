# Базовый образ
FROM node:14

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем package.json и package-lock.json из папки site
COPY site/package*.json ./

# Устанавливаем зависимости
RUN npm install

# Копируем исходный код проекта (из папки site) в контейнер
COPY site/ .

# Указываем порт, который будет слушать приложение
EXPOSE 3000

# Запускаем приложение
CMD ["npm", "start"]
