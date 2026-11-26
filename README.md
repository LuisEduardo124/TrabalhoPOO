# Sistema Biblioteca --- Trabalho 2 de POO

Sistema de gestão de acervo de biblioteca, desenvolvido em Java para a
disciplina de Programação Orientada a Objetos.

A aplicação permite cadastrar, consultar, editar e excluir livros,
revistas e usuários, além de registrar empréstimos e devoluções. Cada
empréstimo possui prazo de dois dias, e o sistema controla a situação do
empréstimo e a disponibilidade dos itens.

A interface gráfica foi desenvolvida com **Java Swing**, com modos claro
e escuro. Os dados são armazenados em **SQLite**, com inicialização
automática da estrutura do banco.

## Integrantes

-   Luis Felipe
-   Luis Eduardo
-   Murilo Dias
-   Vinicius Sanchotene
-   Pietro Zinelli
-   Emanuel Villa

## Requisitos

  Requisito           Versão / descrição
  ------------------- ------------------------------------------------
  Java                **JDK 17 ou superior**
  Banco de dados      SQLite
  Driver JDBC         `sqlite-jdbc-3.54.1.0.jar`, incluído em `lib/`
  Interface gráfica   Java Swing

O código-fonte foi compilado com sucesso usando `javac --release 17`.
Isso confirma a compatibilidade da compilação com Java 17. O projeto não
possui `pom.xml` nem `build.gradle`; a dependência SQLite é fornecida
pelo JAR incluído no repositório.

Não é necessário instalar um servidor de banco de dados. O SQLite
utiliza um arquivo local. O Swing já faz parte do JDK, portanto não é
necessário instalar JavaFX para executar esta aplicação.

## Como executar

Execute os comandos a partir da **pasta raiz do projeto**, aquela que
contém `src/`, `lib/` e `sql/`.

### Windows --- PowerShell

Crie a pasta de saída e compile:

``` powershell
New-Item -ItemType Directory -Force bin | Out-Null

$arquivos = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }

javac --release 17 -encoding UTF-8 -cp "lib\sqlite-jdbc-3.54.1.0.jar" -d bin $arquivos
```

Execute a aplicação:

``` powershell
java -cp "bin;lib\sqlite-jdbc-3.54.1.0.jar" interfacegrafica.MainGUI
```

### Linux / macOS --- Terminal

Crie a pasta de saída e compile:

``` bash
mkdir -p bin

javac --release 17 -encoding UTF-8 \
  -cp "lib/sqlite-jdbc-3.54.1.0.jar" \
  -d bin \
  $(find src -name "*.java")
```

Execute a aplicação:

``` bash
java -cp "bin:lib/sqlite-jdbc-3.54.1.0.jar" interfacegrafica.MainGUI
```

### Execução pela IDE

-   **IntelliJ IDEA:** abra a pasta do projeto, confirme que `src` está
    marcada como diretório de fontes e adicione
    `lib/sqlite-jdbc-3.54.1.0.jar` às bibliotecas do módulo. Execute
    `interfacegrafica.MainGUI`.
-   **Visual Studio Code:** instale o Extension Pack for Java, abra a
    pasta raiz e confirme que `lib/sqlite-jdbc-3.54.1.0.jar` está no
    classpath do projeto. Execute `interfacegrafica.MainGUI`.
-   **Eclipse / NetBeans:** configure `src` como diretório de fontes,
    adicione o JAR SQLite ao classpath e execute
    `interfacegrafica.MainGUI`.

## Banco de dados SQLite

### Inicialização automática

O método `main()` de `MainGUI` chama
`InicializadorBancoSQLite.inicializar()` antes de abrir a janela
principal.

A classe `InicializadorBancoSQLite`:

1.  Abre ou cria o banco por meio de `ConexaoSQLite`.
2.  Procura o script `sql/schema.sql` relativo à pasta de execução.
3.  Caso não encontre o arquivo nesse caminho, tenta carregá-lo como
    recurso `/sql/schema.sql`.
4.  Executa as instruções do script.
5.  Verifica a existência das tabelas `itens`, `usuarios` e
    `emprestimos`.

O script utiliza `CREATE TABLE IF NOT EXISTS`, para que a inicialização
não recrie tabelas que já existem.

