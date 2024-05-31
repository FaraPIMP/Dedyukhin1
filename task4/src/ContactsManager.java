import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;

import static java.sql.DriverManager.getConnection;

public class ContactsManager extends JFrame {


    private static final String DB_URL = "jdbc:mysql://it.vshp.online:3306/db_9e741d";
    private static final String DB_USER = "st_9e741d";
    private static final String DB_PASSWORD = "1dda91ebb573";

    private JTextField firstNameField, lastNameField, emailField, addressField, cityField, phoneField;

    public ContactsManager() {
        setupFrame();
        JPanel mainPanel = createMainPanel();
        addComponentsToPanel(mainPanel);
        add(mainPanel);
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Управление контактами");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        return mainPanel;
    }

    private void addComponentsToPanel(JPanel panel) {
        addLabelAndField(panel, "Имя:", firstNameField = new JTextField());
        addLabelAndField(panel, "Фамилия:", lastNameField = new JTextField());
        addLabelAndField(panel, "Email:", emailField = new JTextField());
        addLabelAndField(panel, "Адрес:", addressField = new JTextField());
        addLabelAndField(panel, "Город:", cityField = new JTextField());
        addLabelAndField(panel, "Номер телефона:", phoneField = new JTextField());

        addButton(panel, "Добавить контакт", e -> addContact());
        addButton(panel, "Получить контакты", e -> fetchContacts());
        addButton(panel, "Удалить контакт", e -> deleteContact());
    }

    private void addLabelAndField(JPanel panel, String labelText, JTextField textField) {
        panel.add(new JLabel(labelText));
        panel.add(textField);
    }

    private void addButton(JPanel panel, String buttonText, ActionListener actionListener) {
        JButton button = new JButton(buttonText);
        button.addActionListener(actionListener);
        panel.add(button);
    }

    // Добавления контакта в базу данных
    private void addContact() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String address = addressField.getText();
        String city = cityField.getText();
        String phone = phoneField.getText();

        try (Connection connection = getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            int personID = addPerson(connection, firstName, lastName, email);
            if (personID > 0) {
                boolean addressAdded = addAddress(connection, personID, address, city);
                if (addressAdded) {
                    boolean phoneAdded = addPhoneNumber(connection, personID, phone);
                    if (phoneAdded) {
                        showSuccessMessage("Контакт успешно добавлен.");
                    } else {
                        showErrorMessage("Не удалось добавить номер телефона контакта.");
                    }
                } else {
                    showErrorMessage("Не удалось добавить адрес контакта.");
                }
            } else {
                showErrorMessage("Не удалось добавить контакт.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorMessage("Ошибка при добавлении контакта.");
        }
    }

    private int addPerson(Connection connection, String firstName, String lastName, String email) throws SQLException {
        String sql = "INSERT INTO people (FirstName, LastName, Email) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    private boolean addAddress(Connection connection, int personID, String address, String city) throws SQLException {
        String sql = "INSERT INTO addresses (PersonID, Address, City) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, personID);
            preparedStatement.setString(2, address);
            preparedStatement.setString(3, city);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private boolean addPhoneNumber(Connection connection, int personID, String phone) throws SQLException {
        String sql = "INSERT INTO phonenumbers (PersonID, phonenumber) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, personID);
            preparedStatement.setString(2, phone);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }


    // Получения контактов из базы данных
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


    // Удаления контакта из базы данных
    private void deleteContact() {
        Integer personID = getPersonID();
        if (personID != null) {
            boolean phoneDeleted = deleteRecord("phonenumbers", personID);
            if (phoneDeleted) {
                boolean addressDeleted = deleteRecord("addresses", personID);
                if (addressDeleted) {
                    boolean personDeleted = deleteRecord("people", personID);
                    if (personDeleted) {
                        showMessage("Контакт успешно удален.");
                    } else {
                        showError("Не удалось удалить контакт.");
                    }
                } else {
                    showError("Не удалось удалить адрес контакта.");
                }
            } else {
                showError("Не удалось удалить номер телефона контакта.");
            }
        }
    }

    private Integer getPersonID() {
        String personIDString = JOptionPane.showInputDialog(this, "Введите ID контакта для удаления:");
        if (personIDString != null && !personIDString.isEmpty()) {
            try {
                return Integer.parseInt(personIDString);
            } catch (NumberFormatException e) {
                showError("Некорректный ID.");
            }
        }
        return null;
    }

    private boolean deleteRecord(String tableName, int personID) {
        String sql = "DELETE FROM " + tableName + " WHERE PersonID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, personID);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Ошибка при удалении из таблицы " + tableName + ".");
            return false;
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Ошибка", JOptionPane.ERROR_MESSAGE);
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


