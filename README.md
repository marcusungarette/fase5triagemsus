# 🏥 Triage AI SUS

Sistema inteligente de triagem médica para o SUS utilizando Inteligência Artificial com Google Gemini 1.5 Pro.

## 📋 Sobre o Projeto

O **Triage AI SUS** automatiza o processo de triagem médica seguindo o Protocolo de Manchester, classificando pacientes por nível de urgência em menos de 2 minutos através de análise inteligente de sintomas.

### 🎯 Funcionalidades

- ✅ **Cadastro de Pacientes** com validação de dados
- 🔍 **Triagem Automática** com IA do Google Gemini
- ⚡ **Processamento Assíncrono** para alta performance
- 📊 **Classificação por Prioridade** seguindo Protocolo de Manchester
- 🚨 **Sistema de Filas Inteligentes** com retry automático
- 📱 **API REST** completa com documentação Swagger
- 🔄 **Monitoramento** em tempo real do status das triagens

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem principal
- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Data Redis** - Cache e sistema de filas
- **Spring Integration** - Processamento assíncrono
- **Spring Boot Actuator** - Monitoramento e métricas
- **Lombok** - Redução de boilerplate
- **Jackson** - Serialização JSON
- **SpringDoc OpenAPI 3** - Documentação da API

### Banco de Dados
- **PostgreSQL 14** - Banco de dados principal
- **Redis 7** - Cache e sistema de filas

### IA e APIs Externas
- **Google Gemini 1.5 Pro** - Análise inteligente de sintomas
- **Spring WebFlux** - Cliente HTTP reativo

### Infraestrutura
- **Docker** - Containerização
- **Docker Compose** - Orquestração de serviços
- **Maven** - Gerenciamento de dependências

## 🚀 Como Executar

### Pré-requisitos

- **Java 17+**
- **Maven 3.8+**
- **Docker** e **Docker Compose**
- **Chave da API Google Gemini** (obrigatória)

### 🔑 Configuração da API Key