O arquivo `biblioteca.db` é criado no diretório de trabalho da
aplicação. Por isso, execute o programa a partir da pasta raiz do
projeto para que o caminho `sql/schema.sql` seja encontrado
corretamente.

**Atenção:** não exclua `biblioteca.db` se quiser preservar os cadastros
e o histórico. Para começar com um banco vazio, feche a aplicação e faça
uma cópia de segurança antes de remover o arquivo.

### Tabelas

  -----------------------------------------------------------------------
  Tabela                              Finalidade
  ----------------------------------- -----------------------------------
  `itens`                             Armazena livros e revistas,
                                      incluindo código, título, tipo e
                                      disponibilidade.

  `usuarios`                          Armazena os usuários cadastrados.

  `emprestimos`                       Armazena os empréstimos, os
                                      usuários e itens relacionados,
                                      datas e situação.
  -----------------------------------------------------------------------

A implementação SQLite utiliza JDBC, `PreparedStatement`, fechamento de
recursos com `try-with-resources` e transações nas operações que
precisam manter mais de uma alteração consistente.

A classe `ConexaoSQLite` centraliza a abertura das conexões e ativa
`PRAGMA foreign_keys = ON`.

O script `sql/schema.sql` também restringe os empréstimos: aceita somente
os status `ATIVO`, `DEVOLVIDO` e `EXPIRADO`; exige data de devolução para
registros `DEVOLVIDO` e não permite essa data nos demais status; e cria um
índice único parcial para impedir mais de um empréstimo `ATIVO` por item.
Essas regras usam recursos disponíveis tanto no SQLite quanto no
PostgreSQL. Como o script usa `CREATE TABLE IF NOT EXISTS`, as novas
restrições da tabela não são aplicadas automaticamente a bancos já
existentes; nesses bancos, é necessário migrar a tabela antes de depender
delas.

## Estrutura do projeto

``` text
TrabalhoPOO/
├── src/
│   ├── modelo/
│   │   ├── Emprestimo.java
│   │   ├── ItemBiblioteca.java
│   │   ├── ItemIndisponivelException.java
│   │   ├── Livro.java
│   │   ├── Revista.java
│   │   ├── StatusEmprestimo.java
│   │   └── Usuario.java
│   ├── servico/
│   │   ├── Biblioteca.java
│   │   ├── ConexaoSQLite.java
│   │   ├── InicializadorBancoSQLite.java
│   │   ├── RepositorioBiblioteca.java
│   │   ├── RepositorioBibliotecaMemoria.java
│   │   └── RepositorioBibliotecaSQLite.java
│   └── interfacegrafica/
│       ├── BotaoArredondado.java
│       └── MainGUI.java
├── sql/
│   └── schema.sql
├── lib/
│   └── sqlite-jdbc-3.54.1.0.jar
├── docs/
│   └── javadoc/
├── .vscode/
├── README.md
└── TrabalhoPOO.iml
```

## Classes principais

  -----------------------------------------------------------------------
  Classe                              Responsabilidade
  ----------------------------------- -----------------------------------
  `ItemBiblioteca`                    Classe abstrata que representa um
                                      item do acervo.

  `Livro`                             Representa um livro, com autor e
                                      ISBN.

  `Revista`                           Representa uma revista, com número
                                      de edição.

  `Usuario`                           Representa um usuário cadastrado.

  `Emprestimo`                        Relaciona item e usuário e controla
                                      datas e situação do empréstimo.

  `StatusEmprestimo`                  Enumeração dos estados `ATIVO`,
                                      `DEVOLVIDO` e `EXPIRADO`.

  `ItemIndisponivelException`         Exceção própria para tentativas de
                                      empréstimo de item indisponível.

  `Biblioteca`                        Serviço que aplica as regras de
                                      negócio e utiliza o repositório.

  `RepositorioBiblioteca`             Interface que define as operações
                                      de armazenamento.

  `RepositorioBibliotecaMemoria`      Implementação em memória do
                                      repositório.

  `RepositorioBibliotecaSQLite`       Implementação persistente do
                                      repositório usando SQLite.

  `ConexaoSQLite`                     Centraliza a abertura de conexões
                                      com o banco.

  `InicializadorBancoSQLite`          Inicializa e verifica a estrutura
                                      do banco.

  `MainGUI`                           Janela principal e interface Swing
                                      da aplicação.

  `BotaoArredondado`                  Componente Swing personalizado para
                                      botões.
  -----------------------------------------------------------------------

