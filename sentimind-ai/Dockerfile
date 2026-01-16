# Usar una imagen de Python ligera
FROM python:3.11-slim

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo de requerimientos (asumiendo que está en la raíz de ds)
COPY requirements.txt .

# Instalar dependencias
RUN pip install --no-cache-dir -r requirements.txt

# Copiar todo el contenido de la carpeta ds al contenedor
COPY . .

# Agregamos esta línea para que el sistema encuentre 'uvicorn'
ENV PATH="/home/python/.local/bin:${PATH}"

# Exponer el puerto que usa FastAPI
EXPOSE 8000

# Comando para arrancar la aplicación
# Usamos service.app porque tu archivo está en ds/service/app.py
CMD ["python", "-m", "uvicorn", "service.app:app", "--host", "0.0.0.0", "--port", "8000", "--app-dir", "service"]