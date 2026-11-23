# Finnhub test responses

`company_news_sample.json` is a **made-up** response in Finnhub's documented
`company-news` format, for a fictional company "ACME", so the parser tests need no
network and no API key. It includes broken items on purpose (no headline, no time).

To check the parser against the real thing, save a real response next to it, e.g.:

    curl -H "X-Finnhub-Token: $FINNHUB_API_KEY" \
      "https://finnhub.io/api/v1/company-news?symbol=NVDA&from=2026-10-01&to=2026-10-31" \
      > company_news_nvda.json
