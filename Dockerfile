# 使用 OpenJDK 11 為基礎鏡像
FROM openjdk:11

# 設置一個工作目錄來存儲應用程序
WORKDIR /app

# 將 target 目錄下的 *.jar 文件複製到工作目錄（假設你的 Spring Boot 專案名為 my-app）
COPY target/rock-0.0.1-*.jar app.jar

# 開放應用所需的端口，這裡假設是 8080
EXPOSE 8090

# 設定容器啟動後執行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]