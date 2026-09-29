# TrabalhoPOO da Sistema de Biblioteca

Sistema de biblioteca em Java, desenvolvido para a disciplina de Programação Orientada a Objetos. Permite cadastrar livros, revistas e usuários, listar o acervo e controlar empréstimos e devoluções, com prazo de devolução e liberação automática do item. O sistema é usado por uma interface gráfica feita em Swing.

## Como executar

No IntelliJ, abra a pasta do projeto e execute a classe `interfacegrafica.MainGUI`.

Pela linha de comando (Git Bash), na raiz do repositório:

```
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out interfacegrafica.MainGUI
```

Não é preciso instalar nenhuma biblioteca: o Swing já vem junto com o Java.

## Estrutura de pacotes

O código é separado por responsabilidade:

```
src/
  modelo/            ItemBiblioteca, Livro, Revista, Usuario, Emprestimo,
                     StatusEmprestimo, ItemIndisponivelException
  servico/           Biblioteca
  interfacegrafica/  MainGUI
```

| Pacote | Responsabilidade |
|---|---|
| `modelo` | As classes do domínio. Cada uma guarda seus dados e valida as próprias regras (código, título, ISBN, e-mail etc.). |
| `servico` | A `Biblioteca`, que guarda as listas e aplica as regras de negócio: cadastro sem repetição, empréstimo e devolução. |
| `interfacegrafica` | A tela. Só lê os campos, chama a `Biblioteca` e mostra os resultados e os erros. Não tem regra de negócio. |

A dependência vai em um sentido só: `interfacegrafica` usa `servico` e `modelo`, `servico` usa `modelo`, e `modelo` não depende de ninguém.

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
| Interfaces | Os botões da `MainGUI` usam a interface `ActionListener` do Java: cada botão recebe uma classe anônima que implementa o método `actionPerformed()`. |
| Enum | `StatusEmprestimo` (`ATIVO`, `DEVOLVIDO`, `EXPIRADO`) guarda a situação de cada `Emprestimo`, no lugar de um `boolean`. |
| Tratamento de exceções | As classes do `modelo` e a `Biblioteca` lançam `IllegalArgumentException` para dados inválidos e a exceção própria `ItemIndisponivelException` para item já emprestado. A `MainGUI` trata tudo com `try/catch` e mostra a mensagem na tela. |
| Interface gráfica | Feita em Swing (`MainGUI`), com abas, formulários, botões, listas e caixas de mensagem (`JOptionPane`). |
| Data e hora | `Emprestimo` usa `LocalDateTime` para registrar o empréstimo e a devolução, `ChronoUnit` para calcular o prazo de 2 dias e `DateTimeFormatter` para exibir as datas no formato `dd/MM/yyyy HH:mm`. |
