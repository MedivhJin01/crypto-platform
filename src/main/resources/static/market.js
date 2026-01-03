let latestByExchange = new Map();

let tbodyLatest, dot, statusText, baseHostEl, latestUpdated;

function setStatus(state, text) {
    if (!dot || !statusText) return;
    dot.className = "dot " + state; // ok / err / fetch
    statusText.textContent = text;
}

function fmtTime(ms) {
    if (ms == null) return "—";
    return new Date(Number(ms)).toLocaleString();
}

function toNum(x) {
    if (x == null) return null;
    if (typeof x === "number") return x;
    const n = Number(x);
    return Number.isFinite(n) ? n : null;
}

function num(x, digits = 8) {
    const n = toNum(x);
    if (n == null) return "—";
    return n.toLocaleString(undefined, { maximumFractionDigits: digits });
}

function isEmpty(c) {
    return !c || c.openTime == null;
}

async function fetchLatest(CONFIG, symbol) {
    const url = `${CONFIG.BASE_URL}/candlestick/get/latest/${encodeURIComponent(symbol)}/${encodeURIComponent(CONFIG.LATEST_INTERVAL)}`;
    const res = await fetch(url);
    if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`Latest HTTP ${res.status}${t ? " — " + t : ""}`);
    }
    return res.json();
}

function renderLatest(symbol) {
    if (!tbodyLatest) return;
    tbodyLatest.innerHTML = "";

    [...latestByExchange.entries()]
        .sort(([a], [b]) => a.localeCompare(b))
        .forEach(([exchange, c]) => {
            const empty = isEmpty(c);
            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>
          <span class="pill">
            <span class="exDot ${empty ? "err" : "ok"}"></span>
            ${exchange}
          </span>
        </td>
        <td class="${empty ? "muted" : ""}">${empty ? "—" : fmtTime(c.openTime)}</td>
        <td class="${empty ? "muted" : ""}">${empty ? "—" : fmtTime(c.closeTime)}</td>
        <td class="num ${empty ? "muted" : ""}">${empty ? "—" : num(c.openPrice, 8)}</td>
        <td class="num ${empty ? "muted" : ""}">${empty ? "—" : num(c.highPrice, 8)}</td>
        <td class="num ${empty ? "muted" : ""}">${empty ? "—" : num(c.lowPrice, 8)}</td>
        <td class="num ${empty ? "muted" : ""}">${empty ? "—" : num(c.closePrice, 8)}</td>
        <td class="num ${empty ? "muted" : ""}">${empty ? "—" : num(c.volume, 6)}</td>
      `;
            tbodyLatest.appendChild(tr);
        });
}

export function initLatest() {
    tbodyLatest = document.getElementById("tbodyLatest");
    dot = document.getElementById("dot");
    statusText = document.getElementById("statusText");
    baseHostEl = document.getElementById("baseHost");
    latestUpdated = document.getElementById("latestUpdated");
}

export async function tickLatest(CONFIG, { symbol }) {
    // Update backend host text without destroying chip layout
    if (baseHostEl) baseHostEl.textContent = CONFIG.BASE_URL.replace(/^https?:\/\//, "");

    try {
        setStatus("fetch", "Fetching…");

        const data = await fetchLatest(CONFIG, symbol);
        latestByExchange = new Map(Object.entries(data));

        renderLatest(symbol);

        if (latestUpdated) latestUpdated.textContent = `Updated: ${new Date().toLocaleTimeString()}`;
        setStatus("ok", "Live");
    } catch (e) {
        console.error("tickLatest error:", e);
        setStatus("err", "Error");
        if (latestUpdated) latestUpdated.textContent = e?.message || String(e);
    }
}