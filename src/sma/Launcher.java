package sma;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import javax.swing.SwingUtilities;

public class Launcher extends Agent {

    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length < 3) {
            System.err.println("Erreur: arguments de configuration manquants.");
            doDelete();
            return;
        }

        String scenario = (String) args[0];
        int numAgents = (int) args[1];
        int numPiles = (int) args[2];

        Environment.initialize(scenario, numPiles);

        String agentClass;
        switch (scenario) {
            case "2. Stigmergie & Obstacles":
                agentClass = "sma.AgentMissionnaire";
                break;
            case "3. Gestion de l'Énergie":
                agentClass = "sma.EnergyAgent";
                break;
            case "4. Environnement Dynamique (Tempêtes)":
                agentClass = "sma.AgentMissionnaire";
                break;
            default:
                agentClass = "sma.BasicAgent";
                break;
        }

        final String finalScenario = scenario;
        SwingUtilities.invokeLater(() -> {
            PlanetGUI gui = new PlanetGUI("Simulation: " + finalScenario);
            gui.setVisible(true);
        });

        ContainerController container = getContainerController();
        for (int i = 0; i < numAgents; i++) {
            try {
                String agentName = "missionnaire-" + i;
                AgentController agent = container.createNewAgent(agentName, agentClass, null);
                agent.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.out.println(numAgents + " agents de type " + agentClass + " créés.");
        doDelete();
    }
}