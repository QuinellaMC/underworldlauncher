package ch.quinella.launcher.launcher.misc;

import ch.quinella.launcher.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;

public class JSetting extends JDialog implements ActionListener {

    private String RAM;

    private JComboBox<String> combo;
    private JButton buttonOK;
    private JTextField field;
    private JLabel label;

    private double disRAM;

    public JSetting(JFrame frame, String name, double RAM){

        super(frame, name, true);

        disRAM = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize() * 1e-9;
        int rRAM = (int) disRAM;
        Main.display(String.valueOf(rRAM));
        String[] RAMChoices;
        if(rRAM == 0 || rRAM == 1) {
            RAMChoices = new String[2];
            RAMChoices[0] = "1.0Go";
            RAMChoices[1] = "Autre";
        }
        else{
            RAMChoices = new String[rRAM + 1]; // + 1 car il faut mettre "Autre" à la fin
            for(int i = 0; i < rRAM; i++){
                RAMChoices[i] = (i + 1d) + "Go";
            }
            RAMChoices[rRAM] = "Autre";
        }
        String parRAM = (RAM/1000) + "Go";
        boolean exists = false;
        int index = 0;
        for(int i = 0; i < RAMChoices.length; i++){
            if(parRAM.equals(RAMChoices[i])){
                exists = true;
                index = i;
            }
        }
        if(!exists) {
            RAMChoices[RAMChoices.length - 2] = parRAM;
            index = RAMChoices.length - 2;
        }
        int x = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 5;
        int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 5;

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(x, y);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        buttonOK = new JButton("Valider");
        buttonOK.setBounds(120,90,x/5,y/5);

        combo = new JComboBox<>(RAMChoices);
        Container contenu = this.getContentPane();
        contenu.setLayout(new FlowLayout());
        combo.setBounds(30, 10, 30, 0);

        combo.setSelectedItem(index == 0 ? "1.0Go" : RAMChoices[index]);

        field = new JTextField(9);
//        field.setBounds(10, 10, 400, 10);

        label = new JLabel("Entrer une valeur : ");

        label.setVisible(false);
        field.setVisible(false);

        contenu.add(buttonOK);
        contenu.add(combo);
        contenu.add(label);
        contenu.add(field);

        buttonOK.addActionListener(this);
        combo.addActionListener(this);
        pack();

        this.setVisible(true);

        this.RAM = (String)combo.getSelectedItem();


    }

    public float getRAM(){

        return Float.parseFloat(RAM);

    }

    public void actionPerformed(ActionEvent e){

        if(e.getSource() == combo){

            String valeur = (String)combo.getSelectedItem();

            if(valeur == "Autre"){

                label.setVisible(true);
                field.setVisible(true);
                field.setEditable(true);
            }
            else if(label.isVisible()){

                label.setVisible(false);
                field.setVisible(false);
                field.setEditable(false);

            }
            pack();
        }
        if(e.getSource() == buttonOK){

            String valeur = (String)combo.getSelectedItem();
            String input;
            if(valeur == "Autre"){
                try{
                    input = (field.getText().replace(",",".").replace("Go", "").replace(" ", ""));
                    Double.parseDouble(input);
                    if(Double.parseDouble(input)/disRAM >=0.9){

                        int retour = JOptionPane.showConfirmDialog(Main.getInstance(), "Êtes-vous sûr de vouloir mettre plus de 90% de votre mémoire vive restante ? \nCela pourrait ralentir vos applications en tâches de fond.", "Confirmation", JOptionPane.YES_NO_OPTION);

                        if(retour == 1){

                            return;

                        }

                    }
                    Main.getInstance().getLauncherPanel().setRAM(Double.parseDouble(input));
                } catch(NumberFormatException ex){

                    JOptionPane.showMessageDialog(Main.getInstance(), "Veuillez entrer un nombre réel positif, ou sélectionner une autre valeur");
                }
            }
            else{
                input = valeur;
                input = input.substring(0, input.length() - 2);

                Main.getInstance().getLauncherPanel().setRAM(Double.parseDouble(input));

            }
            this.dispose();
        }
    }


}
