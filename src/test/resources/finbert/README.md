# FinBERT test files

The tests also use FinBERT's `vocab.txt` and `config.json`, which live in
`src/main/resources/finbert/` (copied from
[Xenova/finbert](https://huggingface.co/Xenova/finbert) at commit
`8f269abebfdd9009d7d9b5e96af7e5c6bfe50b20`; FinBERT by ProsusAI, CC BY-NC-SA 4.0). Java puts
main resources on the classpath during tests too, so they are not copied here.

- `reference.json`: what Hugging Face's own Python libraries produced for the same files,
  used as the expected answers in the Java tests:
  - `model_cases`: headlines with their tokens, token ids, the model's three scores and the
    label, from Python `tokenizers` 0.23.3 and `onnxruntime` 1.32.0 running
    `onnx/model_quantized.onnx` one headline at a time.
  - `tokenizer_cases`: awkward text (accents, curly quotes, Chinese characters, emoji, tabs,
    a 120-letter word, full-width letters, zero-width characters) with Python's tokens and ids.

The headlines are made up ("ACME" is a fictional company).
