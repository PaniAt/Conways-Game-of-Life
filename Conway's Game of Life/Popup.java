
/**
 * Creates a popup message.
 *
 * @author Atreya Pandit
 * @version 26/05/2025
 */
import java.awt.*;
import javax.swing.*;
public class Popup extends JDialog
{
    /**
     * Constructor for objects of class Popup
     */
    public Popup(String text)
    {
        this.setTitle(text);
        this.pack();
        this.setVisible(true);
        this.setMinimumSize(new Dimension(text.length()*8, 0));
    }
}
