// 비트코인
async function fetchBitcoin() {
    const res = await fetch("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=krw&include_24hr_change=true");
    const data = await res.json();
    const price = data.bitcoin.krw;
    const change = data.bitcoin.krw_24h_change;

    document.getElementById("btc-price").textContent = price.toLocaleString() + " KRW";
    const changeEl = document.getElementById("btc-change");
    changeEl.textContent = change.toFixed(2) + "%";
    changeEl.className = "change " + (change >= 0 ? "up" : "down");
    document.getElementById("btc-time").textContent = new Date().toLocaleTimeString();
}

// 금
async function fetchGold() {
    const res = await fetch("https://api.metals.live/v1/spot/gold");
    const data = await res.json();
    const priceUSD = data[0].price;
    const krwPerUSD = 1400;
    const priceKRW = priceUSD * krwPerUSD / 31.1035;
    document.getElementById("gold-price").textContent = Math.round(priceKRW).toLocaleString() + " KRW/g";
    document.getElementById("gold-change").textContent = "-";
    document.getElementById("gold-time").textContent = new Date().toLocaleTimeString();
}

// 원유
async function fetchOil() {
    const res = await fetch("https://query1.finance.yahoo.com/v7/finance/quote?symbols=CL=F");
    const data = await res.json();
    const quote = data.quoteResponse.result[0];
    const changeEl = document.getElementById("oil-change");

    document.getElementById("oil-price").textContent = quote.regularMarketPrice.toFixed(2) + " USD";
    changeEl.textContent = quote.regularMarketChangePercent.toFixed(2) + "%";
    changeEl.className = "change " + (quote.regularMarketChangePercent >= 0 ? "up" : "down");
    document.getElementById("oil-time").textContent = new Date().toLocaleTimeString();
}

// 병렬 실행
async function updateAll() {
    await Promise.all([fetchBitcoin(), fetchGold(), fetchOil()]);
}
updateAll();
setInterval(updateAll, 60000);
