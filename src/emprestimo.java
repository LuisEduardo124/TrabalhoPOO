class Emprestimo {
    private String livro;
    private String usuario;

    private String dataEmprestimo;
    private String dataDevolucao;
    private  boolean ativo;
   
   
    public Emprestimo(String livro, String usuario, String dataEmprestimo, String dataDevolucao, boolean ativo) {
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
        this.ativo = ativo;
    }
   
   
    // get/set DataEmprestimo
    public String getDataEmprestimo() {
        return dataEmprestimo;
    }
    public void setDataEmprestimo(String dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    // get/set DataDevolucao
    public String getDataDevolucao() {
        return dataDevolucao;
    }
    public void setDataDevolucao(String dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    // get/set Ativo
    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

}