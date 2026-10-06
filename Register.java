import javax.swing.*;

public class Register {
    static String savedUsername = "";
    static String savedPassword = "";

    JFrame frame;
    JTextField txtUsername;
    JPasswordField txtPassword;
    JPasswordField txtConfirmPass;

    public Register() {

        frame = new JFrame("Registration");
        frame.setSize(500, 400);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblUser = new JLabel("Username: ");
        lblUser.setBounds(50, 50, 100, 30);

        txtUsername = new JTextField();
        txtUsername.setBounds(180, 50, 170, 30);


        JLabel lblPass = new JLabel("Password: ");
        lblPass.setBounds(50, 100, 100, 30);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(180, 100, 170, 30);


        JLabel lblconfirmPass = new JLabel("Confirm Password: ");
        lblconfirmPass.setBounds(50, 150, 170,30);

        txtConfirmPass = new JPasswordField();
        txtConfirmPass.setBounds(180,150,170,30);



        JButton btnRegister = new JButton("Register");
        btnRegister.setBounds(80, 210, 110, 35);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(210, 210, 110, 35);

        btnRegister.addActionListener(e -> doRegister());

        btnBack.addActionListener(e -> {
            frame.dispose();
                new MainFrame();
        });

        frame.add(lblUser);
        frame.add(txtUsername);
        frame.add(lblPass);
        frame.add(txtPassword);
        frame.add(lblconfirmPass);
        frame.add(txtConfirmPass);
        frame.add(btnRegister);
        frame.add(btnBack);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void doRegister() {

        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please complete all fields.");
            return;
        }

        if (!password.equals(confirm)){
            JOptionPane.showMessageDialog(frame, "Passwords do not match.");
            txtPassword.setText("");
            txtConfirmPass.setText("");
            txtPassword.requestFocus();
            return;
        }

        savedUsername = username;
        savedPassword = password;

        JOptionPane.showMessageDialog(frame, "Registration successful.");

        frame.dispose();
            new MainFrame();
    }
}
