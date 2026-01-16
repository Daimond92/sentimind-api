// Splash: mostrar logo y luego revelar la página
window.addEventListener("load", () => {
  const logoScreen = document.getElementById("logo-screen");
  const mainContent = document.getElementById("main-content");

  // Visible 1.2s, difuminado 1.6s
  setTimeout(() => {
    logoScreen.style.opacity = 0;
    setTimeout(() => {
      logoScreen.style.display = "none";
      mainContent.style.opacity = 1;
    }, 1600);
  }, 1200);
});

// Referencias de elementos
const textarea = document.getElementById('reviewText');
const charCount = document.getElementById('charCount');
const analyzeBtn = document.getElementById('analyzeBtn');
const loader = document.getElementById('loader');
const result = document.getElementById('result');
const error = document.getElementById('error');
const resultEmoji = document.getElementById('resultEmoji');
const resultSentiment = document.getElementById('resultSentiment');
const resultConfidence = document.getElementById('resultConfidence');

// Contador de caracteres
textarea.addEventListener('input', () => {
  const count = textarea.value.length;
  charCount.textContent = count;
  charCount.style.color = count < 10 ? '#f87171' : count > 480 ? '#facc15' : '#22c55e';
});

// Llamada a la API y manejo de estados
analyzeBtn.addEventListener('click', analyzeSentiment);

async function analyzeSentiment() {
  const text = textarea.value.trim();
  if (text.length < 10) { showError("Ingresa al menos 10 caracteres"); return; }

  // Reset estado de resultado y error
  result.className = "result";
  error.classList.remove("active");

  // Loading ON
  analyzeBtn.disabled = true;
  loader.classList.add("active");

  try {
    const response = await fetch("http://localhost:8080/api/v1/sentiment/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text })
    });
    if (!response.ok) throw new Error("Error en el servidor");
    const data = await response.json();
    displayResult(data);
  } catch (err) {
    showError("Error al conectar con la API. Asegúrate de que el servidor esté corriendo.");
  } finally {
    // Loading OFF
    loader.classList.remove("active");
    analyzeBtn.disabled = false;
  }
}

// Mostrar resultado
function displayResult(data) {
  const sentiment = data.sentiment || "Neutral";
  const confidence = ((data.confidence || 0) * 100).toFixed(0) + "%";

  resultSentiment.textContent = sentiment;
  resultConfidence.textContent = "Confianza: " + confidence;

  let emoji = "😐";
  let cls = "neutral";

  const s = (sentiment || "").toLowerCase();
  if (s.includes("positiv")) { emoji = "😊"; cls = "positive"; }
  else if (s.includes("negativ")) { emoji = "😞"; cls = "negative"; }

  resultEmoji.textContent = emoji;
  result.className = "result active " + cls;
}

// Mostrar error temporal
function showError(msg) {
  error.textContent = msg;
  error.classList.add("active");
  setTimeout(() => error.classList.remove("active"), 4000);
}