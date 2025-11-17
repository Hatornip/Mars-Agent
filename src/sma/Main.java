package sma;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SetupGUI setupGUI = new SetupGUI();
            setupGUI.setVisible(true);
        });
    }
}