1. Obtenha sua chave da API no [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Configure a variável de ambiente:

```bash
# Windows
set GEMINI_API_KEY=sua_chave_aqui

# Linux/Mac
export GEMINI_API_KEY=sua_chave_aqui
```

### 🐳 Execução com Docker (Recomendado)

#### Subir a aplicação
```bash
# Clone o repositório
git clone <url-do-repositorio>
cd fase5triagemsus

# Compile o projeto
mvn clean package -DskipTests

# Suba todos os serviços
docker-compose up -d

# Ou para ver os logs em tempo real
docker-compose up
```

#### Verificar status dos serviços
```bash
# Ver status dos containers
docker-compose ps

# Ver logs da aplicação
docker-compose logs app

# Ver logs do Redis
docker-compose logs redis

# Ver logs do PostgreSQL
docker-compose logs postgres
```

#### Parar a aplicação
```bash
# Parar todos os serviços
docker-compose down

# Parar e remover volumes (dados serão perdidos)
docker-compose down --volumes

# Parar e rebuildar na próxima execução
docker-compose down --rmi local
```

### 💻 Execução Local (Desenvolvimento)

#### 1. Subir dependências (PostgreSQL e Redis)
```bash
# Apenas os bancos de dados
docker-compose up postgres redis -d
```

#### 2. Executar aplicação com Maven
```bash
# Compilar e executar
mvn spring-boot:run

# Ou compilar separadamente
mvn clean package -DskipTests
java -jar target/fase5triagemsus-0.0.1-SNAPSHOT.jar
```

#### 3. Perfil de desenvolvimento
```bash
# Executar com perfil local (usa localhost para bancos)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Com debug habilitado
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 📖 Acessos e URLs

Após subir a aplicação, os seguintes serviços estarão disponíveis:

- **API Base**: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/api/v1/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api/v1/api-docs`
- **Health Check**: `http://localhost:8080/api/v1/actuator/health`
- **Métricas**: `http://localhost:8080/api/v1/actuator/metrics`

### Bancos de Dados
- **PostgreSQL**: `localhost:5440` (usuário: `postgres`, senha: `postgres`)
- **Redis**: `localhost:6379` (senha: `redis123`)

## 🧪 Testando a API

### 1. Criar um Paciente
```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "cpf": "123.456.789-01",
    "birthDate": "1985-06-15",
    "gender": "M",
    "phone": "(11) 99999-9999",
    "email": "joao@email.com"
  }'
```

### 2. Criar uma Triagem
```bash
curl -X POST http://localhost:8080/api/v1/triages \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "ID_DO_PACIENTE_CRIADO",
    "symptoms": [
      {
        "description": "Dor no peito intensa",
        "intensity": 9,
        "location": "Tórax"
      },
      {
        "description": "Dificuldade para respirar",
        "intensity": 8
      }
    ]
  }'
```

### 3. Consultar Status da Triagem
```bash
curl http://localhost:8080/api/v1/triages/{ID_DA_TRIAGEM}/status
```

### 4. Ver Status da Fila
```bash
curl http://localhost:8080/api/v1/triages/queue/status
```

## 🔧 Comandos Úteis de Desenvolvimento

### Maven
```bash
# Limpar e compilar
mvn clean compile

# Executar testes
mvn test

# Gerar JAR
mvn clean package

# Pular testes na compilação
mvn clean package -DskipTests

# Executar testes específicos
mvn test -Dtest=CreatePatientUseCaseTest

# Ver dependências
mvn dependency:tree
```

### Docker
```bash
# Rebuild apenas a aplicação
docker-compose build app

# Rebuild sem cache
docker-compose build --no-cache

# Ver logs de um serviço específico
docker-compose logs -f app

# Executar comando dentro do container
docker-compose exec app bash

# Limpar containers parados
docker system prune

# Ver uso de recursos
docker stats
```

### Redis (Debugging)
```bash
# Conectar no Redis
docker exec -it triage-ai-redis redis-cli -a redis123

# Comandos úteis no Redis
KEYS *                    # Ver todas as chaves
LLEN triage:queue        # Tamanho da fila
LRANGE triage:queue 0 -1 # Ver mensagens da fila
FLUSHALL                 # Limpar tudo (cuidado!)
```

### PostgreSQL (Debugging)
```bash
# Conectar no PostgreSQL
docker exec -it triage-ai-postgres psql -U postgres

# Comandos úteis no PostgreSQL
\dt                      # Listar tabelas
SELECT * FROM triages;   # Ver triagens
SELECT * FROM patients;  # Ver pacientes
\q                       # Sair
```

## 📝 Configurações do Ambiente

### Profiles Disponíveis
- **default**: Configuração para desenvolvimento local
- **docker**: Configuração para execução em containers
- **test**: Configuração para testes

### Variáveis de Ambiente Importantes
```bash
SPRING_PROFILES_ACTIVE=docker
GEMINI_API_KEY=sua_chave_aqui
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres
SPRING_REDIS_HOST=redis
```

## 🚨 Solução de Problemas

### Erro de conexão com Redis
```bash
# Verificar se Redis está rodando
docker-compose ps redis

# Reiniciar Redis
docker-compose restart redis
```

### Erro de conexão com PostgreSQL
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps postgres

# Ver logs do PostgreSQL
docker-compose logs postgres
```

### Aplicação não inicia
```bash
# Verificar se a API Key do Gemini está configurada
echo $GEMINI_API_KEY

# Verificar logs da aplicação
docker-compose logs app
```

### Limpar ambiente completamente
```bash
# Parar tudo e limpar volumes
docker-compose down --volumes --rmi local

# Limpar containers órfãos
docker system prune -f

# Rebuildar do zero
docker-compose build --no-cache
docker-compose up
```

## 📚 Documentação Adicional

- **Swagger/OpenAPI**: Acesse `/swagger-ui.html` para documentação interativa
- **Actuator Endpoints**: Acesse `/actuator` para métricas e health checks
- **Logs**: Configurados em `logs/triage-ai.log`

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

**Desenvolvido com ❤️ para revolucionar a triagem médica no SUS**