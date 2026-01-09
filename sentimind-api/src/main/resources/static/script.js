const CONFIG = {
  API_BASE_URL: window.location.hostname === 'localhost'
    ? "http://localhost:8080/api/v1"
    : "https://api.sentimind.com/api/v1",
  MIN_TEXT_LENGTH: 10,
  MAX_TEXT_LENGTH: 500,
  SPLASH_DURATION: 1200,
  SPLASH_FADE_DURATION: 1600,
  ERROR_DISPLAY_DURATION: 4000,
  RATE_LIMIT_COOLDOWN: 2000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
  AUTOSAVE_KEY: 'sentimind-draft',
  AUTOSAVE_DEBOUNCE: 500,
  // Credenciales de autenticación
  AUTH: {
    USERNAME: 'usuario',
    PASSWORD: '123456'
  }
};

const CSS_CLASSES = {
  ACTIVE: 'active',
  POSITIVE: 'positive',
  NEGATIVE: 'negative',
  NEUTRAL: 'neutral'
};

const SENTIMENT_CONFIG = {
  positive: { emoji: '😊', cssClass: CSS_CLASSES.POSITIVE },
  negative: { emoji: '😞', cssClass: CSS_CLASSES.NEGATIVE },
  neutral: { emoji: '😐', cssClass: CSS_CLASSES.NEUTRAL }
};

// ESTADO GLOBAL
const state = {
  isAnalyzing: false,
  lastAnalysisTime: 0,
  retryCount: 0,
  isOfflineMode: false // Para modo offline
};

// Generar Basic Auth Header
const getAuthHeader = () => {
  const credentials = btoa(`${CONFIG.AUTH.USERNAME}:${CONFIG.AUTH.PASSWORD}`);
  return `Basic ${credentials}`;
};

// SPLASH SCREEN
window.addEventListener("load", () => {
  const logoScreen = document.getElementById("logo-screen");
  const mainContent = document.getElementById("main-content");

  setTimeout(() => {
    logoScreen.style.opacity = 0;
    setTimeout(() => {
      logoScreen.remove();
      mainContent.style.opacity = 1;
    }, CONFIG.SPLASH_FADE_DURATION);
  }, CONFIG.SPLASH_DURATION);
});

// REFERENCIAS DE ELEMENTOS
const elements = {
  textarea: document.getElementById('reviewText'),
  charCount: document.getElementById('charCount'),
  analyzeBtn: document.getElementById('analyzeBtn'),
  loader: document.getElementById('loader'),
  result: document.getElementById('result'),
  error: document.getElementById('error'),
  resultEmoji: document.getElementById('resultEmoji'),
  resultSentiment: document.getElementById('resultSentiment'),
  resultConfidence: document.getElementById('resultConfidence'),
  resultId: document.getElementById('resultId'),
  resultTimestamp: document.getElementById('resultTimestamp')
};

// UTILIDADES
const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

const capitalizeFirst = (str) => {
  return str.charAt(0).toUpperCase() + str.slice(1);
};

const sanitizeText = (text) => {
  return text.trim().replace(/\s+/g, ' ');
};

