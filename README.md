# 🧠 Sentimind API - Análisis de Sentimiento

Sentimind es una API REST profesional desarrollada con **Spring Boot** diseñada para procesar reseñas de usuarios y clasificar su sentimiento mediante una arquitectura robusta y escalable, preparada para integración con IA.

---

## 🚀 Demo en la Nube (Oracle Cloud Infrastructure)

Demo público disponible durante el hackathon:
http://139.177.101.190:8080

Swagger UI:
http://139.177.101.190:8080/swagger-ui.html

Nota: El servicio estará disponible únicamente durante el periodo del hackathon.
Para ejecución local, utiliza el flujo con Docker descrito más abajo.

---

## 🛠️ Tecnologías y Versiones

### 🤖 Equipo Data Science
| Componente | Detalle |
|------------|---------|
| **Modelo** | **TF-IDF (N-gramas 1-2) + Logistic Regression**<br>✅ **Accuracy: 67.1% **<br>✅ **Recall Negativos: 83.5%**|
| **Dataset** | **Amazon Reviews ES**<br>✅ **400 muestras por clase (Total Test: 1205)**<br>✅ **Dataset 100% Balanceado (Negativo/Neutro/Positivo) |

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
2. **Lanzar contenedores:** Asegúrate de tener Docker Desktop iniciado y ejecuta en una terminal en la carpeta raíz `sentimind-api` donde se encuentra el archivo `docker-compose.yml`:
   * Primero ejecuta:
   ```bash
   cd sentimind-api
   ```

   * Segundo ejecuta:
   ```bash
   ./mvnw clean package -DskipTests
   ```

   * Tercero ejecuta:
   ```bash
   cd ..
   ```

   * Cuarto ejecuta:
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

## ⚙️ Inteligencia Artificial y Lógica del Modelo
A diferencia de sistemas basados en reglas fijas, SentiMind utiliza un pipeline de Procesamiento de Lenguaje Natural (NLP) real:

1. **Vectorización Semántica:** Utilizamos TF-IDF con un rango de n-gramas de (1, 2). Esto permite que el modelo entienda no solo palabras sueltas, sino también conceptos compuestos (ej: "no bueno").
2. **Búsqueda de Palabras Clave:** El algoritmo rastrea el texto buscando términos positivos predefinidos (excelente, bueno, increíble, feliz).
3. **Cálculo de Confianza:** 
* Si detecta palabras positivas, asigna el sentimiento **"Positivo"** con una confianza del **95%**.
* Precisión General: El sistema clasifica los sentimientos con una confianza promedio (Accuracy) del 67.1%, asegurando un equilibrio entre las categorías Positivo, Neutro y Negativo sin sobreajuste (overfitting).
* El modelo ha sido optimizado para priorizar la sensibilidad ante quejas, logrando identificar comentarios "Negativos" con una tasa de acierto (Recall) del 83.5%.
* **Persistencia:** El resultado se mapea a una Entidad JPA y se guarda automáticamente en la base de datos H2 con su respectiva marca de tiempo.

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

* **Pruebas Unitarias:** Verificación de la lógica del `SentimentService` usando JUnit 5.
* **Pruebas de Integración:** Validación de los endpoints mediante `MockMvc`.
* **Datos Iniciales:** Carga automática de registros en `import.sql` para demostración inmediata.

##📦 Artefactos de Data Science
Los recursos del modelo se encuentran en la carpeta /models:

sentiment_pipeline_ternario_v2.pkl: Pipeline listo para producción.

notebooks/EDA_and_Training.ipynb: Documentación del proceso de entrenamiento y limpieza de datos.
## 📊 Monitoreo e Inspección
* **Swagger UI:** Pruébalo en vivo en http://localhost:8080/swagger-ui.html
* **Acceso a DB (Docker):**
``` bash
docker exec -it sentimind-db psql -U user_admin -d sentimind_db
```
