# Backlog — Sistema Biblioteca | Trabalho 2 de POO

**Objetivo:** evoluir o Sistema Biblioteca em Java para uma aplicação com interface consistente, persistência em banco de dados e documentação executável e navegável.

**Como usar:** marque uma tarefa quando seu critério de aceite estiver atendido na versão que será entregue. O backlog parte do enunciado do professor Silvio e da versão discutida neste chat, não de uma auditoria da branch final do grupo.

## Prioridades

| Ordem | Frente de trabalho | Resultado esperado |
| --- | --- | --- |
| 1 | Interface e banco, em paralelo | Definir o padrão visual e concluir um fluxo de ponta a ponta de cadastro, consulta e edição de um item persistido |
| 2 | Funcionalidades restantes e arquitetura | Fechar os fluxos de usuários, empréstimos e devoluções, e verificar as regras sem a GUI |
| 3 | Revisão e entrega | Testar a partir de uma cópia limpa, gerar JavaDoc HTML e reunir README e capturas |

A interface vale **3,0 dos 10,0 pontos** e é o principal foco do trabalho. Swing já é uma opção válida para o projeto. A rubrica não exige migração para JavaFX.

## Backlog por critério da rubrica

### 1. Checklist do Trabalho 1 — 1,0

- [ ] Conferir todos os itens do checklist original na versão final e corrigir as pendências. Preservar encapsulamento, herança, abstração, polimorfismo, exceções e enumerações já usados no projeto.
- [ ] Revisar o requisito de interfaces do Trabalho 1. Registrar o papel de `RepositorioBiblioteca` e conferir se o grupo também precisa demonstrar um comportamento de domínio substituível conforme o checklist original.
- [ ] Atualizar no README onde cada requisito pode ser encontrado.

**Aceite:** o checklist original foi revisado e cada item atendido tem um exemplo verificável no código ou na execução.

### 2. SOLID — SRP e OCP — 1,0

- [ ] Manter a apresentação na `MainGUI`, as regras nas entidades ou no serviço e o armazenamento nos repositórios.
- [ ] Demonstrar uma extensão por abstração ou polimorfismo. Exemplo: trocar o repositório em memória pelo de banco alterando apenas a montagem da aplicação, sem reescrever as regras de `Biblioteca`. Se o contrato atual precisar mudar, estabilizá-lo antes de demonstrar a troca.

**Aceite:** há um exemplo concreto de separação de responsabilidades e uma extensão demonstrável que preserva o código consumidor.

### 3. SOLID — LSP, ISP e DIP — 1,0

- [ ] **LSP:** verificar que `Livro` e `Revista` respeitam o contrato de `ItemBiblioteca`. Conferir também se as implementações de repositório têm comportamento compatível em buscas, listagens e operações de gravação.
- [ ] **ISP:** conferir o que cada cliente usa. Se clientes distintos precisarem apenas de itens, usuários ou empréstimos, expor contratos adequados a cada necessidade.
- [ ] **DIP:** manter o serviço dependente do contrato `RepositorioBiblioteca` e fornecer a implementação concreta de fora, pelo construtor.

**Aceite:** o README aponta um exemplo real de cada princípio e explica o contrato demonstrado.

### 4. Clean Architecture — 1,0

- [ ] Separar entidades (`modelo`), casos de uso (`servico`), apresentação (`interfacegrafica`) e detalhes externos (implementação de persistência).
- [ ] Manter os contratos usados pelo serviço no núcleo. Não importar Swing ou JDBC nas entidades nem no serviço que coordena as regras. Montar as dependências no ponto de entrada.
- [ ] Executar um cenário de cadastro, empréstimo e devolução sem abrir a interface, usando teste ou um executável simples em memória.

**Aceite:** o núcleo roda sem a GUI e não conhece detalhes do SGBD. A interface e o adaptador de persistência dependem do núcleo, não o contrário.

### 5. Clean Code — 1,0

- [ ] Revisar nomes, formatação e métodos com responsabilidades misturadas. Remover duplicações relevantes de validação, tratamento de ações ou construção de componentes.
- [ ] Manter mensagens compreensíveis para o usuário e preservar a causa técnica dos erros de persistência sem expor esses detalhes na tela.
- [ ] Revisar efeitos colaterais pouco óbvios. Exemplo: na versão em memória, consultar o status de um empréstimo pode atualizar sua situação e liberar o item. Deixar essa operação explícita ou documentar claramente esse comportamento.