## Regras de negócio

### Itens

-   O código do item é obrigatório, numérico e único.
-   O título não pode ser vazio.
-   Livros possuem autor e ISBN.
-   O ISBN deve possuir 13 dígitos.
-   Revistas possuem número de edição maior que zero.
-   Na edição, o código e o tipo do item não podem ser alterados.

### Usuários

-   O ID deve ser um número inteiro maior que zero e único.
-   O nome não pode ser vazio.
-   O e-mail deve ter um formato válido; são aceitos endereços de diferentes
    provedores, como Gmail, Hotmail e Outlook.
-   Domínios de exemplo/reservados e `teste.com` são rejeitados. Isso não
    confirma que a caixa postal existe; para comprovar isso seria necessário
    enviar um código ou link de confirmação por e-mail.

### Empréstimos e devoluções

-   Só é possível emprestar um item cadastrado e disponível.
-   O usuário precisa estar cadastrado.
-   Um item emprestado não pode ser emprestado novamente.
-   O prazo de devolução é de dois dias.
-   O sistema controla os estados `ATIVO`, `DEVOLVIDO` e `EXPIRADO`.
-   Quando o prazo vence, o empréstimo passa para `EXPIRADO` e o item
    volta a ficar disponível, conforme a lógica implementada no serviço.
-   A devolução registra a data e hora e exige um empréstimo ativo.
-   O histórico dos empréstimos é preservado.

### Exclusões

-   Um item emprestado não pode ser excluído.
-   Itens e usuários relacionados a empréstimos no histórico não podem
    ser excluídos.
-   A interface solicita confirmação antes de executar exclusões.

## Interface gráfica

A aplicação possui três abas principais:

### Itens

Permite cadastrar, consultar, editar e excluir livros e revistas. A
tabela apresenta código, título, tipo e disponibilidade.

### Usuários

Permite cadastrar, consultar, editar e excluir usuários. A tabela
apresenta ID, nome e e-mail.

### Empréstimos

Permite registrar empréstimos e devoluções. A tabela apresenta item,
usuário, data do empréstimo, prazo de devolução, data de devolução e
situação.

### Temas

A aplicação possui modo claro e modo escuro, alternados pelo botão no
canto superior direito. A interface utiliza uma paleta de cores para
manter a consistência dos componentes nos dois temas.

## SOLID aplicado

  -----------------------------------------------------------------------
  Princípio                           Aplicação no projeto
  ----------------------------------- -----------------------------------
  **SRP --- Responsabilidade Única**  `MainGUI` cuida da apresentação,
                                      `Biblioteca` das regras de negócio
                                      e `RepositorioBibliotecaSQLite` da
                                      persistência.

  **OCP --- Aberto/Fechado**          `Biblioteca` utiliza a interface
                                      `RepositorioBiblioteca`, permitindo
                                      diferentes implementações de
                                      armazenamento.

  **LSP --- Substituição de Liskov**  `Livro` e `Revista` são
                                      especializações de `ItemBiblioteca`
                                      e podem ser tratados por meio do
                                      tipo abstrato.

  **ISP --- Segregação de             O serviço depende do contrato de
  Interfaces**                        repositório que reúne as operações
                                      de armazenamento de que necessita.

  **DIP --- Inversão de Dependência** `Biblioteca` recebe
                                      `RepositorioBiblioteca` pelo
                                      construtor, em vez de depender
                                      diretamente de uma implementação
                                      concreta.
  -----------------------------------------------------------------------

Exemplo de DIP:

``` java
public class Biblioteca {
    private final RepositorioBiblioteca repositorio;

    public Biblioteca(RepositorioBiblioteca repositorio) {
        this.repositorio = repositorio;
    }
}
```

## Clean Architecture

O projeto utiliza uma organização inspirada na Clean Architecture,
separando modelo, serviço, interface gráfica e persistência.

-   **Modelo (`modelo`):** contém entidades e conceitos do domínio.
-   **Serviço (`servico`):** contém as regras de negócio, a abstração do
    repositório e suas implementações.
-   **Interface gráfica (`interfacegrafica`):** apresenta os dados e
    recebe as interações do usuário.

