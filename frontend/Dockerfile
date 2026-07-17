# Stage 1: Build the Angular application
FROM node:22-alpine AS builder

WORKDIR /build

# Copy package files first for layer caching
COPY frontend/package.json ./
RUN npm install

# Copy source and build
COPY frontend/ .
RUN npm run build -- --configuration production

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy custom nginx configuration
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Copy built Angular dist files
COPY --from=builder /build/dist/browser/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]