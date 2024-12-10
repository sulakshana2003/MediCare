package Controller;

import java.sql.*;
import javax.swing.*;
import Model.Patient;
import View.PatientUI;

public class PatientManagement {
    private Connection connection;
    private PatientUI patientUI;
    private JFrame frame;

    // MySQL Connection Parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/patient_management";
    private static final String USER = "root"; // Replace with your phpMyAdmin username
    private static final String PASS = ""; // Replace with your phpMyAdmin password

    public PatientManagement(PatientUI patientUI) {
        this.patientUI = patientUI;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish Database Connection
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

            // Load existing patients into UI
            loadExistingPatients();

            // Test Connection
            if (connection != null) {
                System.out.println("Database Connected Successfully!");
            }
        } catch (ClassNotFoundException e) {
            // Driver not found error
            JOptionPane.showMessageDialog(null,
                "MySQL JDBC Driver not found: " + e.getMessage(),
                "Driver Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            // Detailed Error Logging
            System.err.println("Connection Failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Message: " + e.getMessage());
            e.printStackTrace();

            // Show User-Friendly Error Dialog
            JOptionPane.showMessageDialog(null,
                "Database Connection Error: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addPatient() {
    JTextField idField = new JTextField();
    JTextField nameField = new JTextField();
    JTextField ageField = new JTextField();
    JTextField contactField = new JTextField();
    
    Object[] fields = {
        "ID:", idField,
        "Name:", nameField,
        "Age:", ageField,
        "Contact:", contactField
    };
    
    int option = JOptionPane.showConfirmDialog(frame, fields, "Add Patient", JOptionPane.OK_CANCEL_OPTION);
    
    if (option == JOptionPane.OK_OPTION) {
        try {
            // Create a new Patient instance
            Patient patient = new Patient(
                idField.getText(),
                nameField.getText(),
                Integer.parseInt(ageField.getText()),
                Integer.parseInt(contactField.getText())
            );
            
            // Insert patient into database
            String sql = "INSERT INTO patients (id, name, age, contact) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, patient.getId());
                pstmt.setString(2, patient.getName());
                pstmt.setInt(3, patient.getAge());
                pstmt.setInt(4, patient.getContactNumber());
                
                pstmt.executeUpdate();
                
                // Add to UI table
                patientUI.model.addRow(new Object[]{patient.getId(), patient.getName(), patient.getAge(), patient.getContactNumber()});
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, 
                "Invalid input. Age and Contact Number must be numbers.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, 
                "Database error: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}


    public void updatePatient() {
    if (patientUI.selectedRow == -1) {
        JOptionPane.showMessageDialog(frame, 
            "Please select a patient to update.", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Get current patient ID from table
    String id = (String) patientUI.model.getValueAt(patientUI.selectedRow, 0);
    
    try {
        // Fetch current patient details from database
        String selectSql = "SELECT * FROM patients WHERE id = ?";
        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        selectStmt.setString(1, id);
        ResultSet rs = selectStmt.executeQuery();
        
        if (rs.next()) {
            // Create a Patient instance with current data
            Patient patient = new Patient(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getInt("contact")
            );
            
            JTextField nameField = new JTextField(patient.getName());
            JTextField ageField = new JTextField(String.valueOf(patient.getAge()));
            JTextField contactField = new JTextField(String.valueOf(patient.getContactNumber()));
            
            Object[] fields = {
                "Name:", nameField,
                "Age:", ageField,
                "Contact:", contactField
            };
            
            int option = JOptionPane.showConfirmDialog(frame, fields, "Update Patient", JOptionPane.OK_CANCEL_OPTION);
            
            if (option == JOptionPane.OK_OPTION) {
                try {
                    patient.setName(nameField.getText());
                    patient.setAge(Integer.parseInt(ageField.getText()));
                    patient.setContactNumber(Integer.parseInt(contactField.getText()));
                    
                    // Update patient in database
                    String updateSql = "UPDATE patients SET name = ?, age = ?, contact = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, patient.getName());
                        updateStmt.setInt(2, patient.getAge());
                        updateStmt.setInt(3, patient.getContactNumber());
                        updateStmt.setString(4, patient.getId());
                        
                        updateStmt.executeUpdate();
                        
                        // Update UI table
                        patientUI.model.setValueAt(patient.getName(), patientUI.selectedRow, 1);
                        patientUI.model.setValueAt(patient.getAge(), patientUI.selectedRow, 2);
                        patientUI.model.setValueAt(patient.getContactNumber(), patientUI.selectedRow, 3);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Invalid input. Age must be a number.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(frame, 
            "Database error: " + ex.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

    public void removePatient() {
    if (patientUI.selectedRow == -1) {
        JOptionPane.showMessageDialog(frame, 
            "Please select a patient to remove.", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Get the selected patient ID from the table
    String id = (String) patientUI.model.getValueAt(patientUI.selectedRow, 0);

    int confirm = JOptionPane.showConfirmDialog(frame, 
        "Are you sure you want to remove this patient?", 
        "Confirm Removal", 
        JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            // Remove patient from the database
            String deleteSql = "DELETE FROM patients WHERE id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, id);
                deleteStmt.executeUpdate();
            }

            // Remove the patient from the UI table
            patientUI.model.removeRow(patientUI.selectedRow);
            patientUI.selectedRow = -1;

            JOptionPane.showMessageDialog(frame, 
                "Patient removed successfully.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, 
                "Error removing patient: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}


    private void loadExistingPatients() {
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM patients")) {
        
        while (rs.next()) {
            // Create a Patient instance
            Patient patient = new Patient(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getInt("contact")
            );
            
            // Add patient to the UI table
            patientUI.model.addRow(new Object[]{patient.getId(), patient.getName(), patient.getAge(), patient.getContactNumber()});
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(frame, 
            "Error loading patients: " + ex.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}


    // Close database connection when done
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}