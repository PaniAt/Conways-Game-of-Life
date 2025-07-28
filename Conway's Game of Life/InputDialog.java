/**
 * Gets input from the user using a dialog box.
 *
 * @author Atreya Pandit
 * @version 26/05/2025
 */
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class InputDialog extends JDialog
{
    private String response;

    /**
     * Creates a dialog box which takes the user's input.
     */
    public InputDialog(String query)
    {
        super(new JFrame(query), query);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setMinimumSize(new Dimension(query.length()*8, 100));
        JTextField reply = new JTextField();
        JButton button = new JButton("Confirm");
        button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent evt){
                    response = reply.getText();
                    close();
                }
            }
        );
        this.setLayout(new GridLayout(2, 1, 5, 5));
        this.add(reply);
        this.add(button);
        this.pack();
        setModal(true);
    }
    
    private void close(){
        this.dispose();
    }
    
    public String getText(){
        return response;
    }
}