// news.js

let newsBody, newsUpdated;

export function initNews() {
    newsBody = document.getElementById("newsBody");
    newsUpdated = document.getElementById("newsUpdated");
}

function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

function renderLinks(linksByTitle) {
    if (!linksByTitle || typeof linksByTitle !== "object") {
        return `<span class="muted">—</span>`;
    }

    const entries = Object.entries(linksByTitle);
    if (!entries.length) return `<span class="muted">—</span>`;

    return entries
        .map(([title, url]) => {
            const safeTitle = escapeHtml(title);
            const safeUrl = escapeHtml(url);
            return `
        <div class="news-link">
          <a href="${safeUrl}" target="_blank" rel="noopener noreferrer">
            ${safeTitle}
          </a>
        </div>
      `;
        })
        .join("");
}

export async function tickNews(CONFIG, { symbol } = {}) {
    if (!newsBody) return;

    const sym = symbol || "BTC-USDT";

    try {
        const res = await fetch(
            `${CONFIG.BASE_URL}/ai/demo?symbol=${encodeURIComponent(sym)}`,
            { method: "POST" }
        );
        if (!res.ok) throw new Error(`AI request failed: ${res.status}`);

        const data = await res.json();

        // AiNewsResponse:
        const dateText = data.updateAt ? String(data.updateAt) : "—";
        const analysisText = data.summary ?? "—";
        const linksHtml = renderLinks(data.linksByTitle);

        // IMPORTANT: column order MUST match thead: Date | Analysis | News Links
        newsBody.innerHTML = `
      <tr>
        <td class="mono">${escapeHtml(dateText)}</td>
        <td class="newsAnalysisCell">${escapeHtml(analysisText)}</td>
        <td class="newsLinksCell">${linksHtml}</td>
      </tr>
    `;

        if (newsUpdated) {
            newsUpdated.textContent = `symbol: ${sym} • updated: ${new Date().toLocaleTimeString()}`;
        }
    } catch (err) {
        console.error("tickNews error:", err);

        // keep thead order: Date | Analysis | News Links
        newsBody.innerHTML = `
      <tr>
        <td class="muted">—</td>
        <td class="muted">AI analysis unavailable</td>
        <td class="muted">—</td>
      </tr>
    `;

        if (newsUpdated) {
            newsUpdated.textContent = "Waiting for AI service…";
        }
    }
}