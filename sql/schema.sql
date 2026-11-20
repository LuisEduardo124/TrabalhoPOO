CREATE TABLE IF NOT EXISTS itens (
     codigo TEXT PRIMARY KEY NOT NULL,
    titulo TEXT NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('Livro', 'Revista')),
    autor TEXT,
    isbn TEXT,
    edicao INTEGER,
    disponivel INTEGER NOT NULL DEFAULT 1 CHECK (disponivel IN (0, 1))
    );

-- Guarda os usuários cadastrados na biblioteca.
CREATE TABLE IF NOT EXISTS usuarios (
                                        id INTEGER PRIMARY KEY CHECK (id > 0),
    nome TEXT NOT NULL CHECK (length(trim(nome)) > 0),
    email TEXT NOT NULL
    );

-- Registra os empréstimos e vincula cada item ao usuário.
CREATE TABLE IF NOT EXISTS emprestimos (
                                           id INTEGER PRIMARY KEY,
                                           codigo_item TEXT NOT NULL REFERENCES itens(codigo),
    id_usuario INTEGER NOT NULL REFERENCES usuarios(id),
    data_emprestimo TEXT NOT NULL,
    data_devolucao TEXT,
    status TEXT NOT NULL DEFAULT 'ATIVO'
    CHECK (status IN ('ATIVO', 'DEVOLVIDO', 'EXPIRADO'))
    );