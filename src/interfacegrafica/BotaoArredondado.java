package interfacegrafica;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicGraphicsUtils;
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
 * fique legível tanto no tema claro quanto no escuro. Desenha um anel branco quando recebe
 * o foco do teclado.
 */
public class BotaoArredondado extends JButton {

    private Color fundoDesabilitado = new Color(218, 223, 221);
    private Color textoDesabilitado = new Color(115, 123, 120);

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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<?, ?> dicasTexto = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (dicasTexto != null) {
            g2.addRenderingHints(dicasTexto);
        }

        Color base = getBackground();
        Color cor = !isEnabled()
                ? fundoDesabilitado
                : getModel().isPressed()
                ? misturar(base, Color.BLACK, 0.20f)
                : getModel().isRollover()
                ? misturar(base, Color.WHITE, 0.10f)
                : base;

        g2.setColor(cor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        if (isFocusOwner()) {
            g2.setColor(Color.WHITE);
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
            int sublinhado = isEnabled() ? getDisplayedMnemonicIndex() : -1;
            BasicGraphicsUtils.drawStringUnderlineCharAt(this, g2, texto, sublinhado, x, y);
        }

        g2.dispose();
    }

    private static Color misturar(Color origem, Color alvo, float proporcao) {
        return new Color(
                Math.round(origem.getRed() + (alvo.getRed() - origem.getRed()) * proporcao),
                Math.round(origem.getGreen() + (alvo.getGreen() - origem.getGreen()) * proporcao),
                Math.round(origem.getBlue() + (alvo.getBlue() - origem.getBlue()) * proporcao));
    }
}
