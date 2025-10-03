# Sistema de Pedidos Desktop

## 🛠️ Tecnologias Utilizadas

### Backend (`pedidos-backend`)
- **Java 17**
- **Spring Boot 3.x**
    - Spring Web (APIs REST)
    - Spring AMQP (Integração com RabbitMQ)
    - Spring Validation
- **Jackson** (Serialização/Desserialização JSON)
- **Lombok**
- **Maven** (Gerenciador de Dependências)
- **SLF4J / Logback** (Logging)

### GUI (`pedidos-gui`)
- **Java 17**
- **Java Swing** (Interface Gráfica)
- **Java 11+ HttpClient** (Comunicação HTTP)
- **Jackson** (Manipulação de JSON)
- **Maven**

### Infraestrutura & Mensageria
- **RabbitMQ** (Message Broker)
- **Docker** (Para execução da infraestrutura)

## ✅ Pré-requisitos

Antes de começar, garanta que você tenha as seguintes ferramentas instaladas em sua máquina:
- **JDK 17** ou superior
- **Apache Maven 3.8+**
- **Docker** e **Docker Engine** (em execução)
- **Git**

## 🚀 Como Executar o Projeto

Siga os passos abaixo para colocar toda a aplicação em funcionamento.

### 1. Clonar o Repositório

git clone [https://github.com/EmersonFontes/sistema-pedidos.git](https://github.com/EmersonFontes/sistema-pedidos.git)

cd sistema-pedidos

### 2. Iniciar a Infraestrutura (RabbitMQ)

docker run -d --hostname rabbit-local --name rabbitmq-pedidos -p 5672:5672 -p 15672:15672 rabbitmq:3-management

Credenciais padrão: guest / guest

### 3. Executar o Backend

Abra um novo terminal na pasta do projeto e execute os seguintes comandos:

#### Navegue até a pasta do backend
cd pedidos-backend

#### Execute a aplicação Spring Boot
mvn spring-boot:run

O serviço estará rodando em http://localhost:8080

### 4. Executar a Aplicação Desktop (GUI)

#### Navegue até a pasta da GUI
cd pedidos-gui

#### Compile e execute a aplicação Swing
mvn clean install exec:java -Dexec.mainClass="com.front.pedidos.MainFramePolling"