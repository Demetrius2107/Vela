# =============================================================================
# Vela IM — Web 前端 Dockerfile
# 多阶段构建：npm build → nginx 运行
# =============================================================================

# ---- Build stage ----
FROM node:20-alpine AS build

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Run stage ----
FROM nginx:alpine

COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
