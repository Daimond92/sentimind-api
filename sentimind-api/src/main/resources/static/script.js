const CONFIG = {
  // Detección automática de entorno
  API_BASE_URL: (() => {
    // Desarrollo local
    if (window.location.hostname === 'localhost' ||
        window.location.hostname === '127.0.0.1') {
      return "http://localhost:8080/api/v1";
    }

    // Producción: Detectar automáticamente (funciona para Ngrok y OCI)
    const protocol = window.location.protocol; // http: o https:
    const host = window.location.host; // hostname:puerto
    return `${protocol}//${host}/api/v1`;
  })(),
  MIN_TEXT_LENGTH: 10,
  MAX_TEXT_LENGTH: 500,
  SPLASH_DURATION: 1200,
  SPLASH_FADE_DURATION: 1600,
  ERROR_DISPLAY_DURATION: 4000,
  RATE_LIMIT_COOLDOWN: 2000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
  AUTOSAVE_KEY: 'sentimind-draft',
  AUTOSAVE_DEBOUNCE: 500
};

const CSS_CLASSES = {
  ACTIVE: 'active',
  POSITIVE: 'positive',
  NEGATIVE: 'negative',
  NEUTRAL: 'neutral'
};

// ESTADO GLOBAL
const state = {
  isAnalyzing: false,
  lastAnalysisTime: 0,
  retryCount: 0,
  isOfflineMode: false
};

// SPLASH SCREEN
window.addEventListener("load", () => {
  const logoScreen = document.getElementById("logo-screen");
  const mainContent = document.getElementById("main-content");

  // Visible 1.2s, difuminado 1.6s
  setTimeout(() => {
    logoScreen.style.opacity = 0;
    setTimeout(() => {
      logoScreen.style.display = "none";
      mainContent.style.opacity = 1;
    }, CONFIG.SPLASH_FADE_DURATION);
  }, CONFIG.SPLASH_DURATION);
});

// Referencias de elementos
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

// Utilidades
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));
const capitalizeFirst = (str) => str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
const sanitizeText = (text) => text.trim().replace(/\s+/g, ' ');
const isSpamText = (text) => /^(.)\1{9,}$/.test(text) || /^[^a-záéíóúñ]*$/i.test(text);
const formatTimestamp = (ts) => ts ? new Date(ts).toLocaleString('es-ES') : 'N/A';

