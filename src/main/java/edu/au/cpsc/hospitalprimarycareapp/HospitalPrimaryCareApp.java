package edu.au.cpsc.hospitalprimarycareapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class HospitalPrimaryCareApp {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/hospital_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Jesusis#01";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HospitalPrimaryCareApp::startGui);
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void startGui() {
        applyUiStyle();

        try (Connection conn = getConnection()) {
            createTables(conn);
            seedData(conn);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Database failed to initialize:\n" + e.getMessage()
                            + "\n\nMake sure MySQL is running and this exists:\nCREATE DATABASE hospital_db;",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame frame = new JFrame("Hospital Primary Care Database System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1180, 720);
        frame.setMinimumSize(new Dimension(1050, 650));
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Hospital Primary Care Database System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(35, 45, 60));

        JLabel subtitle = new JLabel("Select a report, enter optional values, and run the SQL query.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(90, 100, 115));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        String[] reports = {
                "1.1 Occupied Rooms",
                "1.2 Unoccupied Rooms",
                "1.3 All Rooms",
                "2.1 All Patients",
                "2.2 Currently Admitted Patients",
                "2.3 Patients Discharged in Date Range",
                "2.4 Patients Admitted in Date Range",
                "2.5 Admissions for Patient ID",
                "2.6 Treatments for Patient ID",
                "2.7 Readmitted Within 30 Days",
                "2.8 Admission Statistics",
                "3.1 Diagnosis Occurrences",
                "3.2 Hospital Diagnosis Occurrences",
                "3.3 Treatments Performed",
                "3.4 Diagnoses for Frequent Admissions",
                "3.5 Treatment Occurrence Lookup",
                "4.1 All Employees",
                "4.2 Primary Doctors of High Admission Rate Patients",
                "4.3 Diagnoses by Doctor ID",
                "4.4 Treatments Ordered by Doctor ID",
                "4.5 Employees Involved in Every Admitted Patient"
        };

        JComboBox<String> reportBox = new JComboBox<>(reports);

        JTextField patientIdField = new JTextField();
        JTextField doctorIdField = new JTextField();
        JTextField treatmentAdminIdField = new JTextField();
        JTextField startDateField = new JTextField("2026-01-01");
        JTextField endDateField = new JTextField("2026-12-31");

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(inputPanel, gbc, 0, "Report:", reportBox);
        addFormRow(inputPanel, gbc, 1, "Patient ID:", patientIdField);
        addFormRow(inputPanel, gbc, 2, "Doctor Employee ID:", doctorIdField);
        addFormRow(inputPanel, gbc, 3, "Treatment Administration ID:", treatmentAdminIdField);
        addFormRow(inputPanel, gbc, 4, "Start Date YYYY-MM-DD:", startDateField);
        addFormRow(inputPanel, gbc, 5, "End Date YYYY-MM-DD:", endDateField);

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220)),
                BorderFactory.createTitledBorder("SQL Query Results")
        ));

        JButton runButton = new JButton("Run Selected Query");
        JButton clearButton = new JButton("Clear Results");
        JButton exitButton = new JButton("Exit");

        styleButton(runButton);
        styleButton(clearButton);
        styleButton(exitButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.WEST);
        mainPanel.add(outputScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        runButton.addActionListener(e -> {
            try (Connection conn = getConnection()) {
                runSelectedReport(
                        conn,
                        (String) reportBox.getSelectedItem(),
                        patientIdField.getText().trim(),
                        doctorIdField.getText().trim(),
                        treatmentAdminIdField.getText().trim(),
                        startDateField.getText().trim(),
                        endDateField.getText().trim(),
                        outputArea
                );
            } catch (SQLException ex) {
                outputArea.setText("Database error:\n" + ex.getMessage());
            }
        });

        clearButton.addActionListener(e -> outputArea.setText(""));
        exitButton.addActionListener(e -> System.exit(0));

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private static void applyUiStyle() {
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Consolas", Font.PLAIN, 14));
    }

    private static void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JComponent field
    ) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        field.setPreferredSize(new Dimension(390, 36));
        panel.add(field, gbc);
    }

    private static void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(175, 38));
        button.setBackground(new Color(232, 238, 247));
        button.setBorder(BorderFactory.createLineBorder(new Color(165, 175, 190)));
    }

    private static void runSelectedReport(
            Connection conn,
            String selected,
            String patientId,
            String doctorId,
            String treatmentAdminId,
            String startDate,
            String endDate,
            JTextArea output
    ) {
        try {
            if (selected.startsWith("1.1")) {
                runQuery(conn, """
                    SELECT r.room_no, p.patient_id, p.first_name, p.last_name, a.admit_time
                    FROM room r
                    JOIN admission a ON r.room_no = a.room_no AND a.discharge_time IS NULL
                    JOIN patient p ON p.patient_id = a.patient_id
                    ORDER BY r.room_no
                """, output);

            } else if (selected.startsWith("1.2")) {
                runQuery(conn, """
                    SELECT r.room_no
                    FROM room r
                    LEFT JOIN admission a
                        ON r.room_no = a.room_no AND a.discharge_time IS NULL
                    WHERE a.admission_id IS NULL
                    ORDER BY r.room_no
                """, output);

            } else if (selected.startsWith("1.3")) {
                runQuery(conn, """
                    SELECT r.room_no, p.patient_id, p.first_name, p.last_name, a.admit_time
                    FROM room r
                    LEFT JOIN admission a
                        ON r.room_no = a.room_no AND a.discharge_time IS NULL
                    LEFT JOIN patient p ON p.patient_id = a.patient_id
                    ORDER BY r.room_no
                """, output);

            } else if (selected.startsWith("2.1")) {
                runQuery(conn, "SELECT * FROM patient ORDER BY last_name, first_name", output);

            } else if (selected.startsWith("2.2")) {
                runQuery(conn, """
                    SELECT DISTINCT p.patient_id, p.first_name, p.last_name
                    FROM patient p
                    JOIN admission a ON p.patient_id = a.patient_id
                    WHERE a.discharge_time IS NULL
                    ORDER BY p.patient_id
                """, output);

            } else if (selected.startsWith("2.3")) {
                runPreparedQuery(conn, """
                    SELECT DISTINCT p.patient_id, p.first_name, p.last_name
                    FROM patient p
                    JOIN admission a ON p.patient_id = a.patient_id
                    WHERE DATE(a.discharge_time) BETWEEN DATE(?) AND DATE(?)
                    ORDER BY p.patient_id
                """, output, startDate, endDate);

            } else if (selected.startsWith("2.4")) {
                runPreparedQuery(conn, """
                    SELECT DISTINCT p.patient_id, p.first_name, p.last_name
                    FROM patient p
                    JOIN admission a ON p.patient_id = a.patient_id
                    WHERE DATE(a.admit_time) BETWEEN DATE(?) AND DATE(?)
                    ORDER BY p.patient_id
                """, output, startDate, endDate);

            } else if (selected.startsWith("2.5")) {
                require(patientId, "Patient ID");
                runPreparedQuery(conn, """
                    SELECT a.admission_id, a.admit_time, a.discharge_time, d.diagnosis_name
                    FROM admission a
                    JOIN diagnosis d ON a.diagnosis_id = d.diagnosis_id
                    WHERE a.patient_id = ?
                    ORDER BY a.admit_time DESC
                """, output, patientId);

            } else if (selected.startsWith("2.6")) {
                require(patientId, "Patient ID");
                runPreparedQuery(conn, """
                    SELECT a.admission_id, a.admit_time, t.treatment_name, ta.admin_time
                    FROM admission a
                    JOIN treatment_order tor ON tor.admission_id = a.admission_id
                    JOIN treatment t ON t.treatment_id = tor.treatment_id
                    JOIN treatment_administration ta ON ta.order_id = tor.order_id
                    WHERE a.patient_id = ?
                    ORDER BY a.admit_time DESC, ta.admin_time ASC
                """, output, patientId);

            } else if (selected.startsWith("2.7")) {
                runQuery(conn, """
                    SELECT p.patient_id, p.first_name, p.last_name, d.diagnosis_name,
                           CONCAT(e.first_name, ' ', e.last_name) AS admitting_doctor
                    FROM admission a
                    JOIN admission prev
                        ON prev.patient_id = a.patient_id
                       AND prev.discharge_time IS NOT NULL
                       AND a.admit_time > prev.discharge_time
                       AND a.admit_time <= DATE_ADD(prev.discharge_time, INTERVAL 30 DAY)
                    JOIN patient p ON p.patient_id = a.patient_id
                    JOIN diagnosis d ON d.diagnosis_id = a.diagnosis_id
                    JOIN employee e ON e.employee_id = a.primary_doctor_id
                    ORDER BY p.patient_id
                """, output);

            } else if (selected.startsWith("2.8")) {
                runQuery(conn, """
                    WITH ordered AS (
                        SELECT patient_id, admission_id, admit_time, discharge_time,
                               LAG(discharge_time) OVER (
                                   PARTITION BY patient_id ORDER BY admit_time
                               ) AS previous_discharge
                        FROM admission
                    ),
                    spans AS (
                        SELECT patient_id,
                               DATEDIFF(COALESCE(discharge_time, CURRENT_TIMESTAMP), admit_time)
                                   AS duration_days,
                               CASE
                                   WHEN previous_discharge IS NOT NULL
                                   THEN DATEDIFF(admit_time, previous_discharge)
                               END AS span_days
                        FROM ordered
                    )
                    SELECT p.patient_id, p.first_name, p.last_name,
                           COUNT(*) AS total_admissions,
                           ROUND(AVG(duration_days), 2) AS avg_duration_days,
                           MAX(span_days) AS longest_span_days,
                           MIN(span_days) AS shortest_span_days,
                           ROUND(AVG(span_days), 2) AS avg_span_days
                    FROM patient p
                    JOIN spans s ON s.patient_id = p.patient_id
                    GROUP BY p.patient_id, p.first_name, p.last_name
                    ORDER BY p.patient_id
                """, output);

            } else if (selected.startsWith("3.1") || selected.startsWith("3.2")) {
                runQuery(conn, """
                    SELECT d.diagnosis_id, d.diagnosis_name, COUNT(*) AS total_occurrences
                    FROM admission a
                    JOIN diagnosis d ON d.diagnosis_id = a.diagnosis_id
                    GROUP BY d.diagnosis_id, d.diagnosis_name
                    ORDER BY total_occurrences DESC
                """, output);

            } else if (selected.startsWith("3.3")) {
                runQuery(conn, """
                    SELECT t.treatment_id, t.treatment_name, COUNT(*) AS total_occurrences
                    FROM treatment_administration ta
                    JOIN treatment_order tor ON tor.order_id = ta.order_id
                    JOIN treatment t ON t.treatment_id = tor.treatment_id
                    GROUP BY t.treatment_id, t.treatment_name
                    ORDER BY total_occurrences DESC
                """, output);

            } else if (selected.startsWith("3.4")) {
                runQuery(conn, """
                    SELECT d.diagnosis_id, d.diagnosis_name, COUNT(*) AS occurrences
                    FROM admission a
                    JOIN diagnosis d ON d.diagnosis_id = a.diagnosis_id
                    WHERE a.patient_id IN (
                        SELECT patient_id
                        FROM admission
                        GROUP BY patient_id
                        HAVING COUNT(*) = (
                            SELECT MAX(total)
                            FROM (
                                SELECT COUNT(*) AS total
                                FROM admission
                                GROUP BY patient_id
                            ) x
                        )
                    )
                    GROUP BY d.diagnosis_id, d.diagnosis_name
                    ORDER BY occurrences ASC
                """, output);

            } else if (selected.startsWith("3.5")) {
                require(treatmentAdminId, "Treatment Administration ID");
                runPreparedQuery(conn, """
                    SELECT CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
                           CONCAT(e.first_name, ' ', e.last_name) AS ordering_doctor
                    FROM treatment_administration ta
                    JOIN treatment_order tor ON tor.order_id = ta.order_id
                    JOIN admission a ON a.admission_id = tor.admission_id
                    JOIN patient p ON p.patient_id = a.patient_id
                    JOIN employee e ON e.employee_id = tor.ordered_by_doctor_id
                    WHERE ta.administration_id = ?
                """, output, treatmentAdminId);

            } else if (selected.startsWith("4.1")) {
                runQuery(conn, """
                    SELECT employee_id, first_name, last_name, job_category
                    FROM employee
                    ORDER BY last_name, first_name
                """, output);

            } else if (selected.startsWith("4.2")) {
                runQuery(conn, """
                    SELECT DISTINCT e.employee_id, e.first_name, e.last_name
                    FROM admission a
                    JOIN employee e ON e.employee_id = a.primary_doctor_id
                    WHERE a.patient_id IN (
                        SELECT patient_id
                        FROM admission
                        GROUP BY patient_id, YEAR(admit_time)
                        HAVING COUNT(*) >= 4
                    )
                    ORDER BY e.last_name, e.first_name
                """, output);

            } else if (selected.startsWith("4.3")) {
                require(doctorId, "Doctor Employee ID");
                runPreparedQuery(conn, """
                    SELECT d.diagnosis_id, d.diagnosis_name, COUNT(*) AS total_occurrences
                    FROM admission a
                    JOIN diagnosis d ON d.diagnosis_id = a.diagnosis_id
                    WHERE a.primary_doctor_id = ?
                    GROUP BY d.diagnosis_id, d.diagnosis_name
                    ORDER BY total_occurrences DESC
                """, output, doctorId);

            } else if (selected.startsWith("4.4")) {
                require(doctorId, "Doctor Employee ID");
                runPreparedQuery(conn, """
                    SELECT t.treatment_id, t.treatment_name, COUNT(*) AS total_ordered
                    FROM treatment_order tor
                    JOIN treatment t ON t.treatment_id = tor.treatment_id
                    WHERE tor.ordered_by_doctor_id = ?
                    GROUP BY t.treatment_id, t.treatment_name
                    ORDER BY total_ordered DESC
                """, output, doctorId);

            } else if (selected.startsWith("4.5")) {
                runQuery(conn, """
                    SELECT e.employee_id, e.first_name, e.last_name, e.job_category
                    FROM employee e
                    WHERE NOT EXISTS (
                        SELECT DISTINCT a.patient_id
                        FROM admission a
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM treatment_administration_employee tae
                            JOIN treatment_administration ta
                                ON ta.administration_id = tae.administration_id
                            JOIN treatment_order tor
                                ON tor.order_id = ta.order_id
                            JOIN admission adm
                                ON adm.admission_id = tor.admission_id
                            WHERE tae.employee_id = e.employee_id
                              AND adm.patient_id = a.patient_id
                        )
                    )
                """, output);
            }

        } catch (Exception ex) {
            output.setText("Input/query error:\n" + ex.getMessage());
        }
    }

    private static void require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required for this query.");
        }
    }

    private static void runQuery(Connection conn, String sql, JTextArea output) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            output.setText(formatResults(rs));
        }
    }

    private static void runPreparedQuery(
            Connection conn,
            String sql,
            JTextArea output,
            String... values
    ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setString(i + 1, values[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                output.setText(formatResults(rs));
            }
        }
    }

    private static String formatResults(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();

        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            sb.append(String.format("%-25s", md.getColumnName(i)));
        }
        sb.append("\n");
        sb.append("-".repeat(cols * 25)).append("\n");

        int count = 0;

        while (rs.next()) {
            count++;
            for (int i = 1; i <= cols; i++) {
                String value = rs.getString(i);
                sb.append(String.format("%-25s", value == null ? "" : value));
            }
            sb.append("\n");
        }

        sb.append("\nRows returned: ").append(count);
        return sb.toString();
    }

    private static void createTables(Connection conn) throws SQLException {
        Statement st = conn.createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS patient (
                patient_id INT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                date_of_birth DATE NOT NULL,
                sex VARCHAR(10) NOT NULL,
                street_address VARCHAR(100) NOT NULL,
                city VARCHAR(50) NOT NULL,
                state VARCHAR(10) NOT NULL,
                zip_code VARCHAR(10) NOT NULL,
                phone VARCHAR(20) NOT NULL
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS employee (
                employee_id INT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                job_category VARCHAR(20) NOT NULL,
                CHECK (job_category IN ('Doctor', 'Nurse', 'Technician', 'Staff', 'Administrator'))
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS room (
                room_no INT PRIMARY KEY,
                CHECK (room_no BETWEEN 1 AND 20)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS diagnosis (
                diagnosis_id INT AUTO_INCREMENT PRIMARY KEY,
                diagnosis_name VARCHAR(100) NOT NULL
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS treatment (
                treatment_id INT AUTO_INCREMENT PRIMARY KEY,
                treatment_name VARCHAR(100) NOT NULL,
                treatment_type VARCHAR(20) NOT NULL,
                CHECK (treatment_type IN ('Procedure', 'Medication'))
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS admission (
                admission_id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id INT NOT NULL,
                room_no INT NOT NULL,
                admit_time DATETIME NOT NULL,
                discharge_time DATETIME,
                diagnosis_id INT NOT NULL,
                primary_doctor_id INT NOT NULL,
                admitted_by_admin_id INT NOT NULL,
                discharged_by_admin_id INT,
                FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
                FOREIGN KEY (room_no) REFERENCES room(room_no),
                FOREIGN KEY (diagnosis_id) REFERENCES diagnosis(diagnosis_id),
                FOREIGN KEY (primary_doctor_id) REFERENCES employee(employee_id),
                FOREIGN KEY (admitted_by_admin_id) REFERENCES employee(employee_id),
                FOREIGN KEY (discharged_by_admin_id) REFERENCES employee(employee_id)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS admission_emergency_contact (
                contact_id INT AUTO_INCREMENT PRIMARY KEY,
                admission_id INT NOT NULL,
                contact_name VARCHAR(100) NOT NULL,
                relationship VARCHAR(50) NOT NULL,
                phone VARCHAR(20) NOT NULL,
                FOREIGN KEY (admission_id) REFERENCES admission(admission_id)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS admission_insurance (
                insurance_id INT AUTO_INCREMENT PRIMARY KEY,
                admission_id INT NOT NULL,
                provider_name VARCHAR(100) NOT NULL,
                policy_number VARCHAR(50) NOT NULL,
                FOREIGN KEY (admission_id) REFERENCES admission(admission_id)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS assigned_doctor (
                admission_id INT NOT NULL,
                doctor_id INT NOT NULL,
                PRIMARY KEY (admission_id, doctor_id),
                FOREIGN KEY (admission_id) REFERENCES admission(admission_id),
                FOREIGN KEY (doctor_id) REFERENCES employee(employee_id)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS treatment_order (
                order_id INT AUTO_INCREMENT PRIMARY KEY,
                admission_id INT NOT NULL,
                treatment_id INT NOT NULL,
                ordered_by_doctor_id INT NOT NULL,
                order_time DATETIME NOT NULL,
                FOREIGN KEY (admission_id) REFERENCES admission(admission_id),
                FOREIGN KEY (treatment_id) REFERENCES treatment(treatment_id),
                FOREIGN KEY (ordered_by_doctor_id) REFERENCES employee(employee_id)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS treatment_administration (
                administration_id INT AUTO_INCREMENT PRIMARY KEY,
                order_id INT NOT NULL,
                admin_time DATETIME NOT NULL,
                notes VARCHAR(255),
                FOREIGN KEY (order_id) REFERENCES treatment_order(order_id)
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS treatment_administration_employee (
                administration_id INT NOT NULL,
                employee_id INT NOT NULL,
                PRIMARY KEY (administration_id, employee_id),
                FOREIGN KEY (administration_id) REFERENCES treatment_administration(administration_id),
                FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
            )
        """);
    }

    private static void seedData(Connection conn) throws SQLException {
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patient");
        if (rs.next() && rs.getInt(1) > 0) {
            return;
        }

        for (int i = 1; i <= 20; i++) {
            st.executeUpdate("INSERT INTO room(room_no) VALUES (" + i + ")");
        }

        st.executeUpdate("""
            INSERT INTO patient(first_name,last_name,date_of_birth,sex,street_address,city,state,zip_code,phone)
            VALUES
            ('Kay','Smith','1945-02-10','F','100 Oak St','Auburn','AL','36830','555-1001'),
            ('Robert','Jones','1940-07-22','M','200 Pine St','Auburn','AL','36830','555-1002'),
            ('Linda','Brown','1950-11-05','F','300 Maple St','Auburn','AL','36830','555-1003'),
            ('James','Taylor','1939-04-18','M','400 Cedar St','Auburn','AL','36830','555-1004')
        """);

        st.executeUpdate("""
            INSERT INTO employee(first_name,last_name,job_category)
            VALUES
            ('Alice','Carter','Doctor'),
            ('Brian','Miller','Doctor'),
            ('Nina','Wilson','Nurse'),
            ('Tom','Davis','Technician'),
            ('Sarah','Adams','Administrator'),
            ('Emma','Moore','Nurse')
        """);

        st.executeUpdate("""
            INSERT INTO diagnosis(diagnosis_name)
            VALUES ('Pneumonia'), ('Hypertension'), ('Diabetes'), ('Fracture')
        """);

        st.executeUpdate("""
            INSERT INTO treatment(treatment_name,treatment_type)
            VALUES
            ('Antibiotic Therapy','Medication'),
            ('Blood Pressure Medication','Medication'),
            ('Insulin Injection','Medication'),
            ('X-Ray','Procedure')
        """);

        st.executeUpdate("""
            INSERT INTO admission(patient_id,room_no,admit_time,discharge_time,diagnosis_id,
                                  primary_doctor_id,admitted_by_admin_id,discharged_by_admin_id)
            VALUES
            (1,1,'2026-01-10 09:00:00','2026-01-15 10:00:00',1,1,5,5),
            (1,2,'2026-02-01 11:00:00',NULL,2,1,5,NULL),
            (2,3,'2026-03-05 14:00:00',NULL,3,2,5,NULL),
            (3,4,'2026-01-01 08:00:00','2026-01-05 12:00:00',4,2,5,5),
            (4,5,'2026-04-01 08:00:00','2026-04-03 09:00:00',1,1,5,5),
            (4,6,'2026-05-01 08:00:00','2026-05-03 09:00:00',2,1,5,5),
            (4,7,'2026-06-01 08:00:00','2026-06-03 09:00:00',3,1,5,5),
            (4,8,'2026-07-01 08:00:00',NULL,4,1,5,NULL)
        """);

        st.executeUpdate("""
            INSERT INTO admission_emergency_contact(admission_id,contact_name,relationship,phone)
            VALUES
            (1,'Mark Smith','Son','555-2001'),
            (2,'Mark Smith','Son','555-2001'),
            (3,'Anna Jones','Daughter','555-2002'),
            (4,'Paul Brown','Husband','555-2003'),
            (5,'Mary Taylor','Wife','555-2004'),
            (6,'Mary Taylor','Wife','555-2004'),
            (7,'Mary Taylor','Wife','555-2004'),
            (8,'Mary Taylor','Wife','555-2004')
        """);

        st.executeUpdate("""
            INSERT INTO admission_insurance(admission_id,provider_name,policy_number)
            VALUES
            (1,'Medicare','MED1001'),
            (2,'Aetna','AET1002'),
            (3,'Blue Cross','BC1003'),
            (4,'Medicare','MED1004'),
            (5,'Medicare','MED1005'),
            (6,'Medicare','MED1006'),
            (7,'Medicare','MED1007'),
            (8,'Medicare','MED1008')
        """);

        st.executeUpdate("""
            INSERT INTO assigned_doctor(admission_id,doctor_id)
            VALUES
            (1,1), (2,1), (2,2), (3,2), (4,2),
            (5,1), (6,1), (7,1), (8,1)
        """);

        st.executeUpdate("""
            INSERT INTO treatment_order(admission_id,treatment_id,ordered_by_doctor_id,order_time)
            VALUES
            (1,1,1,'2026-01-10 10:00:00'),
            (2,2,1,'2026-02-01 12:00:00'),
            (3,3,2,'2026-03-05 15:00:00'),
            (4,4,2,'2026-01-01 09:00:00'),
            (5,1,1,'2026-04-01 10:00:00'),
            (6,2,1,'2026-05-01 10:00:00'),
            (7,3,1,'2026-06-01 10:00:00'),
            (8,4,1,'2026-07-01 10:00:00')
        """);

        st.executeUpdate("""
            INSERT INTO treatment_administration(order_id,admin_time,notes)
            VALUES
            (1,'2026-01-10 12:00:00','First dose'),
            (2,'2026-02-01 13:00:00','BP medication administered'),
            (3,'2026-03-05 16:00:00','Insulin administered'),
            (4,'2026-01-01 10:00:00','X-Ray completed'),
            (5,'2026-04-01 12:00:00','Antibiotic administered'),
            (6,'2026-05-01 12:00:00','BP medication administered'),
            (7,'2026-06-01 12:00:00','Insulin administered'),
            (8,'2026-07-01 12:00:00','X-Ray completed')
        """);

        st.executeUpdate("""
            INSERT INTO treatment_administration_employee(administration_id,employee_id)
            VALUES
            (1,3), (1,6),
            (2,3),
            (3,6),
            (4,4),
            (5,3),
            (6,3),
            (7,6),
            (8,4)
        """);
    }
}