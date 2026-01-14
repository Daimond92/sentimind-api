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
  AUTH: {
    USERNAME: 'usuario',
    PASSWORD: '123456'
  },
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000
};

// Estado global
const state = {
  allAnalysis: [],
  filteredAnalysis: [],
  currentFilter: 'all',
  isLoading: false
};

// Elementos del DOM
const elements = {
  loader: document.getElementById('loader'),
  resultsList: document.getElementById('resultsList'),
  emptyState: document.getElementById('emptyState'),
  errorState: document.getElementById('errorState'),
  errorMessage: document.getElementById('errorMessage'),
  filterSentiment: document.getElementById('filterSentiment'),
  refreshBtn: document.getElementById('refreshBtn'),
  retryBtn: document.getElementById('retryBtn'),
  totalAnalysis: document.getElementById('totalAnalysis'),
  totalPositive: document.getElementById('totalPositive'),
  totalNegative: document.getElementById('totalNegative'),
  totalNeutral: document.getElementById('totalNeutral')
};

// Generar Basic Auth Header
const getAuthHeader = () => {
  const credentials = btoa(`${CONFIG.AUTH.USERNAME}:${CONFIG.AUTH.PASSWORD}`);
  return `Basic ${credentials}`;
};

// Formatear fecha
const formatDate = (timestamp) => {
  if (!timestamp) return 'N/A';
  try {
    const date = new Date(timestamp);
    return date.toLocaleString('es-AR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (err) {
    return 'Fecha inválida';
  }
};

// Obtener emoji según sentimiento
const getSentimentEmoji = (sentiment) => {
  if (!sentiment) return '❓';
  const sentimentLower = sentiment.toLowerCase();
  if (sentimentLower === 'positivo') return '😊';
  if (sentimentLower === 'negativo') return '😞';
  if (sentimentLower === 'neutro') return '😐';
  return '❓';
};

// Obtener clase CSS según sentimiento
const getSentimentClass = (sentiment) => {
  if (!sentiment) return 'neutral';
  const sentimentLower = sentiment.toLowerCase();
  if (sentimentLower === 'positivo') return 'positive';
  if (sentimentLower === 'negativo') return 'negative';
  if (sentimentLower === 'neutro') return 'neutral';
  return 'neutral';
};

// Truncar texto largo
const truncateText = (text, maxLength = 150) => {
  //  text es undefined, null o vacío
  if (!text || typeof text !== 'string') {
    return 'Sin texto disponible';
  }
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// Sleep utility
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Fetch con retry
const fetchWithRetry = async (url, options, attempt = 1) => {
  try {
    const response = await fetch(url, options);

    if (response.status === 401) {
      throw new Error('No autorizado. Verifica las credenciales.');
    }

    if (!response.ok) {
      if (response.status >= 500 && attempt < CONFIG.RETRY_ATTEMPTS) {
        await sleep(CONFIG.RETRY_DELAY * attempt);
        return fetchWithRetry(url, options, attempt + 1);
      }
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }

    return response;
  } catch (err) {
    if (err.name === 'TypeError' && attempt < CONFIG.RETRY_ATTEMPTS) {
      await sleep(CONFIG.RETRY_DELAY * attempt);
      return fetchWithRetry(url, options, attempt + 1);
    }
    throw err;
  }
};

// Cargar todos los análisis desde la API
const loadAllAnalysis = async () => {
  setLoadingState(true);
  hideError();

  try {
    const response = await fetchWithRetry(`${CONFIG.API_BASE_URL}/sentiment/all`, {
      method: 'GET',
      headers: {
        'Authorization': getAuthHeader(),
        'Accept': 'application/json'
      }
    });

    const data = await response.json();

    console.log('Datos recibidos del backend:', data);

    // Validar estructura de respuesta
    if (!Array.isArray(data)) {
      throw new Error('Respuesta inválida del servidor (no es un array)');
    }

    state.allAnalysis = data;
    state.filteredAnalysis = data;

    updateStats();
    renderAnalysisList();

  } catch (err) {
    console.error('Error cargando análisis:', err);
    showError(err.message);
  } finally {
    setLoadingState(false);
  }
};

// Actualizar estadísticas
const updateStats = () => {
  const total = state.allAnalysis.length;
  const positive = state.allAnalysis.filter(a =>
    a.sentiment && a.sentiment.toLowerCase() === 'positivo'
  ).length;
  const negative = state.allAnalysis.filter(a =>
    a.sentiment && a.sentiment.toLowerCase() === 'negativo'
  ).length;
  const neutral = state.allAnalysis.filter(a =>
    a.sentiment && a.sentiment.toLowerCase() === 'neutro'
  ).length;

  elements.totalAnalysis.textContent = total;
  elements.totalPositive.textContent = positive;
  elements.totalNegative.textContent = negative;
  elements.totalNeutral.textContent = neutral;
};

// Renderizar lista de análisis
const renderAnalysisList = () => {
  const container = elements.resultsList;

  if (state.filteredAnalysis.length === 0) {
    container.innerHTML = '';
    elements.emptyState.style.display = 'block';
    return;
  }

  elements.emptyState.style.display = 'none';

  // Ordenar por fecha (más recientes primero)
  const sortedAnalysis = [...state.filteredAnalysis].sort((a, b) => {
    const dateA = a.timestamp ? new Date(a.timestamp) : new Date(0);
    const dateB = b.timestamp ? new Date(b.timestamp) : new Date(0);
    return dateB - dateA;
  });

  container.innerHTML = sortedAnalysis.map(analysis => {
    // Asegurar que todos los campos existen
    const sentiment = analysis.sentiment || 'Desconocido';
    const confidence = typeof analysis.confidence === 'number' ? analysis.confidence : 0;
    const text = analysis.text || 'Sin texto disponible';
    const id = analysis.id || 'N/A';
    const timestamp = analysis.timestamp || null;

    return `
    <article class="analysis-card ${getSentimentClass(sentiment)}">
      <div class="analysis-header">
        <div class="sentiment-badge">
          <span class="sentiment-emoji">${getSentimentEmoji(sentiment)}</span>
          <span class="sentiment-text">${sentiment}</span>
        </div>
        <div class="confidence-badge">
          ${Math.round(confidence * 100)}%
        </div>
      </div>

      <div class="analysis-body">
        <p class="analysis-text">${truncateText(text)}</p>
      </div>

      <div class="analysis-footer">
        <span class="analysis-id">ID: ${id}</span>
        <span class="analysis-date">${formatDate(timestamp)}</span>
      </div>
    </article>
  `;
  }).join('');
};

// Filtrar análisis
const filterAnalysis = (sentiment) => {
  state.currentFilter = sentiment;

  if (sentiment === 'all') {
    state.filteredAnalysis = state.allAnalysis;
  } else {
    state.filteredAnalysis = state.allAnalysis.filter(a =>
      a.sentiment && a.sentiment.toLowerCase() === sentiment
    );
  }

  renderAnalysisList();
};

// Estado de carga
const setLoadingState = (isLoading) => {
  state.isLoading = isLoading;
  elements.loader.style.display = isLoading ? 'block' : 'none';
  elements.resultsList.style.display = isLoading ? 'none' : 'block';
  elements.refreshBtn.disabled = isLoading;
  elements.filterSentiment.disabled = isLoading;
};

// Mostrar error
const showError = (message) => {
  elements.errorState.style.display = 'block';
  elements.errorMessage.textContent = message;
  elements.resultsList.style.display = 'none';
  elements.emptyState.style.display = 'none';
};

// Ocultar error
const hideError = () => {
  elements.errorState.style.display = 'none';
};

// Event Listeners
elements.filterSentiment.addEventListener('change', (e) => {
  filterAnalysis(e.target.value);
});

elements.refreshBtn.addEventListener('click', () => {
  loadAllAnalysis();
});

elements.retryBtn.addEventListener('click', () => {
  loadAllAnalysis();
});

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
  console.log('Dashboard Sentimind v1.0.1 - Inicializado');
  console.log('API Endpoint:', CONFIG.API_BASE_URL);
  loadAllAnalysis();
});

// Manejo de errores globales
window.addEventListener('error', (event) => {
  console.error('Error global:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('Promise rechazada:', event.reason);
  event.preventDefault();
});