// Contador de caracteres
elements.textarea.addEventListener('input', () => {
  const count = elements.textarea.value.length;
  elements.charCount.textContent = count;
  elements.charCount.style.color = count < 10 ? '#f87171' : count > 480 ? '#facc15' : '#22c55e';
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

// API CALL CON RETRY
const fetchWithRetry = async (url, options, attempt = 1) => {
  try {
    const response = await fetch(url, options);

    if (response.status === 401) {
      throw new Error('UNAUTHORIZED');
    }

    if (!response.ok) {
      if (response.status >= 500 && attempt < CONFIG.RETRY_ATTEMPTS) {
        console.warn(`Intento ${attempt} falló, reintentando...`);
        await sleep(CONFIG.RETRY_DELAY * attempt);
        return fetchWithRetry(url, options, attempt + 1);
      }

      const errorData = await response.json().catch(() => ({}));

      if (errorData.errors) {
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
  const text = elements.textarea.value.trim();
  
  // Validación
  const validation = validateInput(text);
  if (!validation.valid) {
    showError(validation.error);
    return;
  }

  // Rate limiting
  const rateCheck = checkRateLimit();
  if (!rateCheck.allowed) {
    showError(rateCheck.error);
    return;
  }

  // Reset estado de resultado y error
  elements.result.className = "result";
  elements.error.classList.remove("active");

  // Loading ON
  elements.analyzeBtn.disabled = true;
  elements.loader.classList.add("active");
  state.isAnalyzing = true;

  try {
    const response = await fetchWithRetry(`${CONFIG.API_BASE_URL}/sentiment`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json"
      },
      body: JSON.stringify({ text: validation.text })
    });

    if (!response.ok) throw new Error("Error en el servidor");
    
    const data = await response.json();
    
    if (!isValidResponse(data)) {
      throw new Error("Respuesta inválida del servidor");
    }
    
    state.lastAnalysisTime = Date.now();
    state.isOfflineMode = false;
    displayResult(data);
  } catch (err) {
    console.error("Error en análisis:", err);

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
    // Loading OFF
    elements.loader.classList.remove("active");
    elements.analyzeBtn.disabled = false;
    state.isAnalyzing = false;
  }
}

// MANEJO DE ERRORES
const handleError = (err) => {
  let errorMessage = "Error desconocido";

  if (err.message === 'UNAUTHORIZED') {
    errorMessage = "Error de autenticación. Por favor, contacta al administrador.";
  } else if (err.name === 'TypeError' && err.message.includes('fetch')) {
    errorMessage = "No se puede conectar con el servidor. Verifica tu conexión a internet.";
  } else if (err.message.includes('429')) {
    errorMessage = "Demasiadas solicitudes. Intenta nuevamente en unos minutos.";
  } else if (err.message.includes('timeout')) {
    errorMessage = "La solicitud tardó demasiado. Intenta con un texto más corto.";
  } else if (err.message.includes('caracteres')) {
    errorMessage = err.message;
  } else {
    errorMessage = err.message;
  }

  showError(errorMessage);
};

// Mostrar advertencias
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

// UI - MOSTRAR RESULTADO (con fix para emoji en español e inglés)
const displayResult = (data) => {
  const sentiment = (data.sentiment || "").toLowerCase();
  const confidence = Math.round(data.confidence * 100);

  // Detectar emoji y clase CSS (funciona con español e inglés)
  let emoji = "😐";
  let cssClass = CSS_CLASSES.NEUTRAL;

  if (sentiment.includes("positiv")) {
    emoji = "😊";
    cssClass = CSS_CLASSES.POSITIVE;
  } else if (sentiment.includes("negativ")) {
    emoji = "😞";
    cssClass = CSS_CLASSES.NEGATIVE;
  }

  elements.resultEmoji.textContent = emoji;
  elements.resultSentiment.textContent = capitalizeFirst(data.sentiment);
  elements.resultConfidence.textContent = `Confianza: ${confidence}%`;

  if (elements.resultId) {
    elements.resultId.textContent = data.isOffline ?
        'ID: Local (sin guardar)' :
        `ID: ${data.id || 'N/A'}`;
  }

  if (elements.resultTimestamp) {
    elements.resultTimestamp.textContent = `Fecha: ${formatTimestamp(data.timestamp)}`;
  }

  const offlineClass = data.isOffline ? 'offline-mode' : '';
  elements.result.className = `result ${CSS_CLASSES.ACTIVE} ${cssClass} ${offlineClass}`;

  if (window.innerWidth < 768) {
    elements.result.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }
};

// Mostrar error temporal
function showError(msg) {
  elements.error.textContent = msg;
  elements.error.classList.add("active");
  setTimeout(() => elements.error.classList.remove("active"), CONFIG.ERROR_DISPLAY_DURATION);
}

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
  // Restaurar borrador guardado
  const savedDraft = localStorage.getItem(CONFIG.AUTOSAVE_KEY);
  if (savedDraft) {
    elements.textarea.value = savedDraft;
    elements.charCount.textContent = savedDraft.length;
  }

  console.log('Sentimind v1.1.1 - Production (Public API)');
  console.log('API Endpoint:', CONFIG.API_BASE_URL);
});

window.addEventListener('beforeunload', () => {
  if (elements.textarea.value.trim().length > 0) {
    localStorage.setItem(CONFIG.AUTOSAVE_KEY, elements.textarea.value);
  }
});
