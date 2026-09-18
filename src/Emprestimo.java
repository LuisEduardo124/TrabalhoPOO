public class Emprestimo {
    private final ItemBiblioteca item;
    private final Usuario usuario;
    private boolean ativo;

    public Emprestimo(ItemBiblioteca item, Usuario usuario) {
        this.item = item;
        this.usuario = usuario;
        this.ativo = true;
        item.emprestar();
    }

    public ItemBiblioteca getItem() {
        return item;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void devolver() {
        if (ativo) {
            ativo = false;
            item.devolver();
        }
    }

    @Override
    public String toString() {
        return "Empréstimo | item: " + item.getTitulo()
                + " | usuário: " + usuario.getNome()
                + " | ativo: " + ativo;
    }
}
