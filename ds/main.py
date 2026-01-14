from fastapi import FastAPI
from pydantic import BaseModel
import joblib

app = FastAPI()

# Cargar el modelo que ya tienes en el repo
model = joblib.load('models/sentiment_pipeline_ternario_v2.pkl')

class TextData(BaseModel):
    text: str

@app.post("/predict")
def predict_sentiment(data: TextData):
    # El pipeline de sklearn suele devolver la clase directamente
    prediction = model.predict([data.text])[0] 
    
    # Para obtener la confianza, el modelo debe soportar predict_proba
    probabilities = model.predict_proba([data.text])[0]
    confidence = float(max(probabilities)) 

    return {
        "sentiment": str(prediction), # "positivo", "negativo" o "neutro"
        "confidence": confidence      # Ejemplo: 0.95
    }