# TrabalhoPOO da Sistema de Biblioteca

Sistema de biblioteca em Java, desenvolvido para a disciplina de Programação Orientada a Objetos. Permite cadastrar livros, revistas e usuários, listar o acervo e controlar empréstimos e devoluções, com prazo de devolução e liberação automática do item. O sistema é usado por uma interface gráfica feita em Swing.


## Como executar

O Sistema Biblioteca pode ser executado em qualquer IDE compatível com Java, como IntelliJ IDEA, Eclipse, NetBeans ou Visual Studio Code, desde que o ambiente esteja configurado corretamente.

### Requisitos

- JDK compatível com a versão Java utilizada no projeto.
- Driver JDBC `org.xerial:sqlite-jdbc:3.54.1.0`.
- Arquivo `sql/schema.sql`, disponível no repositório.

### Passos para execução

1. Abra o projeto na IDE de sua preferência.
2. Configure o driver SQLite JDBC nas dependências do projeto, caso ainda não esteja disponível.
3. Na primeira execução, inicialize as tabelas do banco `biblioteca.db` utilizando o script `sql/schema.sql`.
4. Execute a classe `interfacegrafica.MainGUI`.

**Observações:**

- Não é necessário instalar um servidor de banco de dados, pois o SQLite funciona diretamente com arquivos locais.
- O banco mantém os dados cadastrados mesmo após fechar a aplicação.
- A criação automática das tabelas ainda está em desenvolvimento.


Não é preciso instalar nenhuma biblioteca: o Swing já vem junto com o Java.


## Estrutura de pacotes

O projeto está organizado em pacotes, separando as entidades, as regras de negócio, o armazenamento dos dados e a interface gráfica.

```text
src/
  modelo/
    ItemBiblioteca.java
    Livro.java
    Revista.java
    Usuario.java
    Emprestimo.java
    StatusEmprestimo.java
    ItemIndisponivelException.java

  servico/
    Biblioteca.java
    RepositorioBiblioteca.java
    RepositorioBibliotecaMemoria.java
    RepositorioBibliotecaSQLite.java
    ConexaoSQLite.java

  interfacegrafica/
    MainGUI.java
```

| Pacote | Responsabilidade |
|---|---|
| `modelo` | Contém as entidades do sistema e suas validações, como `ItemBiblioteca`, `Livro`, `Revista`, `Usuario` e `Emprestimo`. |
| `servico` | Contém as regras de negócio, a interface `RepositorioBiblioteca`, suas implementações em memória e SQLite e o gerenciamento das conexões com o banco. |
| `interfacegrafica` | Contém a interface Swing, responsável pela interação com o usuário, cadastros, consultas e apresentação dos resultados. |

### Organização arquitetural

O sistema utiliza uma separação de responsabilidades inspirada na **Clean Architecture** e nos princípios **SOLID**.

A classe `Biblioteca` depende da interface `RepositorioBiblioteca`, permitindo utilizar diferentes implementações de armazenamento sem alterar as regras de negócio.

Atualmente, estão disponíveis duas implementações:

- **`RepositorioBibliotecaMemoria`:** armazena os dados temporariamente durante a execução, sendo útil para testes.
- **`RepositorioBibliotecaSQLite`:** utiliza o SQLite para armazenar os dados permanentemente.

A classe `ConexaoSQLite` centraliza a abertura das conexões com o banco de dados e a ativação das chaves estrangeiras.

Essa organização aplica o princípio de **Inversão de Dependência (DIP)**, reduzindo o acoplamento entre as regras de negócio e a tecnologia de persistência.


## Classes principais

`ItemBiblioteca`: classe abstrata com código, título e disponibilidade.\
`Livro`: item com autor e ISBN.\
`Revista`: item com número da edição.\
`Usuario`: pessoa cadastrada na biblioteca.\
`Emprestimo`: liga um item a um usuário, com data do empréstimo, prazo, data da devolução e status.\
`StatusEmprestimo`: enum com os estados do empréstimo (`ATIVO`, `DEVOLVIDO`, `EXPIRADO`).\
`ItemIndisponivelException`: exceção própria, lançada ao tentar emprestar um item que já está emprestado.\
`Biblioteca`: guarda as listas e aplica as regras de negócio.\
`MainGUI`: interface gráfica em Swing.

