package sma;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;
import java.util.Map;

public class PlanetGUI extends JFrame {

    private final DrawingPanel drawingPanel;
    private static final int CELL_SIZE = 8;

    public PlanetGUI(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Panneau de Contrôle"));
        
        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> Environment.getInstance().pauseSimulation());
        controlPanel.add(pauseButton);
        
        JButton resumeButton = new JButton("Reprendre");
        resumeButton.addActionListener(e -> Environment.getInstance().resumeSimulation());
        controlPanel.add(resumeButton);

        controlPanel.add(new JLabel("Vitesse:"));
        JSlider speedSlider = new JSlider(10, 500, 150);
        speedSlider.setInverted(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(10, new JLabel("Rapide"));
        labelTable.put(250, new JLabel("Normal"));
        labelTable.put(500, new JLabel("Lent"));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintLabels(true);
        speedSlider.setPreferredSize(new Dimension(200, 50));
        
        speedSlider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                Environment.getInstance().agentTickDelay = source.getValue();
            }
        });
        controlPanel.add(speedSlider);
        
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setSize(new Dimension(Environment.GRID_SIZE * CELL_SIZE + 100, Environment.GRID_SIZE * CELL_SIZE + 200));
        setLocationRelativeTo(null);

        Timer refreshTimer = new Timer(50, e -> drawingPanel.repaint());
        refreshTimer.start();
    }

    private static class DrawingPanel extends JPanel {
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Environment env = Environment.getInstance();
            Graphics2D g2d = (Graphics2D) g;

            this.setBackground(Color.BLACK);

            float[][] beaconSignals = env.getBeaconSignals();
            if (beaconSignals != null) {
                for (int y = 0; y < Environment.GRID_SIZE; y++) {
                    for (int x = 0; x < Environment.GRID_SIZE; x++) {
                        if (beaconSignals[x][y] > 0) {
                            float intensity = Math.min(1.0f, beaconSignals[x][y] / 50.0f);
                            g2d.setColor(new Color(1.0f, 1.0f, 1.0f, intensity * 0.5f)); 
                            g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
            }

            boolean[][] obstacles = env.getObstacles();
            if (obstacles != null) {
                g.setColor(new Color(80, 80, 80));
                for (int y = 0; y < Environment.GRID_SIZE; y++) {
                    for (int x = 0; x < Environment.GRID_SIZE; x++) {
                        if (obstacles[x][y]) {
                            g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
            }
            
            int[][] stones = env.getStones();
            for (int y = 0; y < Environment.GRID_SIZE; y++) {
                for (int x = 0; x < Environment.GRID_SIZE; x++) {
                    if (stones[x][y] > 0) {
                        int alpha = Math.min(255, 100 + stones[x][y]);
                        g.setColor(new Color(139, 69, 19, alpha));
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 8));
                        String quantity = String.valueOf(stones[x][y]);
                        g.drawString(quantity, x * CELL_SIZE + 1, y * CELL_SIZE + CELL_SIZE - 1);
                    }
                }
            }

            Point shipPos = env.getShipPosition();
            g.setColor(Color.CYAN);
            g.fillRect(shipPos.x * CELL_SIZE, shipPos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(shipPos.x * CELL_SIZE, shipPos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

            Map<String, Point> agentPositions = env.getAgentPositions();
            Map<String, String> agentStates = env.getAgentStates();
            Map<String, Integer> energyLevels = env.getAgentEnergyLevels();
            
            for (Map.Entry<String, Point> entry : agentPositions.entrySet()) {
                String agentName = entry.getKey();
                Point pos = entry.getValue();
                String state = agentStates.get(agentName);
                if (state != null) {
                    switch (state) {
                        case "Explore": g.setColor(Color.BLUE); break;
                        case "Collect": g.setColor(Color.ORANGE); break;
                        case "ReturnToShip": g.setColor(Color.GREEN); break;
                        default: g.setColor(Color.GRAY); break;
                    }
                } else {
                    g.setColor(Color.GRAY);
                }
                g.fillOval(pos.x * CELL_SIZE, pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                
                if (getTopLevelAncestor() instanceof JFrame && ((JFrame)getTopLevelAncestor()).getTitle().contains("Énergie")) {
                    Integer energy = energyLevels.get(agentName);
                    if (energy != null) {
                        float energyRatio = (float) energy / Environment.MAX_ENERGY;
                        if (energyRatio < 0.25f) g.setColor(Color.RED);
                        else if (energyRatio < 0.6f) g.setColor(Color.YELLOW);
                        else g.setColor(Color.GREEN);
                        int barWidth = (int) (CELL_SIZE * energyRatio);
                        g.fillRect(pos.x * CELL_SIZE, pos.y * CELL_SIZE + CELL_SIZE, barWidth, 2);
                    }
                }
            }
            
            if(env.isStormActive()) {
                g2d.setColor(new Color(0.5f, 0.5f, 0.5f, 0.4f));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            if (env.isPaused) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
                
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 50));
                FontMetrics fm = g.getFontMetrics();
                String pauseText = "PAUSE";
                int x = (getWidth() - fm.stringWidth(pauseText)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(pauseText, x, y);
            }
        }
    }
}