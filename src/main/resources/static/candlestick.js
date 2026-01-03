let chart, candleSeries, volumeSeries;
let aggUpdated, chartEl;

// --- incremental update state ---
let lastKey = null;         // "symbol|exchange|interval"
let lastTime = null;        // last candle unix seconds
let hasLoadedOnce = false;

function toNum(x) {
    if (x == null) return null;
    if (typeof x === "number") return x;
    const n = Number(x);
    return Number.isFinite(n) ? n : null;
}

async function fetchAggregated(CONFIG, { exchange, symbol, interval, openTime, closeTime }) {
    const qs = new URLSearchParams({
        exchange,
        symbol,
        interval,
        openTime: String(openTime),
        closeTime: String(closeTime),
    }).toString();

    const url = `${CONFIG.BASE_URL}/candlestick/get/aggregated?${qs}`;
    const res = await fetch(url);
    if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`Agg HTTP ${res.status}${t ? " — " + t : ""}`);
    }
    return res.json(); // Candlestick[]
}

function toChartCandle(cs) {
    const time = Math.floor(Number(cs.openTime) / 1000);
    const o = toNum(cs.openPrice);
    const h = toNum(cs.highPrice);
    const l = toNum(cs.lowPrice);
    const c = toNum(cs.closePrice);

    if ([time, o, h, l, c].some(v => v == null)) return null;
    return { time, open: o, high: h, low: l, close: c };
}

function toChartVolume(cs) {
    const time = Math.floor(Number(cs.openTime) / 1000);
    const v = toNum(cs.volume);
    const o = toNum(cs.openPrice);
    const c = toNum(cs.closePrice);

    if ([time, v, o, c].some(x => x == null)) return null;

    const up = c >= o;
    return {
        time,
        value: v,
        color: up ? "rgba(34,197,94,0.35)" : "rgba(239,68,68,0.35)",
    };
}

// detect whether user is “near real-time” (so we can keep auto-follow)
function isNearRightEdge(range, latestTime) {
    if (!range || latestTime == null) return true;
    const to = range.to; // unix seconds
    // if your visible range ends within ~2 bars of the last bar, treat as live-follow
    return (latestTime - to) <= 2 * 60; // 120 seconds threshold; good enough for 1m-10m
}

export function initAggregated() {
    aggUpdated = document.getElementById("aggUpdated");
    chartEl = document.getElementById("chart");

    chart = LightweightCharts.createChart(chartEl, {
        layout: {
            background: { type: "solid", color: "rgba(2,6,23,0.00)" },
            textColor: "rgba(229,231,235,0.85)",
        },
        grid: {
            vertLines: { color: "rgba(148,163,184,0.10)" },
            horzLines: { color: "rgba(148,163,184,0.10)" },
        },
        rightPriceScale: { borderColor: "rgba(148,163,184,0.14)" },
        timeScale: {
            borderColor: "rgba(148,163,184,0.14)",
            timeVisible: true,
            secondsVisible: false,
        },
        crosshair: { mode: LightweightCharts.CrosshairMode.Normal },
    });

    candleSeries = chart.addCandlestickSeries({
        upColor: "#22c55e",
        downColor: "#ef4444",
        borderDownColor: "#ef4444",
        borderUpColor: "#22c55e",
        wickDownColor: "#ef4444",
        wickUpColor: "#22c55e",
    });

    volumeSeries = chart.addHistogramSeries({
        priceFormat: { type: "volume" },
        priceScaleId: "vol",
    });

    candleSeries.priceScale().applyOptions({
        scaleMargins: { top: 0.08, bottom: 0.28 },
    });
    volumeSeries.priceScale().applyOptions({
        scaleMargins: { top: 0.72, bottom: 0.02 },
    });

    chart.priceScale("vol").applyOptions({
        visible: false,
        borderVisible: false,
    });

    new ResizeObserver(() => {
        chart.applyOptions({ width: chartEl.clientWidth, height: chartEl.clientHeight });
    }).observe(chartEl);
}

export async function tickAggregated(CONFIG, { symbol, exchange, interval }) {
    const key = `${symbol}|${exchange}|${interval}`;
    const now = Date.now();
    const openTime = now - 24 * 60 * 60 * 1000;
    const closeTime = now;

    // capture user viewport BEFORE updating (so we can restore it)
    const prevRange = chart?.timeScale()?.getVisibleRange?.() ?? null;

    try {
        aggUpdated.textContent = "Loading…";

        const list = await fetchAggregated(CONFIG, { exchange, symbol, interval, openTime, closeTime });
        const arr = Array.isArray(list) ? list : [];

        const candleData = arr.map(toChartCandle).filter(Boolean).sort((a, b) => a.time - b.time);
        const volData = arr.map(toChartVolume).filter(Boolean).sort((a, b) => a.time - b.time);

        if (!candleData.length) {
            candleSeries.setData([]);
            volumeSeries.setData([]);
            aggUpdated.textContent = `No data (${exchange} • ${interval} • 24h)`;
            return;
        }

        const latestTime = candleData[candleData.length - 1].time;

        // If first load OR the query changed => full reset is fine
        if (!hasLoadedOnce || lastKey !== key) {
            candleSeries.setData(candleData);
            volumeSeries.setData(volData);

            chart.timeScale().fitContent(); // only here
            lastKey = key;
            lastTime = latestTime;
            hasLoadedOnce = true;

            aggUpdated.textContent = `Updated: ${new Date().toLocaleTimeString()} (${exchange} • ${interval} • 24h)`;
            return;
        }

        // Otherwise: incremental update
        // decide whether we should auto-follow to the right edge
        const follow = isNearRightEdge(prevRange, lastTime);

        // append/update only new bars (and the last bar)
        // Important: series.update() can both update an existing last bar and append a new one.
        for (let i = 0; i < candleData.length; i++) {
            const p = candleData[i];
            if (lastTime == null || p.time >= lastTime) {
                candleSeries.update(p);
            }
        }

        for (let i = 0; i < volData.length; i++) {
            const p = volData[i];
            if (lastTime == null || p.time >= lastTime) {
                volumeSeries.update(p);
            }
        }

        lastTime = latestTime;

        // Preserve viewport if user panned/zoomed away; otherwise keep following live
        if (follow) {
            chart.timeScale().scrollToRealTime();
        } else if (prevRange) {
            chart.timeScale().setVisibleRange(prevRange);
        }

        aggUpdated.textContent = `Updated: ${new Date().toLocaleTimeString()} (${exchange} • ${interval} • 24h)`;
    } catch (e) {
        aggUpdated.textContent = e?.message || String(e);
        // do NOT wipe the chart on transient errors; that would feel “jumpy”
        // candleSeries.setData([]);
        // volumeSeries.setData([]);
    }
}