## Interface gráfica

A janela (`MainGUI`, que estende `JFrame`) tem três abas (`JTabbedPane`). Em cada aba há um formulário em cima e, embaixo, a lista do que está cadastrado:

| Aba | Campos | Botões | Lista |
|---|---|---|---|
| Itens | Código, Título, Autor, ISBN, Edição | Cadastrar livro, Cadastrar revista | Livros e revistas, com a situação (disponível ou emprestado) |
| Usuários | ID, Nome, E-mail | Cadastrar usuário | Usuários cadastrados |
| Empréstimos | Código do item, ID do usuário | Fazer empréstimo, Fazer devolução | Empréstimos com data, prazo, status e data da devolução |

Como funciona:
Cada botão tem um `ActionListener`, escrito como classe anônima, que chama um método da tela, por exemplo `cadastrarLivro()`.
Cada método lê os campos, cria o objeto do `modelo` e chama a `Biblioteca`. Se der certo, mostra uma mensagem, limpa os campos e atualiza as listas (`atualizarListas()`).
Se algum dado for inválido, a classe do `modelo` ou a `Biblioteca` lança uma exceção, e a tela mostra a mensagem num `JOptionPane`. A `ItemIndisponivelException` aparece num aviso próprio, "Item indisponível".
Campos numéricos (ID e edição) são convertidos com `Integer.parseInt` no método `lerInteiro()`, que avisa quando o texto não é um número.
Ao fazer um empréstimo, a mensagem de confirmação mostra a data do empréstimo e o prazo de devolução.

## Regras de negócio

O código do item é único no acervo e só aceita números.\
O título não pode ser vazio (aceita números, como "1984").\
O autor só aceita letras e espaços.\
O ISBN do livro tem exatamente 13 dígitos.\
O número da edição da revista deve ser maior que zero.\
O ID do usuário é único e deve ser maior que zero, o nome não pode ser vazio e o e-mail deve conter "@gmail.com".\
Só é possível emprestar um item existente, para um usuário cadastrado, e que esteja disponível.\
O prazo de cada empréstimo é de 2 dias. Ao vencer, o empréstimo passa a `EXPIRADO` e o item volta a ficar disponível automaticamente.\
A devolução é feita pelo código do item, exige um empréstimo ativo e registra a data da devolução.\
O histórico de empréstimos é mantido, inclusive dos devolvidos ou expirados.

