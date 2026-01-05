import { initLatest, tickLatest } from "./market.js";
import { initAggregated, tickAggregated } from "./candlestick.js";
import { initNews, tickNews } from "./news.js";

/* ===== CONFIG ===== */
export const CONFIG = {
    BASE_URL: "http://localhost:8081",
    LATEST_INTERVAL: "1m",
    POLL_MS: 30_000,

    AVAILABLE_SYMBOLS: ["BTC-USDT", "ETH-USDT", "SOL-USDT"],
    AVAILABLE_EXCHANGES: ["BINANCE", "OKX", "CRYPTO", "BYBIT"],
};

const symbolSelect = document.getElementById("symbolSelect");
const exchangeSelect = document.getElementById("exchangeSelect");
const aggSymbolSelect = document.getElementById("aggSymbolSelect");
const intervalBar = document.getElementById("aggIntervalBar");

// NEW: AI symbol selector (must exist in HTML)
const aiSymbolSelect = document.getElementById("aiSymbolSelect");

// init dropdowns
function populateSelect(select, options, defaultValue) {
    if (!select) return;
    select.innerHTML = "";
    options.forEach((v) => {
        const opt = document.createElement("option");
        opt.value = v;
        opt.textContent = v;
        select.appendChild(opt);
    });
    if (defaultValue && options.includes(defaultValue)) select.value = defaultValue;
    else if (options.length) select.value = options[0];
}

populateSelect(symbolSelect, CONFIG.AVAILABLE_SYMBOLS, "BTC-USDT");
populateSelect(aggSymbolSelect, CONFIG.AVAILABLE_SYMBOLS, "BTC-USDT");
populateSelect(exchangeSelect, CONFIG.AVAILABLE_EXCHANGES, "BINANCE");

// NEW: populate AI symbol dropdown with same options
populateSelect(aiSymbolSelect, CONFIG.AVAILABLE_SYMBOLS, "BTC-USDT");

// interval state (defaults to active button if present)
let aggInterval = "1m";

function setAggInterval(next) {
    aggInterval = next;
    intervalBar?.querySelectorAll(".intervalBtn").forEach((btn) => {
        btn.classList.toggle("is-active", btn.dataset.interval === next);
    });
}

// pick initial interval from DOM active button (if any)
const initialActive = intervalBar?.querySelector(".intervalBtn.is-active");
if (initialActive?.dataset?.interval) aggInterval = initialActive.dataset.interval;

// wire interval clicks
intervalBar?.addEventListener("click", (e) => {
    const btn = e.target.closest(".intervalBtn");
    if (!btn) return;
    const next = btn.dataset.interval;
    if (!next) return;
    setAggInterval(next);
    tickAll(); // refresh chart
});

// init modules
initLatest(CONFIG);
initAggregated(CONFIG);
initNews();

// Optional: sync candlestick symbol to market symbol
function syncCandlestickSymbolToMarket() {
    if (!aggSymbolSelect || !symbolSelect) return;
    aggSymbolSelect.value = symbolSelect.value;
}

async function tickAll() {
    try {
        const marketSymbol = symbolSelect?.value || "BTC-USDT";
        await tickLatest(CONFIG, { symbol: marketSymbol });

        const candleSymbol = aggSymbolSelect?.value || marketSymbol;
        const exchange = exchangeSelect?.value || "BINANCE";
        const interval = aggInterval;

        await tickAggregated(CONFIG, { symbol: candleSymbol, exchange, interval });

        // NEW: AI news uses its own dropdown symbol; fallback to marketSymbol
        const aiSymbol = aiSymbolSelect?.value || marketSymbol;
        await tickNews(CONFIG, { symbol: aiSymbol });
    } catch (err) {
        // If something throws outside module catches, don't kill the loop silently.
        console.error("tickAll error:", err);
    }
}

// events
symbolSelect?.addEventListener("change", async () => {
    syncCandlestickSymbolToMarket();
    await tickAll();
});

aggSymbolSelect?.addEventListener("change", () => tickAll());
exchangeSelect?.addEventListener("change", () => tickAll());

// NEW: AI dropdown change triggers refresh
aiSymbolSelect?.addEventListener("change", () => tickAll());

// auto start
tickAll();
setInterval(tickAll, CONFIG.POLL_MS);