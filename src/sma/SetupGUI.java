package sma;

import javax.swing.*;
import java.awt.*;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class SetupGUI extends JFrame {

    public SetupGUI() {
        setTitle("Configuration de la Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 2, 10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("Scénario :"));
        String[] scenarios = {
            "1. Standard (Avec Obstacles)",
            "2. Stigmergie & Obstacles",
            "3. Gestion de l'Énergie",
            "4. Environnement Dynamique (Tempêtes)"
        };
        JComboBox<String> scenarioComboBox = new JComboBox<>(scenarios);
        add(scenarioComboBox);

        add(new JLabel("Nombre d'agents :"));
        JSlider agentSlider = new JSlider(1, 200, 50);
        agentSlider.setMajorTickSpacing(50);
        agentSlider.setMinorTickSpacing(10);
        agentSlider.setPaintTicks(true);
        agentSlider.setPaintLabels(true);
        add(agentSlider);

        add(new JLabel("Nombre de tas de pierres :"));
        JSlider stoneSlider = new JSlider(1, 20, 5);
        stoneSlider.setMajorTickSpacing(5);
        stoneSlider.setPaintTicks(true);
        stoneSlider.setPaintLabels(true);
        add(stoneSlider);
        
        JButton launchButton = new JButton("Lancer la Simulation");
        launchButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(new JLabel());
        add(launchButton);

        launchButton.addActionListener(e -> {
            String selectedScenario = (String) scenarioComboBox.getSelectedItem();
            int numAgents = agentSlider.getValue();
            int numPiles = stoneSlider.getValue();
            
            System.out.println("Lancement avec la configuration suivante :");
            System.out.println("- Scénario: " + selectedScenario);
            System.out.println("- Agents: " + numAgents);
            System.out.println("- Tas de pierres: " + numPiles);
            
            new Thread(() -> launchJade(selectedScenario, numAgents, numPiles)).start();
            dispose();
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void launchJade(String scenario, int numAgents, int numPiles) {
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            Object[] args = { scenario, numAgents, numPiles };
            AgentController launcher = mainContainer.createNewAgent("launcher", "sma.Launcher", args);
            launcher.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}