**Aceite:** uma revisão de código encontra nomes claros, métodos focados, formatação consistente e falhas que podem ser diagnosticadas.

### 6. Interface — identidade visual — 1,0

- [ ] Definir paleta, tipografia, hierarquia e estados de sucesso, aviso e erro.
- [ ] Aplicar o mesmo padrão de botões, campos, tabelas e mensagens às telas de itens, usuários e empréstimos.

**Aceite:** as telas parecem parte da mesma aplicação e as informações e ações principais são fáceis de identificar.

### 7. Interface — layout — 1,0

- [ ] Organizar formulários e listas com alinhamento, margens e espaçamentos consistentes. Mostrar campos relevantes ao tipo escolhido, como ISBN para livro e edição para revista.
- [ ] Testar a janela no tamanho padrão e no menor tamanho suportado. Garantir rolagem quando necessária e ausência de cortes ou sobreposições.

**Aceite:** os fluxos principais continuam legíveis e utilizáveis ao redimensionar a janela.

### 8. Interface — usabilidade e funcionamento — 1,0

- [ ] Permitir pela tela cadastrar e consultar registros, editar ou excluir quando couber ao tema, emprestar e devolver.
- [ ] Validar entradas e exibir feedback claro de sucesso e erro. Conferir que a listagem reflete cada operação concluída.
- [ ] Testar navegação com Tab e Shift+Tab e manter o foco visível. Executar operações demoradas fora da thread de eventos do Swing e atualizar a tela na thread correta.

**Aceite:** é possível concluir os fluxos sem terminal. A navegação funciona por teclado e a janela continua responsiva durante uma operação demorada.

### 9. Banco de dados e persistência — 1,0

- [ ] Definir e documentar o SGBD. Entregar script ou inicialização automática que crie o esquema de itens, usuários e empréstimos.
- [ ] Implementar o repositório de banco e usar consultas parametrizadas, por exemplo com `PreparedStatement` em JDBC. Não concatenar entradas do usuário ao SQL.
- [ ] Conectar cadastro, consulta e alterações ou exclusões pertinentes às telas. Definir como preservar o histórico ao excluir um registro vinculado a empréstimos.
- [ ] Gravar explicitamente as mudanças de empréstimo, devolução e vencimento que afetem registros persistidos. Garantir consistência entre disponibilidade e empréstimo, usando transação quando houver gravações relacionadas. Definir se vencer o prazo libera o item ou apenas o marca como atrasado.
- [ ] Criar dados pela tela, fechar o programa, reabrir e conferir os registros e estados. Repetir a criação do banco em uma instalação limpa.

**Aceite:** o banco pode ser recriado a partir da entrega, as consultas são parametrizadas e os dados sobrevivem ao reinício.

### 10. JavaDoc — 1,0

- [x] Documentar no código os métodos do pacote `servico` (commit `1a750a9`) e o pacote `modelo` (commit `623726f`), com envio confirmado à `main`.
- [x] Documentar a classe, o construtor e o `main()` de `MainGUI` (commit local `ab03845`).
- [ ] Confirmar que a documentação da `MainGUI` está na versão final. Completar também a documentação das classes, interfaces, construtores e métodos públicos acrescentados ou alterados na versão com banco.
- [ ] Conferir responsabilidades, contratos e tags `@param`, `@return` e `@throws` quando aplicáveis.
- [ ] Gerar o JavaDoc HTML a partir da versão final. Abrir o `index.html`, conferir a navegação e incluir o diretório completo na entrega.

**Aceite:** o HTML abre e documenta o código que o professor vai executar, inclusive as partes novas da persistência.

## Checklist da entrega

- [ ] Código-fonte completo e executável, com dependências e instruções suficientes para reproduzir a execução.
- [ ] Capturas das telas principais na versão final.
- [ ] JavaDoc HTML completo e navegável.
- [ ] README com integrantes, pré-requisitos, instruções de execução, configuração do banco e exemplos de onde os requisitos foram atendidos.
- [ ] Script ou mecanismo automático de criação do banco incluído e testado.
- [ ] Validar tudo a partir de uma cópia limpa do projeto e conferir o prazo e o canal divulgados na disciplina.