const isSpamText = (text) => {
  const repeatedChar = /(.)\1{9,}/;
  return repeatedChar.test(text);
};

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Formatear fecha/hora
const formatTimestamp = (timestamp) => {
  if (!timestamp) return 'N/A';
  const date = new Date(timestamp);
  return date.toLocaleString('es-AR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// AUTOSAVE FUNCIONALIDAD
const autosave = debounce((value) => {
  try {
    if (value.length > 0) {
      localStorage.setItem(CONFIG.AUTOSAVE_KEY, value);
    } else {
      localStorage.removeItem(CONFIG.AUTOSAVE_KEY);
    }
  } catch (err) {
    console.warn('No se pudo guardar el borrador:', err);
  }
}, CONFIG.AUTOSAVE_DEBOUNCE);

const loadDraft = () => {
  try {
    const draft = localStorage.getItem(CONFIG.AUTOSAVE_KEY);
    if (draft) {
      elements.textarea.value = draft;
      updateCharCount();
    }
  } catch (err) {
    console.warn('No se pudo cargar el borrador:', err);
  }
};

// CONTADOR DE CARACTERES
const updateCharCount = () => {
  const count = elements.textarea.value.length;
  elements.charCount.textContent = count;

  if (count < CONFIG.MIN_TEXT_LENGTH) {
    elements.charCount.style.color = '#f87171';
  } else if (count > 480) {
    elements.charCount.style.color = '#facc15';
  } else {
    elements.charCount.style.color = '#22c55e';
  }
};

elements.textarea.addEventListener('input', () => {
  updateCharCount();
  autosave(elements.textarea.value);
});

// EVENTOS
elements.analyzeBtn.addEventListener('click', analyzeSentiment);

elements.textarea.addEventListener('keydown', (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault();
    analyzeSentiment();
  }
});

// VALIDACIONES
const validateInput = (text) => {
  const sanitized = sanitizeText(text);

  if (sanitized.length < CONFIG.MIN_TEXT_LENGTH) {
    return {
      valid: false,
      error: `Ingresa al menos ${CONFIG.MIN_TEXT_LENGTH} caracteres`
    };
  }

  if (sanitized.length > CONFIG.MAX_TEXT_LENGTH) {
    return {
      valid: false,
      error: `El texto no puede exceder ${CONFIG.MAX_TEXT_LENGTH} caracteres`
    };
  }

  if (isSpamText(sanitized)) {
    return {
      valid: false,
      error: 'El texto parece ser spam. Por favor, ingresa contenido válido'
    };
  }

  return { valid: true, text: sanitized };
};

const isValidResponse = (data) => {
  return data &&
         typeof data === 'object' &&
         'sentiment' in data &&
         'confidence' in data &&
         typeof data.sentiment === 'string' &&
         typeof data.confidence === 'number' &&
         data.confidence >= 0 &&
         data.confidence <= 1;
};

// RATE LIMITING
const checkRateLimit = () => {
  const now = Date.now();
  const timeSinceLastAnalysis = now - state.lastAnalysisTime;

  if (timeSinceLastAnalysis < CONFIG.RATE_LIMIT_COOLDOWN) {
    const waitTime = Math.ceil((CONFIG.RATE_LIMIT_COOLDOWN - timeSinceLastAnalysis) / 1000);
    return {
      allowed: false,
      error: `Espera ${waitTime} segundo${waitTime > 1 ? 's' : ''} antes de analizar nuevamente`
    };
  }

  return { allowed: true };
};

// Análisis Mock (Fallback cuando falla la API)
const analyzeSentimentMock = (text) => {
  const lowerText = text.toLowerCase();

  let sentiment, confidence;

  if (lowerText.includes('bueno') || lowerText.includes('excelente') ||
      lowerText.includes('increíble') || lowerText.includes('maravilloso')) {
    sentiment = 'Positivo';
    confidence = 0.85;
  } else if (lowerText.includes('malo') || lowerText.includes('terrible') ||
             lowerText.includes('horrible') || lowerText.includes('pésimo')) {
    sentiment = 'Negativo';
    confidence = 0.80;
  } else {
    sentiment = 'Neutro';
    confidence = 0.70;
  }

  return {
    id: null,
    sentiment,
    confidence,
    timestamp: new Date().toISOString(),
    isOffline: true
  };
};

// API CALL CON RETRY Y AUTH
const fetchWithRetry = async (url, options, attempt = 1) => {
  try {
    const response = await fetch(url, options);

    // Manejo específico de error 401 (No autorizado)
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED');
    }

    if (!response.ok) {
      if (response.status >= 500 && attempt < CONFIG.RETRY_ATTEMPTS) {
        console.warn(`Intento ${attempt} falló, reintentando...`);
        await sleep(CONFIG.RETRY_DELAY * attempt);
        return fetchWithRetry(url, options, attempt + 1);
      }

      // Capturar errores del GlobalExceptionHandler
      const errorData = await response.json().catch(() => ({}));

      if (errorData.errors) {
        // Formato de error de validación
        const errorMessages = Object.values(errorData.errors).join('. ');
        throw new Error(errorMessages);
      }

      throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
    }

    return response;
  } catch (err) {
    if (err.message === 'UNAUTHORIZED') {
      throw err;
    }

    if (err.name === 'TypeError' && attempt < CONFIG.RETRY_ATTEMPTS) {
      console.warn(`Intento ${attempt} falló por error de red, reintentando...`);
      await sleep(CONFIG.RETRY_DELAY * attempt);
      return fetchWithRetry(url, options, attempt + 1);
    }
    throw err;
  }
};

// FUNCIÓN PRINCIPAL DE ANÁLISIS
async function analyzeSentiment() {
  if (state.isAnalyzing) {
    showError('Ya hay un análisis en progreso');
    return;
  }

  const text = elements.textarea.value;

  const validation = validateInput(text);
  if (!validation.valid) {
    showError(validation.error);
    return;
  }

  const rateCheck = checkRateLimit();
  if (!rateCheck.allowed) {
    showError(rateCheck.error);
    return;
  }

  resetUI();
  setLoadingState(true);
  state.isAnalyzing = true;
  state.lastAnalysisTime = Date.now();

  try {
    // Headers con autenticación Basic Auth
    const response = await fetchWithRetry(`${CONFIG.API_BASE_URL}/sentiment`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Authorization": getAuthHeader() // Credenciales de Raúl
      },
      body: JSON.stringify({ text: validation.text })
    });

    const data = await response.json();

    if (!isValidResponse(data)) {
      throw new Error("Respuesta inválida del servidor");
    }

    displayResult(data);
    state.retryCount = 0;
    state.isOfflineMode = false;

  } catch (err) {
    console.error("Error en análisis:", err);

    // Modo offline si falla la conexión
    if (err.name === 'TypeError' || err.message.includes('Failed to fetch')) {
      console.warn('⚠️ Activando modo offline (Mock AI)');
      state.isOfflineMode = true;
      const mockResult = analyzeSentimentMock(validation.text);
      displayResult(mockResult);
      showWarning('Modo offline activado: Usando análisis local (IA no disponible)');
    } else {
      handleError(err);
    }
  } finally {
    setLoadingState(false);
    state.isAnalyzing = false;
  }
}

