import logging
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel
# Importamos la instancia del predictor desde el otro archivo
from .model_loader import predictor

# Configuración de logs del microservicio
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class InputData(BaseModel):
    text: str

app = FastAPI(
    title="Sentiment Analysis API",
    description="API para la clasificación de sentimientos en texto.",
    version="1.0.0")

@app.post("/predict",
          status_code=status.HTTP_200_OK)

def post_predict(data: InputData):
    text_to_process = data.text
    try:
        label, probability = predictor.predict(text_to_process)
        if label is None:
            logger.error("El modelo devolvió una predicción nula.")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, 
                detail="Error interno en el motor de predicción."
            )
        return{
            "prevision": label,
            "probabilidad": probability
        }
    except Exception as e:
        logger.error(f"Error no controlado en el endpoint: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error al procesar la solicitud."
        )
    