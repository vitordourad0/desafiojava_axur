# HtmlAnalyzer

## Compilação

```bash
javac HtmlAnalyzer.java
```

## Execução

```bash
java HtmlAnalyzer <URL>
```

**Exemplo:**
```bash
java HtmlAnalyzer http://hiring.axreng.com/internship/example1.html
```

## Como funciona

O programa recebe uma URL via linha de comando, busca o conteúdo HTML e identifica o trecho de texto que está no nível mais profundo da estrutura de tags.

### Algoritmo

1. **Busca HTTP** — conecta-se à URL fornecida e lê o corpo da resposta como texto UTF-8.
2. **Classificação de linhas** — cada linha é classificada como:
   - Tag de abertura (ex: `<div>`)
   - Tag de fechamento (ex: `</div>`)
   - Trecho de texto
3. **Rastreamento de profundidade** — uma pilha (`Deque`) mantém as tags abertas. A profundidade atual é incrementada a cada tag de abertura e decrementada a cada tag de fechamento.
4. **Seleção do texto mais profundo** — quando um trecho de texto é encontrado, sua profundidade é comparada com a máxima registrada até então. Se for maior, ele vira o novo candidato. Em caso de empate, o primeiro encontrado é mantido.

### Detecção de HTML mal-formado (bônus)

A solução detecta as seguintes situações e retorna `malformed HTML`:

- Tag de fechamento que não corresponde à tag de abertura mais recente na pilha.
- Tag de fechamento quando a pilha está vazia.
- Tags com atributos (ex: `<a href="...">`).
- Tags auto-fechadas (ex: `<br/>`).
- Tags ainda abertas ao final do documento (pilha não vazia).
- Linhas com caracteres `<` ou `>` que não formam uma tag válida.

## Saídas possíveis

| Situação | Output |
|---|---|
| Texto mais profundo encontrado | O próprio trecho de texto |
| HTML mal-formado | `malformed HTML` |
| Falha de conexão | `URL connection error` |
