import joblib
import logging
import os

# Configuración de logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class ModelPredictor:
    def __init__(self, model_path: str):
        self.model_path = model_path
        self.model = self._load_model()

    def _load_model(self):
        try:
            model = joblib.load(self.model_path)
            logger.info(f"¡Modelo '{self.model_path}' cargado con éxito!")
            return model
        except FileNotFoundError:
            logger.error(f"Archivo no encontrado: {self.model_path}")
        except Exception as e:
            logger.error(f"Error al cargar el modelo: {e}")
            return None

    def predict(self, text: str):
        if self.model is None:
            return None, "Modelo no disponible"
        
        try:
            prediction = self.model.predict([text])[0]
            prediction = str(prediction).lower()
            # Obtenemos la probabilidad si el modelo lo permite
            probability = 0.0
            if hasattr(self.model, "predict_proba"):
                probas = self.model.predict_proba([text])[0]
                probability = float(max(probas))
            
            return prediction, probability
        except Exception as e:
            logger.error(f"Error en inferencia: {e}")
            return None, str(e)

# Buscamos la ruta absoluta al modelo subiendo un nivel desde ds/service hacia ds/models
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_PATH = os.path.join(BASE_DIR, "models", "sentiment_pipeline_ternario_v2.pkl")

# Instanciamos el predictor para ser importado por la app
predictor = ModelPredictor(MODEL_PATH)