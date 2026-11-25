package interfacegrafica;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Botão personalizado com fundo verde e cantos arredondados.
 * Desenha um anel branco quando recebe o foco do teclado e um tom acinzentado
 * quando está desabilitado.
 */
public class BotaoArredondado extends JButton {

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

        setForeground(Color.WHITE);
        setBackground(new Color(55, 145, 115));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        Color cor = !isEnabled()
                ? new Color(130, 165, 152)
                : getModel().isPressed()
                ? new Color(38, 115, 90)
                : getModel().isRollover()
                ? new Color(65, 160, 130)
                : getBackground();

        g2.setColor(cor);
        g2.fillRoundRect(
                0, 0, getWidth(), getHeight(),
                16, 16
        );

        if (isFocusOwner()) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 12, 12);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}