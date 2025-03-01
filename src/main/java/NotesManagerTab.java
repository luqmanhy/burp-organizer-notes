
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class NotesManagerTab {
    private final JPanel panel;
    private final DefaultTableModel tableModel;
    private final JTable notesTable;
    private final NotesStorage notesStorage;

    public NotesManagerTab(NotesStorage notesStorage) {
        this.notesStorage = notesStorage;
        panel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"No", "Notes"}, 0);
        notesTable = new JTable(tableModel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        TableColumn column = notesTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(50);
        column.setMaxWidth(50);
        column.setMinWidth(50);

        updateNotesTable();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = new JButton("Add Notes");
        addButton.addActionListener(this::addNotes);

        JButton editButton = new JButton("Edit Notes");
        editButton.addActionListener(this::editNotes);

        JButton deleteButton = new JButton("Delete Notes");
        deleteButton.addActionListener(this::deleteNotes);

        JButton importButton = new JButton("Import as JSON");
        importButton.addActionListener(this::importNotes);

        JButton exportButton = new JButton("Export as JSON");
        exportButton.addActionListener(this::exportNotes);

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(deleteButton);

        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new BoxLayout(bottomButtonPanel, BoxLayout.Y_AXIS));
        bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 10, 10, 10));
        bottomButtonPanel.add(importButton);
        bottomButtonPanel.add(Box.createVerticalStrut(10));
        bottomButtonPanel.add(exportButton);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(buttonPanel, BorderLayout.NORTH);
        leftPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        mainPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(notesTable), gbc);

        panel.add(mainPanel, BorderLayout.CENTER);
    }

    public JPanel getPanel() {
        return panel;
    }

    private void addNotes(ActionEvent e) {
        JTextArea textArea = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(panel, scrollPane, "Enter new notes",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        String note = textArea.getText().trim();
        if (result == JOptionPane.OK_OPTION && !note.isEmpty()) {
            if (notesStorage.getNotes().contains(new Notes(note))) {
                JOptionPane.showMessageDialog(null, "Notes already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                notesStorage.addNotes(new Notes(note));
                updateNotesTable();
            }
        }
    }

    private void deleteNotes(ActionEvent e) {
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a notes to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedRow >= 0) {
            notesStorage.deleteNotes(selectedRow);
            updateNotesTable();
        }
    }

    private void editNotes(ActionEvent e) {
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a notes to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String currentNote = (String) tableModel.getValueAt(selectedRow, 1);

        JTextArea textArea = new JTextArea(5, 30);
        textArea.setText(currentNote);
        JScrollPane scrollPane = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(panel, scrollPane, "Edit notes",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        String newNote = textArea.getText().trim();
        if (result == JOptionPane.OK_OPTION && !newNote.isEmpty()) {
            notesStorage.updateNotes(selectedRow, new Notes(newNote));
            updateNotesTable();
        } else if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(panel, "Notes cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateNotesTable() {
        tableModel.setRowCount(0);
        List<Notes> notes = notesStorage.getNotes();
        for (int i = 0; i < notes.size(); i++) {
            tableModel.addRow(new Object[]{i + 1, notes.get(i).notes()});
        }
    }

    private void importNotes(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(file.toPath())).trim();

                if (!content.startsWith("[") || !content.endsWith("]")) {
                    JOptionPane.showMessageDialog(null, "Invalid JSON format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                content = content.substring(1, content.length() - 1).trim();
                String[] rawNotes = content.split("},");

                List<Notes> importedNotes = new ArrayList<>();

                for (String rawNote : rawNotes) {
                    rawNote = rawNote.trim();
                    if (!rawNote.endsWith("}")) {
                        rawNote += "}";
                    }

                    int startIdx = rawNote.indexOf("\"notes\":") + 8;
                    if (startIdx == 7) continue;

                    int firstQuote = rawNote.indexOf("\"", startIdx);
                    int lastQuote = rawNote.lastIndexOf("\"");

                    if (firstQuote == -1 || lastQuote == -1 || firstQuote == lastQuote) continue;

                    String noteText = rawNote.substring(firstQuote + 1, lastQuote);
                    importedNotes.add(new Notes(noteText));
                }

                if (importedNotes.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid or empty JSON file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (Notes note : importedNotes) {
                    if(!notesStorage.getNotes().contains(note)) {
                        notesStorage.addNotes(note);
                    }
                }

                updateNotesTable();

                JOptionPane.showMessageDialog(null, "Notes imported successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error importing notes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void exportNotes(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }

            try (FileWriter writer = new FileWriter(file)) {
                List<Notes> notesList = notesStorage.getNotes();

                if (notesList == null || notesList.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No notes to export!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("[\n");

                for (int i = 0; i < notesList.size(); i++) {
                    jsonBuilder.append("  { \"notes\": \"")
                            .append(notesList.get(i).notes().replace("\"", "\\\""))
                            .append("\" }");

                    if (i < notesList.size() - 1) {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("\n");
                }

                jsonBuilder.append("]");

                writer.write(jsonBuilder.toString());
                JOptionPane.showMessageDialog(null, "Notes exported successfully to: " + file.getAbsolutePath());

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error exporting notes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}