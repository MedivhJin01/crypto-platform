let newsBody, newsUpdated;

export function initNews() {
    newsBody = document.getElementById("newsBody");
    newsUpdated = document.getElementById("newsUpdated");
}

export async function tickNews() {
    // Placeholder only — no API call yet
    if (!newsBody) return;

    newsBody.innerHTML = `
        <tr>
            <td class="muted">—</td>
            <td class="muted">
                AI-generated market analysis will be displayed here once
                the /ai endpoint is implemented.
            </td>
            <td class="muted">—</td>
        </tr>
    `;

    if (newsUpdated) {
        newsUpdated.textContent = "Waiting for AI service…";
    }
}