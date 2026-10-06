import javax.swing.*;

public class Login {

    JFrame frame;
    JTextField txtUsername;
    JPasswordField txtPassword;

    public Login() {

        frame = new JFrame("Login");
        frame.setSize(400, 280);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblUser = new JLabel("Username: ");
        lblUser.setBounds(50, 50, 100, 30);

        txtUsername = new JTextField();
        txtUsername.setBounds(150, 50, 170, 30);

        JLabel lblPass = new JLabel("Password: ");
        lblPass.setBounds(50, 100, 100, 30);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 100, 170, 30);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(70, 160, 110, 35);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(200, 160, 110, 35);

        btnLogin.addActionListener(e -> doLogin());

        btnBack.addActionListener(e -> {
            frame.dispose();
            new MainFrame();
        });

        frame.add(lblUser);
        frame.add(txtUsername);
        frame.add(lblPass);
        frame.add(txtPassword);
        frame.add(btnLogin);
        frame.add(btnBack);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void doLogin() {

        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (Register.savedUsername.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No account yet. Please register first.");
            return;
        }

        if (username.equals(Register.savedUsername)
                && password.equals(Register.savedPassword)) {

            JOptionPane.showMessageDialog(frame, "Login successful.");

            frame.dispose();
            new Calculator();

        } else {
            JOptionPane.showMessageDialog(frame, "Invalid username or password.");
        }
    }
}
