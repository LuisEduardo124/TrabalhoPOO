package interfacegrafica;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;

/**
 * Botão personalizado com cantos arredondados.
 * Os tons de passagem do mouse e de clique são derivados da cor de fundo; quando desabilitado,
 * usa as cores definidas em {@link #setCoresDesabilitado(Color, Color)}, para que o estado
 * fique legível tanto no tema claro quanto no escuro. Pode ter um contorno (botões secundários)
 * e desenha um anel na cor do texto quando recebe o foco do teclado, o que garante contraste
 * com o fundo em qualquer estilo de botão.
 */
public class BotaoArredondado extends JButton {

    private static final int RAIO = 16;

    private Color fundoDesabilitado = new Color(218, 223, 221);
    private Color textoDesabilitado = new Color(115, 123, 120);
    private Color corContorno;

    /**
     * Cria um botão arredondado com o texto informado.
     *
     * @param texto rótulo exibido no botão
     */
    public BotaoArredondado(String texto) {
        super(texto);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);

        setForeground(Color.WHITE);
        setBackground(new Color(35, 125, 95));
    }

    /**
     * Define as cores usadas enquanto o botão está desabilitado.
     *
     * @param fundo cor de preenchimento
     * @param texto cor do rótulo
     */
    public void setCoresDesabilitado(Color fundo, Color texto) {
        this.fundoDesabilitado = fundo;
        this.textoDesabilitado = texto;
        repaint();
    }

    /**
     * Define a cor do contorno do botão.
     *
     * @param cor cor do contorno, ou {@code null} para um botão sem contorno
     */
    public void setCorContorno(Color cor) {
        this.corContorno = cor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<?, ?> dicasTexto = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (dicasTexto != null) {
            g2.addRenderingHints(dicasTexto);
        }

        Color base = getBackground();
        // Em fundos claros o realce de passagem escurece; em fundos escuros, clareia.
        boolean fundoClaro = luminancia(base) > 0.6;
        Color cor = !isEnabled()
                ? fundoDesabilitado
                : getModel().isPressed()
                ? misturar(base, Color.BLACK, fundoClaro ? 0.12f : 0.20f)
                : getModel().isRollover()
                ? misturar(base, fundoClaro ? Color.BLACK : Color.WHITE, fundoClaro ? 0.05f : 0.10f)
                : base;

        g2.setColor(cor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RAIO, RAIO);

        if (corContorno != null && isEnabled()) {
            g2.setColor(corContorno);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RAIO, RAIO);
        }

        if (isFocusOwner()) {
            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 12, 12);
        }

        // O texto é desenhado aqui para que a cor de desabilitado siga o tema atual.
        String texto = getText();
        if (texto != null && !texto.isEmpty()) {
            g2.setFont(getFont());
            FontMetrics medidas = g2.getFontMetrics();
            int x = (getWidth() - medidas.stringWidth(texto)) / 2;
            int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
            g2.setColor(isEnabled() ? getForeground() : textoDesabilitado);
            g2.drawString(texto, x, y);
        }

        g2.dispose();
    }

    private static double luminancia(Color cor) {
        return (0.299 * cor.getRed() + 0.587 * cor.getGreen() + 0.114 * cor.getBlue()) / 255.0;
    }

    private static Color misturar(Color origem, Color alvo, float proporcao) {
        return new Color(
                Math.round(origem.getRed() + (alvo.getRed() - origem.getRed()) * proporcao),
                Math.round(origem.getGreen() + (alvo.getGreen() - origem.getGreen()) * proporcao),
                Math.round(origem.getBlue() + (alvo.getBlue() - origem.getBlue()) * proporcao));
    }
}
