# 🧠 Sentimind API - Análisis de Sentimiento

Sentimind es una API REST profesional desarrollada con **Spring Boot** diseñada para procesar reseñas de usuarios y clasificar su sentimiento mediante una arquitectura robusta y escalable, preparada para integración con IA.

---

## 🛠️ Tecnologías y Versiones

### 🤖 Equipo Data Science
* **Modelo:** 
* **Dataset:** 

### 💻 Stack Tecnológico
* **Java:** 17 (LTS)
* **Spring Boot:** 3.5.9
* **Gestor de Dependencias:** Maven
* **Base de Datos:** PostgreSQL 15 (Producción) / H2 (Pruebas)
* **Contenedores:** Docker & Docker Compose
* **Seguridad:** Spring Security (En proceso)
* **Documentación:** Swagger UI (OpenAPI 3.0)

---

## 🚀 Ejecución con Docker (Flujo Completo)

Para levantar la API junto con la base de datos PostgreSQL de forma automática, sigue estos pasos:

1. **Clonar y navegar al proyecto:**
   ```bash
   git clone [https://github.com/Daimond92/sentimind-api.git](https://github.com/Daimond92/sentimind-api.git)
   cd sentimind-api
   ```
2. **Lanzar contenedores:** Asegúrate de tener Docker Desktop iniciado y ejecuta:
   ```bash
   docker compose up --build
   ```
3. La API estará operativa en: `http://localhost:8080`.

## 📡 Endpoints Principales
`Base URL: /api/v1/sentiment`

### 📤 Analizar Sentimiento
#### POST `/`
* **Request Body (JSON):**

``` bash
{
  "text": "La comida estuvo excelente y el servicio fue muy rápido."
}
```

* **Response Body (JSON):**
``` bash
{
  "id": 1,
  "sentiment": "Positivo",
  "confidence": 0.95,
  "timestamp": "2025-12-23T22:35:10"
}
```

## ⚙️ Arquitectura y Lógica de Predicción
El sistema utiliza un diseño híbrido controlado por la propiedad `ai.integration.enabled`:

1. **Normalización:** El texto se procesa en minúsculas para una detección precisa.

2. **Lógica Lexicon (Mock AI):** 
* **Positivo:** Detecta términos como excelente, bueno, maravilloso.
* **Negativo:** Detecta términos como malo, terrible, horrible.
* **Neutral:** Asignado automáticamente si no hay coincidencias clave.
3. **Persistencia:** Mapeo mediante `SentimentMapper` y guardado en **PostgreSQL**.
4. **Auditoría:** Uso de `@EnableJpaAuditing` para gestionar el campo `created_at` sin intervención manual.

## 🛡️ Seguridad e Integridad
* **Spring Security:** Endpoints protegidos para evitar accesos no autorizados.
* **Java Records:** DTOs inmutables para una transferencia de datos segura.
* **Validaciones:** Control estricto de entrada de datos mediante anotaciones de Jakarta Bean Validation.

## 🧪 Pruebas de Demo (Cadenas Largas)

| Sentimiento | Palabra Clave Sugerida | Resultado Esperado |
| :--- | :--- | :--- |
| **Positivo** | "maravilloso", "excelente", "bueno" | `Sentiment: Positivo (95%)` |
| **Neutral** | (Sin palabras clave específicas) | `Sentiment: Neutral (95%)` |
| **Negativo** | "terrible", "horrible", "malo" | `Sentiment: Negativo (95%)` |

## 📊 Monitoreo e Inspección
* **Swagger UI:** Pruébalo en vivo en http://localhost:8080/swagger-ui.html
* **Acceso a DB (Docker):**
``` bash
docker exec -it sentimind-db psql -U user_admin -d sentimind_db
```