# 🧠 Sentimind API - Análisis de Sentimiento

Sentimind es una API REST desarrollada con **Spring Boot** diseñada para procesar reseñas de usuarios y clasificar su sentimiento mediante un modelo de lógica computacional (**Mock AI** en Fase 1).

---

## 🛠️ Tecnologías y Versiones

### 🤖 Equipo Data Science
| Componente | Detalle |
|------------|---------|
| **Modelo** | **TF-IDF (N-gramas 1-2) + Logistic Regression**<br>✅ **Accuracy: 67.1% **<br>✅ **Recall Negativos: 83.5%**|
| **Dataset** | **Amazon Reviews ES**<br>✅ **400 muestras por clase (Total Test: 1205)**<br>✅ **Dataset 100% Balanceado (Negativo/Neutro/Positivo) |

### 💻 Equipo Backend
* **Java:** 17 o 21 (LTS)
* **Spring Boot:** 3.x.x
* **Maven:** 3.8+
* **Base de Datos:** H2 (In-memory) / Soporte para PostgreSQL.
* **Documentación:** Swagger UI (OpenAPI 3.0).

---

## 🚀 Cómo ejecutar el proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Daimond92/sentimind-api.git
   cd sentimind-api
   ```
2. Abrir en VS Code:
* Asegúrate de tener instalado el Extension Pack for Java.
* Abre la carpeta raíz `sentimind-api`.

3. Ejecutar la aplicación:
* Localiza el archivo: `src/main/java/com/sentimind/sentimind_api/SentimindApiApplication.java`.
* Haz clic en el botón **"Run"** sobre el método `main`.
* La API estará lista cuando veas en consola: `Started SentimindApiApplication on port 8080`.

## 📡 Ejemplo de Petición y Respuesta
El endpoint principal permite enviar un texto para ser analizado y guardado en la base de datos.
`Endpoint: POST /api/v1/sentiment`

### Ejemplo de Petición (Request JSON):
``` bash
{
  "text": "La comida estuvo excelente y el servicio fue muy rápido."
}
```
### Ejemplo de Respuesta (Response JSON):
``` bash
{
  "id": 1,
  "sentiment": "Positivo",
  "confidence": 0.95,
  "timestamp": "2025-12-23T22:35:10"
}
```

## 🧪 Cómo probar el endpoint

### Opción A: Postman / Insomnia
1. Crea una nueva petición tipo POST.
2. URL: `http://localhost:8080/api/v1/sentiment`
3. En la pestaña **Body**, selecciona **raw** y formato **JSON**.
4. Pega el ejemplo de petición arriba mencionado y dale a **Send**.

### Opción B: Swagger UI (Interfaz Visual)
* Una vez encendida la API, entra desde tu navegador a: 👉 [swagger](http://localhost:8080/swagger-ui.html) 
* Desde allí puedes interactuar con los endpoints de forma visual.

## ⚙️ Inteligencia Artificial y Lógica del Modelo
A diferencia de sistemas basados en reglas fijas, SentiMind utiliza un pipeline de Procesamiento de Lenguaje Natural (NLP) real:

1. **Vectorización Semántica:** Utilizamos TF-IDF con un rango de n-gramas de (1, 2). Esto permite que el modelo entienda no solo palabras sueltas, sino también conceptos compuestos (ej: "no bueno").
2. **Búsqueda de Palabras Clave:** El algoritmo rastrea el texto buscando términos positivos predefinidos (excelente, bueno, increíble, feliz).
3. **Cálculo de Confianza:** 
* Si detecta palabras positivas, asigna el sentimiento **"Positivo"** con una confianza del **95%**.
* Precisión General: El sistema clasifica los sentimientos con una confianza promedio (Accuracy) del 67.1%, asegurando un equilibrio entre las categorías Positivo, Neutro y Negativo sin sobreajuste (overfitting).
* El modelo ha sido optimizado para priorizar la sensibilidad ante quejas, logrando identificar comentarios "Negativos" con una tasa de acierto (Recall) del 83.5%.
* **Persistencia:** El resultado se mapea a una Entidad JPA y se guarda automáticamente en la base de datos H2 con su respectiva marca de tiempo.

## 📊 Calidad y Pruebas
Para asegurar la fiabilidad de la lógica de análisis, contamos con una suite de pruebas:

* **Pruebas Unitarias:** Verificación de la lógica del `SentimentService` usando JUnit 5.
* **Pruebas de Integración:** Validación de los endpoints mediante `MockMvc`.
* **Datos Iniciales:** Carga automática de registros en `import.sql` para demostración inmediata.

##📦 Artefactos de Data Science
Los recursos del modelo se encuentran en la carpeta /models:

sentiment_pipeline_ternario_v2.pkl: Pipeline listo para producción.

notebooks/EDA_and_Training.ipynb: Documentación del proceso de entrenamiento y limpieza de datos.