package br.com.fiap.fase5triagemsus.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Triage AI SUS - API")
                        .description("""
                    API do sistema de triagem inteligente para o SUS utilizando IA.
                    
                    ## Funcionalidades
                    - **Pacientes**: Cadastro e consulta de pacientes
                    - **Triagens**: Criação de triagens com análise automática de IA
                    - **Priorização**: Classificação automática baseada no Protocolo de Manchester
                    - **Consultas**: Diversos filtros para consulta de triagens
                    
                    ## Tecnologias
                    - Java 17 + Spring Boot 3.2
                    - PostgreSQL para persistência
                    - Google Gemini 1.5 Pro para análise de IA
                    - Docker para conteinerização
                    - Redis para processamento assíncrono e mensageria
                    
                    ## Níveis de Prioridade
                    1. **EMERGENCY** (Vermelho) - Atendimento imediato
                    2. **VERY_URGENT** (Laranja) - 10 minutos
                    3. **URGENT** (Amarelo) - 60 minutos  
                    4. **LESS_URGENT** (Verde) - 120 minutos
                    5. **NON_URGENT** (Azul) - 240 minutos
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Triage AI SUS")
                                .email("contato@triageai.sus.br")
                                .url("https://github.com/sus/triage-ai"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                );
    }
}