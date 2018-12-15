package ch.quinella.launcher.launcher;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.*;

import ch.quinella.launcher.launcher.misc.JSetting;
import ch.quinella.launcher.Main;
import ch.quinella.launcher.launcher.misc.PasswordCrypto;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.util.Saver;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("serial")
public class LauncherPanel extends JPanel implements ActionListener {

    private Image background;

    private JButton jouer = new JButton(new ImageIcon(""));
    private JButton quitter = new JButton(new ImageIcon(getClass().getClassLoader().getResource("quitter.png")));
    private JProgressBar pb = new JProgressBar();
    private JLabel pbtext = new JLabel("Chargement…", SwingConstants.CENTER);
    private JButton settings = new JButton(new ImageIcon(getClass().getClassLoader().getResource("setting.png")));

    private Saver saver = new Saver(new File(Launcher.DIR, "launcher.properties"));

    private JTextField usernameField = new JTextField(saver.get("username", ""));
    private JPasswordField passwordField = new JPasswordField();

    private double RAM = Double.parseDouble(saver.get("ram", "1000"));

    private PasswordCrypto crypto = new PasswordCrypto();

    public LauncherPanel() {

        try {
            byte[] key = new BASE64Decoder().decodeBuffer((String)saver.get("key", ""));
            Main.display(new BASE64Encoder().encode(key));
            if(key.length != 0){
                PasswordCrypto passwordCrypto = new PasswordCrypto(new SecretKeySpec(key, 0, key.length, "AES"));
                passwordField.setText(passwordCrypto.decryptToken(new BASE64Decoder().decodeBuffer(saver.get("token", ""))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setLayout(null);

        usernameField.setOpaque(false);
        usernameField.setFont(new Font("Avenir", Font.BOLD, 18));
        usernameField.setBorder(null);
        usernameField.setBounds(198, 67, 305, 35);
        this.add(usernameField);

        passwordField.setOpaque(false);
        passwordField.setFont(usernameField.getFont());
        passwordField.setBorder(null);
        passwordField.setBounds(79, 160, 305, 35);
        this.add(passwordField);

        jouer.setBounds(302, 371, 229, 46);
        jouer.addActionListener(this);
        jouer.setOpaque(false);
        jouer.setContentAreaFilled(false);
        jouer.setBorderPainted(false);
        this.add(jouer);

        quitter.setBounds(820, 5, 20, 20);
        quitter.addActionListener(this);
        this.add(quitter);

        settings.setBounds(790, 5, 20, 20);
        settings.addActionListener(this);
        this.add(settings);

        pb.setBounds(25, 433, 800, 20);
        pb.setStringPainted(true);
        pb.setVisible(false);
        pb.setFont(usernameField.getFont());
        this.add(pb);

        pbtext.setBounds(25, 340, 780, 35);
        pbtext.setForeground(Color.WHITE);
        pbtext.setFont(usernameField.getFont());
        this.add(pbtext);


        try {
            background = ImageIO.read(getClass().getClassLoader().getResource(("background.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == jouer) {

            this.setProgressBarVisible(false);


            setFieldsEnabled(false);

            if (usernameField.getText().replaceAll(" ", "").length() == 0 || passwordField.getText().length() == 0) {

                JOptionPane.showMessageDialog(this, "Erreur, veuillez entrer un pseudo ainsi qu'un mot de passe valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                setFieldsEnabled(true);
                System.out.println("Mot de passe invalide.");

                return;
            }

            saver.set("username", usernameField.getText());

            Thread t = new Thread(() -> {
                try {
                    Launcher.auth(usernameField.getText(), passwordField.getText());
                } catch (AuthenticationException e1) {
                    JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, impossible de se connceter :" + e1.getErrorModel().getErrorMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    setFieldsEnabled(true);
                    return;
                }

                System.out.println("Connexion établie avec succès.");

                byte[] token = crypto.generateToken(passwordField.getText());

                String messagetokenize = new BASE64Encoder().encode(token);

                saver.set("token", messagetokenize);

                saver.set("key", new BASE64Encoder().encode(crypto.getKey().getEncoded()));

                if(Main.isLoading == true){

                    Main.display("Veuillez attendre la fin du téléchargement");
                    JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, veuillez attendre la fin du téléchargement");

                }
                else if(RAM <= 1500){

                    int retour = JOptionPane.showConfirmDialog(Main.getInstance(), "Vous avez alloué moins de 2Go de RAM, il se peut que votre jeu lag ou ne se lance pas correctement." +
                            " \nVous pouvez modifier cela en cliquant sur l'engrenage en haut à droite de la fenêtre. Souhaitez-vous tout de même continuer ?"
                            , "Confirmation" ,JOptionPane.YES_NO_OPTION);

                    if(retour == 1) return;

                }
                else {
                    try {
                        this.setInfoLabel("Le jeu se lance, veuillez attendre la fin du chargement. Cela peut prendre plusieurs secondes");
                        Launcher.launch();
                    } catch (LaunchException e1) {
                        e1.printStackTrace();
                    }
                }

            });
            t.start();
        } else if (e.getSource() == quitter) {

            System.exit(0);

        } else if (e.getSource() == settings) {

            JSetting dialog = new JSetting(LauncherFrame.getInstance(), "Réglages", RAM);

        }
    }

    private void setFieldsEnabled(boolean enabled) {

        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        jouer.setEnabled(enabled);


    }

    public void setProgress(int value) {

        pb.setValue(value);
    }

    public void setInfoLabel(String text) {

        pbtext.setText(text);

    }

    public void setProgressBarVisible(boolean value) {

        pb.setVisible(value);

    }

    public void setRAM(double RAM){

        this.RAM = RAM * 1000;

        saver.set("ram", String.valueOf(RAM * 1000));

    }

    public double getRAM(){

        return this.RAM;

    }

}