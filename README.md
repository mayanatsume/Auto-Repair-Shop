Auto repair shop - JavaFx - Java - Ocalm
📋 Visão Geral do projeto utiliza duas camadas principais:

1. Back-end em OCaml para lógica de negócio, processamento de dados e geração de orçamentos:
...
2. Front-end em Java (JavaFX) para interface gráfica:

A UI foi implementada em Java 11+, utilizando JavaFX como framework de componentes gráficos.

Cada botão na tela invoca comandos OCaml via IntegradorOCaml e exibe tabelas ou resumos.
Este projeto integra:

OCaml para processamento de dados e geração de orçamentos:
main.ml com comandos:
listar_items — lista inventário.
top_n <n> — top‑n itens mais lucrativos.
listar_servicos — lista serviços disponíveis.
orcamento_items <ids> — seleciona peças mais lucrativas por serviço.
orcamento_mecanico <ids> — calcula mão de obra com descontos por tempo.
orcamento_desconto_items <ids> — aplica descontos de marca às peças.
orcamento_preco_fixo <ids> — mostra preços fixos de serviços.
listar_descontos — lista todos os itens com preço final, desconto e lucro.

Módulos auxiliares:

calculo_maoobra.ml — funções de cálculo de mão de obra.
calculo_desconto.ml — funções de cálculo de desconto de peças.
JavaFX (FXMain.java) para UI:
Botões para chamar cada comando OCaml.
Exibição de tabelas (TableView) ou área de texto (TextArea).
Consolidação final do carrinho: soma de peças, mão de obra e preço fixo.

⚙️ Instalação & BuildPré-requisitos: OCaml, Java 11+, JavaFX SDK.
1. OCaml (no diretório ocaml/)# Compila módulos
ocamlc -c calculo_maoobra.ml
ocamlc -c calculo_desconto.ml

# Compila e linka main.exe
make clean && make2. UI JavaFX (na raiz do projeto)# Ajuste a variável do JavaFX SDK
export PATH_TO_FX="/caminho/para/javafx-sdk-XX/lib"
1. OCaml (no diretório ocaml/)
# Compila módulos
ocamlc -c calculo_maoobra.ml
ocamlc -c calculo_desconto.ml
# Compila e linka main.exe
make clean && make

2. UI JavaFX (na raiz do projeto)
# Ajuste a variável do JavaFX SDK (se ainda não tiver feito)
export PATH_TO_FX="/caminho/para/javafx-sdk-XX/lib"

# Torna o script executável e executa a UI
chmod +x run_ui.sh
./run_ui.sh

# Compila e roda UI
./run_ui.sh🎯 

Uso na UIListar Itens: exibe toda a lista de inventário.
Peças Mais Lucrativas: tabela de peças por serviço.
Top‑10 Mais Lucrativas: top 10 por lucro.
Cálculo Mão de Obra: informe IDs (ex.: 1,2) → tabela com horas, desconto e total.
Descontos nas Peças: lista completa de itens com preço original, % desconto, valor desconto e lucro.
Carrinho Consolidado: informe IDs → lista detalhada de peças, totais e total geral.
Sair: fecha a aplicação.

📑 Relatório de Decisões
Busca por ID no Carrinho e Mão de ObraFlexibilidade: permite ao usuário escolher apenas os serviços que realmente contratou, em vez de executar todos.
Clareza: IDs apontam unicamente para serviços na base (database.pl), tornando o fluxo explícito.

UI Consistente: usa TextInputDialog para qualquer operação que precise de seleção dinâmica.
Modularizaçãocalculo_maoobra.ml: isola lógica de descontos temporais, facilitando manutenção e testes.

calculo_desconto.ml: centraliza regras de desconto por marca, evitando duplicação.
Escolha de ApresentaçãoTabelas (TableView) para dados estruturados (listagens, orçamentos parciais).

Área de texto para o resumo final do carrinho, onde o formato livre (listas e blocos) é mais legível.
