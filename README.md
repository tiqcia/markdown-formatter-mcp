<!--
Markdown Cheatsheet
https://github.com/adam-p/markdown-here/wiki/markdown-cheatsheet
-->

## Formatação e Exportação de Markdown 

Este projeto implementa um servidor e um cliente baseados no Protocolo de Contexto do Modelo ([MCP](https://modelcontextprotocol.io/docs/2026-07-28/getting-started/intro) - *Model Context Protocol*) utilizando o [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) e transporte STDIO, disponibilizando ferramentas para correção automática e exportação de documentos Markdown (`.md`) para PDF.

---

### Ferramentas Disponíveis

O servidor disponibiliza três ferramentas via protocolo MCP:

#### 1. `LintMarkdown`
Realiza uma análise estática de sintaxe e estrutura em um arquivo Markdown (`.md`) existente no disco, retornando um relatório de erros de sintaxe sem modificar ou criar arquivos.
* **Regras verificadas:**
  * `MD012`: Múltiplas linhas em branco consecutivas.
  * `MD018`: Falta de espaço após o caractere `#` em cabeçalhos (ex.: `#Titulo`).
  * `MD042`: Links sem URL de destino (ex.: `[texto]()`).
  * `MD055` / `MD056`: Tabelas sem delimitadores `|` nas extremidades, separadores inválidos (menos de 3 traços `---`), linhas em branco interrompendo uma tabela ou divergência na quantidade de colunas.
* **Parâmetros de entrada (`inputSchema`):**
  * `filePath`: caminho completo do arquivo Markdown (`.md`) a ser auditado.
* **Possíveis resultados:**
  * Lista enumerada contendo o número da linha, o código da regra violada e a descrição do problema encontrado (ex.: `- Linha 1 [MD018]: Falta espaço após '#' no cabeçalho.`).
  * Mensagem informando que nenhum problema foi encontrado, caso o arquivo esteja em conformidade.
* **Possíveis erros:**
  * `O caminho do arquivo não pode ser vazio.` (quando `filePath` é nulo ou em branco).
  * `Arquivo não encontrado: <caminho>` (quando o caminho não existe ou não corresponde a um arquivo regular).

#### 2. `FormatMarkdownFile`
Corrige automaticamente problemas de sintaxe e estrutura de tabelas de um arquivo Markdown (`.md`), salvando o resultado em uma nova cópia (`<nome>_formatado.md`) no mesmo diretório para preservar o arquivo original.
* **Correções aplicadas:**
  * Adiciona espaço obrigatório após `#` em cabeçalhos (`MD018`).
  * Reduz múltiplas linhas em branco consecutivas para apenas uma linha em branco (`MD012`).
  * Formatação de tabelas (`TablePrettifier`): Reconstrói tabelas malformadas (MD055 / MD056), adicionando delimitadores | nas bordas, normalizando separadores com no mínimo 3 traços (---, :---, ---:, :---:) e preenchendo colunas faltantes.
* **Parâmetros de entrada (`inputSchema`):**
  * `filePath`: caminho completo do arquivo Markdown (`.md`) de entrada.
* **Possíveis resultados:**
  * Criação do arquivo `<nome>_formatado.md` no mesmo diretório do arquivo original e retorno textual contendo o caminho do novo arquivo e o relatório de erros corrigidos (ou aviso de que o arquivo original já estava sem problemas).
* **Possíveis erros:**
  * `O caminho do arquivo não pode ser vazio.`
  * `Arquivo não encontrado: <caminho>`
  * Erros de leitura/escrita (I/O) caso não haja permissão no diretório de destino.

#### 3. `ExportMarkdownToPdf`
Converte um arquivo Markdown (`.md`) diretamente em um documento PDF estilizado (`<nome>.pdf`) salvo no mesmo diretório do arquivo de entrada, utilizando **Flexmark** (para conversão de Markdown e tabelas em HTML) e **OpenHTMLtoPDF** (para renderização do HTML com folha de estilos CSS em PDF).
* **Parâmetros de entrada (`inputSchema`):**
  * `filePath`: caminho completo do arquivo Markdown (`.md`) a ser convertido para PDF.
* **Possíveis resultados:**
  * Criação do arquivo `<nome>.pdf` no mesmo diretório do arquivo original e retorno textual confirmando o sucesso da operação e o caminho completo do PDF gerado.
* **Possíveis erros:**
  * `O caminho do arquivo não pode ser vazio.`
  * `Arquivo não encontrado: <caminho>`
  * Erros de gravação (I/O) caso o arquivo `.pdf` de destino esteja aberto/bloqueado por outro programa durante a exportação.

---

> As três ferramentas são independentes e podem ser chamadas isoladamente. Você pode converter um arquivo .md diretamente para PDF com export_markdown_to_pdf, mas, caso o documento contenha erros de sintaxe ou tabelas quebradas, o PDF será gerado com esses erros.

---

### Executar a aplicação

#### Usando o Eclipse IDE
1. **Gerar o Servidor:** Clique com o botão direito no projeto > **Run As** > **Maven build...**, digite `clean package -Pserver -DskipTests` em **Goals** e clique em **Run** *(cria o arquivo `server/mcp_server01_server.jar`)*.
2. **Gerar o Cliente:** Repita o passo anterior usando `package -Pclient -DskipTests` em **Goals** *(cria o arquivo `target/dist-client/mcp_server01_client.jar`)*.
3. **Executar:** Abra o terminal na raiz do projeto e rode `java -jar target/dist-client/mcp_server01_client.jar`.

#### Usando o Terminal
Na pasta raiz do projeto, execute os três comandos em sequência:

```bash
# 1. Cria o executável do servidor
mvn clean package -Pserver -DskipTests

# 2. Cria o executável do cliente
mvn package -Pclient -DskipTests

# 3. Inicia o cliente no terminal (que liga o servidor automaticamente)
java -jar target/dist-client/mcp_server01_client.jar