A classe `Biblioteca` depende da abstração `RepositorioBiblioteca`, e
não diretamente do SQLite. As implementações em memória e SQLite podem
ser utilizadas por meio desse contrato.

As implementações de repositório estão no mesmo pacote `servico`;
portanto, a separação é principalmente por responsabilidade e
dependência, não por uma divisão física completa em camadas
independentes.

## Clean Code

O projeto utiliza práticas voltadas à legibilidade e manutenção, como:

-   nomes expressivos para classes, métodos e variáveis;
-   separação entre modelo, regras de negócio, interface e persistência;
-   métodos auxiliares para operações repetidas;
-   validações nas entidades e nos serviços;
-   tratamento de exceções;
-   `try-with-resources` para recursos JDBC;
-   `SwingUtilities.invokeLater` para iniciar a interface;
-   `SwingWorker` para operações demoradas sem bloquear a interface.

## Checklist de conceitos do Trabalho 1

  -----------------------------------------------------------------------
  Conceito                            Aplicação
  ----------------------------------- -----------------------------------
  Classes e objetos                   `Livro`, `Revista`, `Usuario`,
                                      `Emprestimo` e `Biblioteca`.

  Atributos e métodos                 Estado e comportamentos definidos
                                      nas classes do modelo e serviço.

  Construtores                        Construtores das entidades recebem
                                      os dados necessários para sua
                                      criação.

  `this`                              Referencia atributos e construtores
                                      da própria classe.

  Modificadores de acesso             Uso de `private`, `public` e
                                      `protected` para controlar o
                                      acesso.

  Encapsulamento                      Atributos privados e operações que
                                      controlam alterações no estado.

  Pacotes                             Organização em `modelo`, `servico`
                                      e `interfacegrafica`.

  Herança                             `Livro` e `Revista` estendem
                                      `ItemBiblioteca`.

  Polimorfismo                        Livros e revistas são tratados por
                                      referências do tipo
                                      `ItemBiblioteca`.

  `super`                             Utilizado pelas subclasses para
                                      chamar construtores e
                                      comportamentos da classe base.

  Abstração                           `ItemBiblioteca` é uma classe
                                      abstrata.

  Interfaces                          `RepositorioBiblioteca` possui
                                      implementações em memória e SQLite.

  Enumerações                         `StatusEmprestimo` representa os
                                      estados do empréstimo.

  Exceções                            Uso de exceções padrão e
                                      `ItemIndisponivelException`.

  Interface gráfica                   Aplicação construída com Java
                                      Swing.

  Data e hora                         Uso de `LocalDateTime`,
                                      `ChronoUnit` e `DateTimeFormatter`.
  -----------------------------------------------------------------------

## JavaDoc

A documentação HTML gerada está em:

``` text
docs/javadoc/
```

Abra `docs/javadoc/index.html` para consultar a documentação.

Para gerar novamente a documentação, execute na pasta raiz:

**Windows --- PowerShell:**

``` powershell
javadoc -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -cp "lib\sqlite-jdbc-3.54.1.0.jar" -d "docs\javadoc" -sourcepath src -subpackages modelo:servico:interfacegrafica
```

**Linux / macOS:**

``` bash
javadoc -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 \
  -cp "lib/sqlite-jdbc-3.54.1.0.jar" \
  -d "docs/javadoc" \
  -sourcepath src \
  -subpackages modelo:servico:interfacegrafica
```

## Verificação técnica

Na cópia do projeto utilizada para preparar este README:

-   a compilação foi concluída com sucesso usando `javac --release 17`;
-   a geração do JavaDoc foi concluída e produziu `index.html`;
-   os comandos de compilação foram conferidos em ambiente Linux com JDK
    21, limitando a compilação à API e à sintaxe do Java 17.

A abertura da janela Swing e os comandos no PowerShell do Windows devem
ser conferidos em um computador com ambiente gráfico e JDK instalado
antes da entrega final.

## Suporte a PostgreSQL com Docker

O Sistema Biblioteca oferece suporte a dois bancos de dados relacionais: **SQLite** e **PostgreSQL**.

O SQLite continua disponível para execução local, enquanto o PostgreSQL permite centralizar os dados em um servidor e compartilhar o mesmo banco entre diferentes computadores.