## Checklist de conceitos
|Conceito | Onde foi utilizado |
|---|---|
| Classes e objetos | `Livro`, `Revista`, `Usuario`, `Emprestimo` e `Biblioteca` são instanciados na `MainGUI` e dentro de `Biblioteca`. |
| Atributos de classe | Cada classe guarda o estado relevante ao seu papel, como `codigo`, `titulo` e `disponivel` em `ItemBiblioteca`, `autor` e `isbn` em `Livro`, e `dataEmprestimo`, `dataDevolucao` e `status` em `Emprestimo`. |
| Métodos de classe | Cada comportamento fica na classe responsável: `emprestar()` e `devolver()` em `ItemBiblioteca` e `Emprestimo`, e `adicionarItem()`, `emprestar()` e `devolver()` em `Biblioteca`. |
| Construtores | `Livro`, `Revista`, `Usuario` e `Emprestimo` exigem todos os dados obrigatórios na criação e validam esses dados, o que impede objetos incompletos ou inválidos. |
| Palavra `this` | Usada nos construtores e setters para diferenciar o atributo do parâmetro, como em `this.codigo = codigo`. |
| Modificadores | Atributos `private`, métodos de uso externo `public`, construtor de `ItemBiblioteca` `protected` e métodos de validação `private static`. |
| Encapsulamento | O estado só muda por métodos controlados: a disponibilidade do item por `emprestar()` e `devolver()`, os dados pelos setters que validam, e as listas por `adicionarItem()` e `adicionarUsuario()`. Os getters da `Biblioteca` devolvem cópias das listas. |
| Pacotes Java / API | Código separado por responsabilidade em `modelo`, `servico` e `interfacegrafica`. Da API do Java são usados `java.util` (`List`, `ArrayList`), `java.time` (`LocalDateTime`), `javax.swing` e `java.awt`. |
| Herança | `Livro` e `Revista` estendem `ItemBiblioteca`; `MainGUI` estende `JFrame`; `ItemIndisponivelException` estende `RuntimeException`. |
| Polimorfismo | `Biblioteca` guarda uma `List<ItemBiblioteca>` com livros e revistas e chama `getTipo()` e `toString()` sem conhecer o tipo concreto. |
| Palavra `super` | Os construtores de `Livro` e `Revista` chamam `super(codigo, titulo)`, e o `toString()` deles reaproveita `super.toString()`. A exceção própria chama `super(mensagem)`. |
| Abstração | `ItemBiblioteca` é abstrata e declara `getTipo()`, então não pode ser instanciada diretamente. |
| Interfaces | **Interfaces:** o projeto utiliza `RepositorioBiblioteca`, uma interface que define as operações de armazenamento. Ela possui duas implementações: `RepositorioBibliotecaMemoria` e `RepositorioBibliotecaSQLite`, demonstrando abstração e inversão de dependência (DIP).|
| Enum | `StatusEmprestimo` (`ATIVO`, `DEVOLVIDO`, `EXPIRADO`) guarda a situação de cada `Emprestimo`, no lugar de um `boolean`. |
| Tratamento de exceções | As classes do `modelo` e a `Biblioteca` lançam `IllegalArgumentException` para dados inválidos e a exceção própria `ItemIndisponivelException` para item já emprestado. A `MainGUI` trata tudo com `try/catch` e mostra a mensagem na tela. |
| Interface gráfica | Feita em Swing (`MainGUI`), com abas, formulários, botões, listas e caixas de mensagem (`JOptionPane`). |
| Data e hora | `Emprestimo` usa `LocalDateTime` para registrar o empréstimo e a devolução, `ChronoUnit` para calcular o prazo de 2 dias e `DateTimeFormatter` para exibir as datas no formato `dd/MM/yyyy HH:mm`. |
## SOLID aplicado

- **SRP:** `Biblioteca` concentra as regras de negócio; `RepositorioBibliotecaMemoria` cuida do armazenamento; `MainGUI` cuida da interação com o usuário.
- **OCP:** o serviço trabalha com a interface `RepositorioBiblioteca`, permitindo outra implementação sem alterar as regras.
- **LSP:** `Livro` e `Revista` continuam substituíveis por `ItemBiblioteca`.
- **ISP:** a interface de armazenamento contém somente as operações necessárias para o serviço.
- **DIP:** `Biblioteca` recebe `RepositorioBiblioteca` pelo construtor, em vez de criar ou conhecer diretamente a implementação concreta.

## Clean Code aplicado

- nomes de classes, métodos e variáveis expressivos;
- métodos menores e com uma responsabilidade;
- validações preservadas nas entidades;
- tratamento de erros centralizado nos fluxos da interface;
- `StringBuilder` para montar listagens;
- execução da interface pela `SwingUtilities.invokeLater`;
- reutilização de métodos auxiliares para campos, botões, limpeza e operações;
- cópias das listas retornadas pelo repositório para evitar exposição do estado interno.

## Funcionalidades preservadas

- cadastro de livros, revistas e usuários;
- listagem do acervo e dos usuários;
- empréstimo e devolução;
- histórico de empréstimos durante a execução;
- validação dos dados e mensagens de erro.

## Persistência de dados

O Sistema Biblioteca utiliza SQLite para manter os registros de livros, revistas, usuários e empréstimos entre as execuções da aplicação.

Os dados são armazenados localmente no arquivo `biblioteca.db`, que não é versionado no GitHub. A estrutura do banco está disponível no arquivo `sql/schema.sql`, permitindo a criação das tabelas em outros ambientes.

A implementação `RepositorioBibliotecaMemoria` foi preservada como alternativa para testes sem persistência.
