# Backend SentimentAPI - Guia de Implementacion y Logica de Negocio

Este documento detalla la estructura, logica y pasos seguidos para la construccion de la capa Backend de la aplicacion SentimentAPI utilizando Spring Boot.

## 1. Descripcion General de la Capa Backend
La aplicacion Backend actua como un Orquestador de Servicios. Su funcion principal es servir de interfaz entre el cliente final y el modelo de Data Science, gestionando la entrada de datos, la persistencia y la seguridad de la informacion.

## 2. Arquitectura de la Logica de Negocio

El desarrollo se dividio en las siguientes capas siguiendo las mejores practicas de diseño de software:

### Paso 1: Definicion del Modelo de Datos (DTOs)
Se crearon Objetos de Transferencia de Datos (DTOs) para asegurar que el intercambio de informacion cumpla estrictamente con el contrato acordado con el equipo de Data Science.
- SentimentRequest: Clase que captura el texto del usuario.
- SentimentResponse: Clase que mapea la respuesta del modelo, incluyendo los metadatos de "detalles".

### Paso 2: Capa de Validacion y Control (Controller)
Se implemento un RestController para exponer el endpoint POST /sentiment.
- Validacion de entrada: Se utiliza la libreria Validation para asegurar que el campo "text" no sea nulo ni este vacio antes de ser procesado.
- Manejo de Excepciones: Se implemento un manejador global para devolver errores legibles (400 Bad Request) en caso de entradas invalidas.

### Paso 3: Capa de Servicio y Consumo de Microservicio (Service)
Esta capa contiene la inteligencia de la orquestacion:
- Consumo HTTP: Se utiliza RestTemplate para realizar la peticion POST hacia el microservicio de Python (Data Science).
- Lógica de Fallback: Se preparo el sistema para manejar errores de conexion (503 Service Unavailable) en caso de que el modelo de IA no este en linea.

### Paso 4: Capa de Persistencia (Opcional - MVP+)
Se configuro Spring Data JPA con una base de datos H2 para almacenar el historial de predicciones.
- Cada registro incluye: Texto original, prediccion recibida, probabilidad y marca de tiempo (timestamp).

## 3. Contrato de API (Backend - Data Science)

Estandar establecido para la comunicacion entre microservicios:

### Request (Hacia el Backend)
METHOD: POST
URL: /sentiment
BODY:
{
"text": "El producto es de muy buena calidad"
}

### Response (Desde el Backend)
STATUS: 200 OK
BODY:
{
"prevision": "Positivo",
"probabilidad": 0.9452,
"detalles": {
"modelo": "logistic_regression_v1",
"clase_id": 1
}
}

## 4. Tecnologias Utilizadas
- Java 17 o superior.
- Spring Boot 3.x (Spring Web, Spring Data JPA, Validation).
- Maven (Gestor de dependencias).
- H2 Database (Persistencia en memoria).
- Lombok (Reduccion de codigo boilerplate).
- SpringDoc OpenAPI (Swagger para documentacion interactiva).

## 5. Instrucciones de Ejecucion

1. Clonar el repositorio.
2. Verificar que el puerto 8080 este disponible.
3. Ejecutar el comando: mvn spring-boot:run
4. Acceder a la documentacion en: http://localhost:8080/swagger-ui/index.html

## 6. Proximos Pasos Técnicos
- Implementacion de cache para textos repetidos.
- Desarrollo del endpoint /stats para analisis agregado de sentimientos.
- Configuracion de Docker para despliegue conjunto de Backend y Data Science.