FROM node:13-alpine

COPY . /usr/app

WORKDIR /usr/app

RUN npm install

CMD ["node", "app/server.js"]