// MANEJO DE ERRORES
const handleError = (err) => {
  let errorMessage = "Error desconocido";

  // Errores específicos del backend
  if (err.message === 'UNAUTHORIZED') {
    errorMessage = "Error de autenticación. Por favor, contacta al administrador.";
  } else if (err.name === 'TypeError' && err.message.includes('fetch')) {
    errorMessage = "No se puede conectar con el servidor. Verifica tu conexión a internet.";
  } else if (err.message.includes('429')) {
    errorMessage = "Demasiadas solicitudes. Intenta nuevamente en unos minutos.";
  } else if (err.message.includes('timeout')) {
    errorMessage = "La solicitud tardó demasiado. Intenta con un texto más corto.";
  } else if (err.message.includes('caracteres')) {
    // Error de validación del backend
    errorMessage = err.message;
  } else {
    errorMessage = err.message;
  }

  showError(errorMessage);
};

// Mostrar advertencias (diferente a errores)
const showWarning = (msg) => {
  const warningEl = document.createElement('div');
  warningEl.className = 'warning active';
  warningEl.textContent = '⚠️ ' + msg;
  warningEl.setAttribute('role', 'alert');

  elements.result.parentElement.insertBefore(warningEl, elements.result);

  setTimeout(() => {
    warningEl.remove();
  }, CONFIG.ERROR_DISPLAY_DURATION);
};

// UI - MOSTRAR RESULTADO
const displayResult = (data) => {
  const sentiment = data.sentiment.toLowerCase();
  const confidence = Math.round(data.confidence * 100);

  // Obtener configuración ANTES de asignar valores
  const config = SENTIMENT_CONFIG[sentiment] || SENTIMENT_CONFIG.neutral;

  // Actualizar emoji PRIMERO
  elements.resultEmoji.textContent = config.emoji;

  // Luego actualizar textos
  elements.resultSentiment.textContent = capitalizeFirst(data.sentiment);
  elements.resultConfidence.textContent = `Confianza: ${confidence}%`;

  // Mostrar ID y Timestamp
  if (elements.resultId) {
    elements.resultId.textContent = data.isOffline ?
      'ID: Local (sin guardar)' :
      `ID: ${data.id || 'N/A'}`;
  }

  if (elements.resultTimestamp) {
    elements.resultTimestamp.textContent = `Fecha: ${formatTimestamp(data.timestamp)}`;
  }

  // Indicador visual de modo offline
  const offlineClass = data.isOffline ? 'offline-mode' : '';
  elements.result.className = `result ${CSS_CLASSES.ACTIVE} ${config.cssClass} ${offlineClass}`;

  if (window.innerWidth < 768) {
    elements.result.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }
};

// UI - MOSTRAR ERROR
const showError = (msg) => {
  elements.error.textContent = msg;
  elements.error.classList.add(CSS_CLASSES.ACTIVE);
  elements.error.setAttribute('role', 'alert');

  setTimeout(() => {
    elements.error.classList.remove(CSS_CLASSES.ACTIVE);
  }, CONFIG.ERROR_DISPLAY_DURATION);
};

// UI - ESTADO DE CARGA
const setLoadingState = (isLoading) => {
  elements.analyzeBtn.disabled = isLoading;
  elements.analyzeBtn.textContent = isLoading ? "Analizando..." : "Analizar Sentimiento";
  elements.analyzeBtn.setAttribute('aria-busy', isLoading);

  if (isLoading) {
    elements.loader.classList.add(CSS_CLASSES.ACTIVE);
    elements.loader.setAttribute('aria-live', 'polite');
  } else {
    elements.loader.classList.remove(CSS_CLASSES.ACTIVE);
  }
};

// UI - RESET
const resetUI = () => {
  elements.result.className = "result";
  elements.error.classList.remove(CSS_CLASSES.ACTIVE);
};

// INICIALIZACIÓN
document.addEventListener('DOMContentLoaded', () => {
  loadDraft();
  updateCharCount();

  console.log('Sentimind v1.1.0 - Production (Auth Enabled)');
  console.log('API Endpoint:', CONFIG.API_BASE_URL);
});

window.addEventListener('beforeunload', () => {
  if (elements.textarea.value.trim().length > 0) {
    localStorage.setItem(CONFIG.AUTOSAVE_KEY, elements.textarea.value);
  }
});

window.addEventListener('error', (event) => {
  console.error('Error global capturado:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('Promise rechazada sin manejar:', event.reason);
  event.preventDefault();
});