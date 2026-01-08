import joblib
import logging

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
        prediction: int
        if self.model is None:
            return None, "Modelo no disponible"
        
        try:
            prediction = self.model.predict([text])[0]
            label = 'positivo' if prediction else "negativo"
            if hasattr(self.model, "predict_proba"):
                probas = self.model.predict_proba([text])[0]
                probability = float(max(probas))
            else:
                None
            return label, probability
        except Exception as e:
            logger.error(f"Error en inferencia: {e}")
            return None, str(e)

# Instanciamos el predictor para ser importado por la app
predictor = ModelPredictor('sentiment_pipeline.joblib')