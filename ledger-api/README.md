# Ledger API 后端

记账应用后端 API，基于 Spring Boot 3 + JPA，支持部署到服务器。

## 技术栈

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- MySQL 8 / H2（开发）
- Maven

## API 接口

| 方法 | 路径 | 说明 |
|-----|------|-----|
| POST | /api/records | 新增记账 |
| GET | /api/records?start=&end=&categoryId= | 按时间范围查询账目列表 |
| GET | /api/categories | 分类列表 |
| POST | /api/categories | 新增分类 |
| PUT | /api/categories/{id} | 更新分类 |
| DELETE | /api/categories/{id} | 删除分类 |
| PATCH | /api/categories/{id}/sort | 更新排序 |
| GET | /api/budget | 获取预算 |
| PUT | /api/budget/yearly | 更新年预算 |
| GET | /api/stats/today | 今日支出 |
| GET | /api/stats/month | 本月支出 |
| GET | /api/stats/year | 今年支出 |
| GET | /api/stats/summary | 汇总（今日/月/年） |
| GET | /api/health | 健康检查 |

### 请求示例

**新增记账**
```json
POST /api/records
{
  "amount": 88.50,
  "categoryId": "c1",
  "date": "2025-01-15",
  "note": "午餐"
}
```

**查询账目**
```
GET /api/records?start=2025-01-01&end=2025-01-31&categoryId=c1
```

**多用户**：请求头 `X-User-Id: your-device-id` 区分不同用户/设备，不传则使用 `default`。

## 本地开发

```bash
cd ledger-api
mvn spring-boot:run
```

默认使用 H2 内存数据库，启动后访问：http://localhost:8080/api/health

## 服务器部署

### 方式一：Docker Compose（推荐）

```bash
cd ledger-api
docker compose up -d
```

API 运行在 http://服务器IP:8080，MySQL 运行在 3306 端口。

### 方式二：手动部署

1. 准备 MySQL 8+ 数据库，创建库：
   ```sql
   CREATE DATABASE ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'ledger'@'%' IDENTIFIED BY 'your-password';
   GRANT ALL ON ledger.* TO 'ledger'@'%';
   ```

2. 编译打包：
   ```bash
   mvn clean package -DskipTests
   ```

3. 运行：
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export MYSQL_HOST=localhost
   export MYSQL_USER=ledger
   export MYSQL_PASSWORD=your-password
   export MYSQL_DATABASE=ledger
   java -jar target/ledger-api-1.0.0.jar
   ```

### 方式三：仅 Docker 运行 API（使用外部 MySQL）

```bash
docker build -t ledger-api .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_HOST=your-mysql-host \
  -e MYSQL_USER=ledger \
  -e MYSQL_PASSWORD=your-password \
  -e MYSQL_DATABASE=ledger \
  ledger-api
```

## 环境变量

| 变量 | 说明 | 默认值 |
|-----|------|--------|
| SPRING_PROFILES_ACTIVE | dev/prod | dev |
| SERVER_PORT | 端口 | 8080 |
| MYSQL_HOST | MySQL 主机 | localhost |
| MYSQL_PORT | MySQL 端口 | 3306 |
| MYSQL_DATABASE | 数据库名 | ledger |
| MYSQL_USER | 用户名 | root |
| MYSQL_PASSWORD | 密码 | - |
