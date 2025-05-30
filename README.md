# Transaction Management System

## 项目简介
这是一个基于 Spring Boot 3.2.3 和 JDK 21 开发的银行交易管理系统，提供交易处理、账户管理等核心功能。系统采用内存存储方式，支持 RESTful API 接口，并包含完整的单元测试和性能测试。

## 技术栈
- JDK 21
- Spring Boot 3.2.3
- Maven
- JUnit 5
- JMeter (性能测试)
- Docker

## 系统要求
- JDK 21 或更高版本
- Maven 3.8.x 或更高版本
- Docker (可选，用于容器化部署)

## 快速开始

### 本地运行
1. 克隆项目
```bash
git clone https://gitee.com/RandyZhang/transaction-management.git

or :
git clone https://github.com/Randdy/transaction-management.git

cd transaction-management
```

2. 编译项目
```bash
mvn clean package
```

3. 运行应用
```bash
mvn spring-boot:run
```

### Docker 运行
```bash
docker-compose up --build
```

## API 接口

### 交易管理
- 创建交易
  ```
  POST /api/transactions
  Content-Type: application/json
  
  {
    "fromAccount": "string",
    "toAccount": "string",
    "amount": "decimal",
    "type": "DEPOSIT|WITHDRAWAL|TRANSFER"
  }
  ```

- 获取所有交易
  ```
  GET /api/transactions
  ```

- 获取特定交易
  ```
  GET /api/transactions/{id}
  ```

- 删除交易
  ```
  DELETE /api/transactions/{id}
  ```


## 测试

### 单元测试
```bash
mvn test
```
####报告

### 生成 JaCoCo 报告：
```bash
mvn clean test jacoco:report
```
JaCoCo 测试覆盖率报告：
HTML 格式：target/site/jacoco/index.html
XML 格式：target/site/jacoco/jacoco.xml

###  生成 Surefire 报告：
```bash
mvn surefire-report:report
```
Surefire 测试报告：
target/surefire-reports/ 目录下

### 已生成报告例子：
/transaction-management/site_test_530/jacoco/index.html

### 性能测试  生成 JMeter 报告：
```bash
jmeter -n -t transaction-test-plan.jmx -l results.jtl -e -o report
```
或
```bash
mvn clean verify
```
## 文档路径
/transaction-management/target/jmeter/reports/stress_test/index.html
## 压力测试
/transaction-management/stress_test_530/index.html

## 配置说明
主要配置项在 `application.yml` 中：
```yaml
spring:
  application:
    name: hsbc-wpb-transaction-management

server:
  port: 8080

app:
  transaction:
    max-amount: 1000000
    min-amount: 1
```

## 接口文档：
http://localhost:8080/api/swagger-ui/index.html#/Transaction%20Management


## 部署
### Docker 部署
1. 构建镜像
```bash
docker build -t hsbc-wpb-transaction-service .
```

2. 运行容器
```bash
docker-compose up -d
```

### 传统部署
1. 构建 jar 包
```bash
mvn clean package
```

2. 运行 jar 包
```bash
java -jar target/transaction-management-1.0-SNAPSHOT.jar
```

## 监控
- 应用健康检查: http://localhost:8080/api/actuator/health
