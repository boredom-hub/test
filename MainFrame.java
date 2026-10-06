import javax.swing.*;

public class MainFrame {

    JFrame frame;

    public MainFrame() {

        frame = new JFrame("Welcome");
        frame.setSize(400, 280);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblTitle = new JLabel("Choose an option");
        lblTitle.setBounds(0, 50, 400, 30);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnRegister = new JButton("Register");
        btnRegister.setBounds(70, 130, 110, 35);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(200, 130, 110, 35);

        btnRegister.addActionListener(e -> {
            frame.dispose();
            new Register();
        });

        btnLogin.addActionListener(e -> {
            frame.dispose();
            new Login();
        });

        frame.add(lblTitle);
        frame.add(btnRegister);
        frame.add(btnLogin);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}
