// java/src/oficina/Cart.java
package oficina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    /** Adiciona um item ao carrinho */
    public void addItem(CartItem item) {
        items.add(item);
    }

    /** Retorna lista imutável de itens */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Soma dos preços de venda */
    public double totalPreco() {
        return items.stream().mapToDouble(CartItem::getPreco).sum();
    }

    /** Soma dos custos */
    public double totalCusto() {
        return items.stream().mapToDouble(CartItem::getCusto).sum();
    }

    /** Soma dos lucros */
    public double totalLucro() {
        return items.stream().mapToDouble(CartItem::getLucro).sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Carrinho Imaginário:\n");
        for (CartItem i : items) {
            sb.append(" • ").append(i).append("\n");
        }
        sb.append(String.format("Total Custo: %.2f\n", totalCusto()));
        sb.append(String.format("Total Preço: %.2f\n", totalPreco()));
        sb.append(String.format("Total Lucro: %.2f\n", totalLucro()));
        return sb.toString();
    }
}
