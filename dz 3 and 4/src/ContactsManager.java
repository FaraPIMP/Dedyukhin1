import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import static java.sql.DriverManager.getConnection;

public class ContactsManager extends JFrame {


    private static final String DB_URL = "jdbc:mysql://it.vshp.online:3306/db_9e741d";
    private static final String DB_USER = "st_9e741d";
    private static final String DB_PASSWORD = "1dda91ebb573";

    private JTextField firstNameField, lastNameField, emailField, addressField, cityField, phoneField;

    public ContactsManager() {
        setTitle("Управление контактами");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        mainPanel.add(new JLabel("Имя:"));
        firstNameField = new JTextField();
        mainPanel.add(firstNameField);

        mainPanel.add(new JLabel("Фамилия:"));
        lastNameField = new JTextField();
        mainPanel.add(lastNameField);

        mainPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        mainPanel.add(emailField);

        mainPanel.add(new JLabel("Адрес:"));
        addressField = new JTextField();
        mainPanel.add(addressField);

        mainPanel.add(new JLabel("Город:"));
        cityField = new JTextField();
        mainPanel.add(cityField);

        mainPanel.add(new JLabel("Номер телефона:"));
        phoneField = new JTextField();
        mainPanel.add(phoneField);

        JButton addButton = new JButton("Добавить контакт");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addContact();
            }
        });
        mainPanel.add(addButton);

        JButton fetchButton = new JButton("Получить контакты");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchContacts();
            }
        });
        mainPanel.add(fetchButton);

        JButton deleteButton = new JButton("Удалить контакт");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteContact();
            }
        });
        mainPanel.add(deleteButton);

        add(mainPanel);
        setVisible(true);
    }

    // Метод для добавления контакта в базу данных
    private void addContact() {
        try (Connection connection = getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String address = addressField.getText();
            String city = cityField.getText();
            String phone = phoneField.getText();

            String sql = "INSERT INTO people (FirstName, LastName, Email) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, email);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int personID = generatedKeys.getInt(1);
                        sql = "INSERT INTO addresses (PersonID, Address, City) VALUES (?, ?, ?)";
                        try (PreparedStatement addressStatement = connection.prepareStatement(sql)) {
                            addressStatement.setInt(1, personID);
                            addressStatement.setString(2, address);
                            addressStatement.setString(3, city);
                            int addressRowsAffected = addressStatement.executeUpdate();
                            if (addressRowsAffected > 0) {
                                sql = "INSERT INTO phonenumbers (PersonID, phonenumber) VALUES (?, ?)";
                                try (PreparedStatement phoneStatement = connection.prepareStatement(sql)) {
                                    phoneStatement.setInt(1, personID);
                                    phoneStatement.setString(2, phone);
                                    int phoneRowsAffected = phoneStatement.executeUpdate();
                                    if (phoneRowsAffected > 0) {
                                        JOptionPane.showMessageDialog(this, "Контакт успешно добавлен.");
                                    } else {
                                        JOptionPane.showMessageDialog(this, "Не удалось добавить номер телефона контакта.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "Не удалось добавить адрес контакта.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось добавить контакт.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении контакта.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для получения контактов из базы данных
    private void fetchContacts() {
        try (Connection connection = getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM people p JOIN addresses a ON p.PersonID = a.PersonID JOIN phonenumbers ph ON p.PersonID = ph.PersonID")) {

            StringBuilder contacts = new StringBuilder("Список контактов:\n");
            while (resultSet.next()) {
                int id = resultSet.getInt("PersonID");
                String firstName = resultSet.getString("FirstName");
                String lastName = resultSet.getString("LastName");
                String email = resultSet.getString("Email");
                String address = resultSet.getString("Address");
                String city = resultSet.getString("City");
                String phone = resultSet.getString("PhoneNumber");

                contacts.append(id).append(": ").append(firstName).append(" ").append(lastName).append(" (").append(email).append("), ");
                contacts.append(address).append(", ").append(city).append(", ").append(phone).append("\n");
            }

            JOptionPane.showMessageDialog(this, contacts.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при получении контактов.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

   
    // Метод для удаления контакта из базы данных
    private void deleteContact() {
        String personIDString = JOptionPane.showInputDialog(this, "Введите ID контакта для удаления:");
        if (personIDString != null && !personIDString.isEmpty()) {
            int personID = Integer.parseInt(personIDString);
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "DELETE FROM phonenumbers WHERE PersonID = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, personID);
                    int phoneRowsAffected = preparedStatement.executeUpdate();
                    if (phoneRowsAffected > 0) {
                        sql = "DELETE FROM addresses WHERE PersonID = ?";
                        try (PreparedStatement addressStatement = connection.prepareStatement(sql)) {
                            addressStatement.setInt(1, personID);
                            int addressRowsAffected = addressStatement.executeUpdate();
                            if (addressRowsAffected > 0) {
                                sql = "DELETE FROM people WHERE PersonID = ?";
                                try (PreparedStatement deleteStatement = connection.prepareStatement(sql)) {
                                    deleteStatement.setInt(1, personID);
                                    int deleteRowsAffected = deleteStatement.executeUpdate();
                                    if (deleteRowsAffected > 0) {
                                        JOptionPane.showMessageDialog(this, "Контакт успешно удален.");
                                    } else {
                                        JOptionPane.showMessageDialog(this, "Не удалось удалить контакт.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "Не удалось удалить адрес контакта.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Не удалось удалить номер телефона контакта.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при удалении контакта.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ContactsManager();
            }
        });
    }

}