### Arquitetura

A aplicação utiliza Java Swing na interface gráfica, JDBC para comunicação com o banco e uma interface Java chamada `RepositorioBiblioteca` para abstrair a persistência.

```text
MainGUI (Java Swing)
         |
     Biblioteca
         |
 RepositorioBiblioteca
         |
    +----+----+
    |         |
  SQLite  PostgreSQL
           (Docker)
```

As classes `RepositorioBibliotecaSQLite` e `RepositorioBibliotecaPostgres` implementam o mesmo contrato, permitindo trocar de banco sem alterar as regras de negócio da classe `Biblioteca`.

Essa organização aplica princípios de orientação a objetos, especialmente a inversão de dependência (DIP).

### PostgreSQL no Docker

O PostgreSQL 18 é executado em um contêiner Docker denominado `postgres-biblioteca`.

Configuração utilizada no ambiente de desenvolvimento:

| Configuração | Valor |
|---|---|
| Banco | `biblioteca` |
| Porta | `5432` |
| Contêiner | `postgres-biblioteca` |
| Volume persistente | `biblioteca_dados` |
| Driver | PostgreSQL JDBC |
| Usuário administrativo local | `postgres` |

O Docker mantém o banco separado da aplicação Java, e o volume preserva os dados mesmo após a reinicialização do contêiner.

Para verificar o contêiner:

```powershell
docker ps
```

Para iniciar um contêiner já criado:

```powershell
docker start postgres-biblioteca
```

**Observação:** o contêiner e as tabelas PostgreSQL foram configurados previamente no ambiente de desenvolvimento. O projeto ainda não possui inicialização automática completa do esquema PostgreSQL em um ambiente novo.

### Escolha do banco de dados

A aplicação utiliza variáveis de ambiente para selecionar a implementação.

**SQLite (padrão):**

```text
BIBLIOTECA_BANCO=sqlite
```

**PostgreSQL:**

```text
BIBLIOTECA_BANCO=postgres
BIBLIOTECA_DB_PASSWORD=<senha_configurada_localmente>
```

No IntelliJ IDEA, essas variáveis podem ser definidas em:

`Run → Edit Configurations → MainGUI → Environment variables`

A conexão PostgreSQL local utiliza:

```text
jdbc:postgresql://localhost:5432/biblioteca
```

As senhas devem permanecer fora do código-fonte e não devem ser enviadas ao GitHub.

### Funcionalidades testadas

Foram realizados testes de integração com PostgreSQL envolvendo:

- Conexão JDBC;
- Cadastro, consulta, atualização e exclusão de itens;
- Cadastro, consulta, atualização e exclusão de usuários;
- Registro de empréstimos;
- Devolução e atualização da disponibilidade dos livros;
- Consulta do histórico de empréstimos pela interface Swing.

Os testes confirmaram o funcionamento dos principais fluxos da Biblioteca com PostgreSQL.

### Acesso por diferentes computadores

O PostgreSQL fica hospedado no computador que executa o Docker. Outros computadores podem acessar o mesmo banco pela rede, mediante configuração de conectividade e permissões.

Para testes de acesso remoto, está sendo adotado o Tailscale, que permite estabelecer uma rede privada entre os computadores.

```text
Computador A — Biblioteca Swing
                  |
              Rede privada
                  |
Computador B — Biblioteca Swing
                  |
        PostgreSQL centralizado
        (Docker no computador servidor)
```

Cada instalação da aplicação utiliza o endereço do servidor PostgreSQL e credenciais autorizadas.

O acesso remoto requer configuração adicional da rede, da porta TCP e das permissões do PostgreSQL. Não é necessário disponibilizar a porta do banco publicamente na internet.

O computador servidor precisa permanecer ligado, com Docker e PostgreSQL em execução.

### Tecnologias utilizadas

- Java e programação orientada a objetos;
- Swing;
- JDBC;
- SQLite;
- PostgreSQL 18;
- Docker;
- Tailscale para conectividade privada entre computadores.

O suporte ao PostgreSQL foi desenvolvido como implementação adicional, preservando a opção de executar a Biblioteca com SQLite.

------------------------------------------------------------------------

**Sistema Biblioteca --- Trabalho 2 de Programação Orientada a
Objetos.**
