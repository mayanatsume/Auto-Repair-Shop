// java/src/oficina/CartItem.java
package oficina;

public class CartItem {
    private final int    id;
    private final String nome;
    private final String marca;
    private final String categoria;
    private final double custo;
    private final double preco;
    private final double lucro;

    public CartItem(int id, String nome, String marca, String categoria,
                    double custo, double preco) {
        this.id        = id;
        this.nome      = nome;
        this.marca     = marca;
        this.categoria = categoria;
        this.custo     = custo;
        this.preco     = preco;
        this.lucro     = preco - custo;
    }

    public int    getId()        { return id; }
    public String getNome()      { return nome; }
    public String getMarca()     { return marca; }
    public String getCategoria(){ return categoria; }
    public double getCusto()     { return custo; }
    public double getPreco()     { return preco; }
    public double getLucro()     { return lucro; }

    @Override
    public String toString() {
        return String.format("%d – %s (%s): custo=%.2f, preco=%.2f, lucro=%.2f",
            id, nome, categoria, custo, preco, lucro);
    }
}
