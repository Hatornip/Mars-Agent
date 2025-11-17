package sma;

import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.Timer;
import java.awt.event.ActionListener;

public class Environment {

    private static Environment instance;

    public volatile boolean isPaused = false;
    public volatile int agentTickDelay = 150;

    private boolean obstaclesEnabled = false;
    private boolean beaconsEnabled = false;
    private boolean stormEnabled = false;
    private int numStonePiles = 5;

    private Timer decayTimer;
    private Timer stormScheduler;

    private boolean isStormActive = false;
    public static final int GRID_SIZE = 100;
    private final Point shipPosition;
    private final int[][] stones;
    private final boolean[][] obstacles;
    private final float[][] beaconSignals;

    private final Map<String, Point> agentPositions;
    private final Map<String, String> agentStates;
    private final Map<String, Integer> agentEnergyLevels;
    public static final int MAX_ENERGY = 1500;

    private static final float BEACON_DECAY_RATE = 0.99f;
    private static final float STORM_DECAY_RATE = 0.50f;
    private static final int BEACON_DECAY_INTERVAL_MS = 1000;
    private static final int STORM_INTERVAL_MS = 90000;
    private static final int STORM_DURATION_MS = 20000;

    private Environment(String scenario, int numPiles) {
        this.numStonePiles = numPiles;

        if (scenario.contains("Obstacles") || scenario.contains("Énergie") || scenario.contains("Tempêtes"))
            this.obstaclesEnabled = true;
        if (scenario.contains("Stigmergie") || scenario.contains("Énergie") || scenario.contains("Tempêtes"))
            this.beaconsEnabled = true;
        if (scenario.contains("Tempêtes"))
            this.stormEnabled = true;

        this.shipPosition = new Point(GRID_SIZE / 2, GRID_SIZE / 2);
        this.stones = new int[GRID_SIZE][GRID_SIZE];
        this.obstacles = new boolean[GRID_SIZE][GRID_SIZE];
        this.beaconSignals = new float[GRID_SIZE][GRID_SIZE];
        this.agentPositions = new ConcurrentHashMap<>();
        this.agentStates = new ConcurrentHashMap<>();
        this.agentEnergyLevels = new ConcurrentHashMap<>();

        if (obstaclesEnabled)
            initializeObstacles();
        initializeStones();

        if (beaconsEnabled) {
            decayTimer = new Timer(BEACON_DECAY_INTERVAL_MS, e -> decayBeaconSignals());
            decayTimer.start();
        }
        if (stormEnabled) {
            stormScheduler = new Timer(STORM_INTERVAL_MS, e -> startStorm());
            stormScheduler.setInitialDelay(30000);
            stormScheduler.start();
        }
    }

    public void pauseSimulation() {
        if (!isPaused) {
            isPaused = true;
            if (decayTimer != null)
                decayTimer.stop();
            if (stormScheduler != null)
                stormScheduler.stop();
            System.out.println("--- SIMULATION EN PAUSE ---");
        }
    }

    public void resumeSimulation() {
        if (isPaused) {
            isPaused = false;
            if (decayTimer != null)
                decayTimer.start();
            if (stormScheduler != null)
                stormScheduler.start();
            System.out.println("--- SIMULATION REPREND ---");
        }
    }

    public static synchronized void initialize(String scenario, int numPiles) {
        instance = new Environment(scenario, numPiles);
    }

    public static synchronized Environment getInstance() {
        return instance;
    }

    private void startStorm() {
        if (isPaused)
            return;
        System.out.println("ALERTE: Une tempete de poussiere se leve ! Les signaux des balises sont brouilles.");
        this.isStormActive = true;

        ActionListener stopStormAction = event -> {
            System.out.println("La tempete de poussiere se calme. Les signaux sont de nouveau clairs.");
            this.isStormActive = false;
        };
        Timer stopStormTimer = new Timer(STORM_DURATION_MS, stopStormAction);
        stopStormTimer.setRepeats(false);
        stopStormTimer.start();
    }

    public boolean isStormActive() {
        return isStormActive;
    }

    private synchronized void decayBeaconSignals() {
        float currentDecayRate = isStormActive ? STORM_DECAY_RATE : BEACON_DECAY_RATE;
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                beaconSignals[x][y] *= currentDecayRate;
                if (beaconSignals[x][y] < 0.01f)
                    beaconSignals[x][y] = 0;
            }
        }
    }

    private void initializeObstacles() {
        Random rand = new Random();
        int numRanges = 10 + rand.nextInt(6);
        for (int i = 0; i < numRanges; i++) {
            Point current = new Point(rand.nextInt(GRID_SIZE), rand.nextInt(GRID_SIZE));
            int length = 20 + rand.nextInt(30);
            int thickness = 2;
            for (int j = 0; j < length; j++) {
                for (int tx = -thickness / 2; tx <= thickness / 2; tx++) {
                    for (int ty = -thickness / 2; ty <= thickness / 2; ty++) {
                        int obsX = current.x + tx;
                        int obsY = current.y + ty;
                        if (obsX >= 0 && obsX < GRID_SIZE && obsY >= 0 && obsY < GRID_SIZE) {
                            if (new Point(obsX, obsY).distance(shipPosition) > 2)
                                obstacles[obsX][obsY] = true;
                        }
                    }
                }
                current.translate(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
            }
        }
    }

    private void initializeStones() {
        Random rand = new Random();
        for (int i = 0; i < this.numStonePiles; i++) {
            int pileX = rand.nextInt(GRID_SIZE);
            int pileY = rand.nextInt(GRID_SIZE);
            if (isObstacle(new Point(pileX, pileY)) || new Point(pileX, pileY).distance(shipPosition) < 15) {
                i--;
                continue;
            }
            int pileSize = 100 + rand.nextInt(150);
            stones[pileX][pileY] = pileSize;
        }
    }

    public boolean isObstacle(Point position) {
        if (!obstaclesEnabled)
            return false;
        if (position.x < 0 || position.x >= GRID_SIZE || position.y < 0 || position.y >= GRID_SIZE)
            return true;
        return obstacles[position.x][position.y];
    }

    public int getStonesAt(Point position) {
        return stones[position.x][position.y];
    }

    public double getSignalStrength(Point position) {
        double distance = position.distance(shipPosition);
        return distance == 0 ? Double.MAX_VALUE : 1000.0 / distance;
    }

    public Point getGradientDirection(Point currentPos) {
        Point bestMove = new Point(0, 0);
        double maxSignal = -1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                Point nextPos = new Point(currentPos.x + dx, currentPos.y + dy);
                if (!isObstacle(nextPos)) {
                    double signal = getSignalStrength(nextPos);
                    if (signal > maxSignal) {
                        maxSignal = signal;
                        bestMove.setLocation(dx, dy);
                    }
                }
            }
        }
        return bestMove;
    }

    public float getBeaconSignalAt(Point position) {
        if (!beaconsEnabled || position.x < 0 || position.x >= GRID_SIZE || position.y < 0 || position.y >= GRID_SIZE)
            return 0;
        return beaconSignals[position.x][position.y];
    }

    public Map<Point, Float> getNearbyBeaconSignals(Point currentPos) {
        Map<Point, Float> signals = new HashMap<>();
        if (!beaconsEnabled)
            return signals;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                Point nextPos = new Point(currentPos.x + dx, currentPos.y + dy);
                if (!isObstacle(nextPos)) {
                    float signalStrength = beaconSignals[nextPos.x][nextPos.y];
                    if (signalStrength > 0)
                        signals.put(new Point(dx, dy), signalStrength);
                }
            }
        }
        return signals;
    }

    public void registerAgent(String agentName) {
        agentPositions.put(agentName, new Point(shipPosition));
        agentStates.put(agentName, "EXPLORE");
        agentEnergyLevels.put(agentName, MAX_ENERGY);
    }

    public void updateAgentState(String agentName, String state) {
        agentStates.put(agentName, state);
    }

    public void moveAgent(String agentName, Point newPosition) {
        agentPositions.put(agentName, newPosition);
    }

    public boolean pickUpStone(Point position) {
        if (stones[position.x][position.y] > 0) {
            stones[position.x][position.y]--;
            return true;
        }
        return false;
    }

    public void dropBeacon(Point position, float amount) {
        if (beaconsEnabled)
            beaconSignals[position.x][position.y] += amount;
    }

    public void updateAgentEnergy(String agentName, int energy) {
        agentEnergyLevels.put(agentName, energy);
    }

    public void removeAgent(String agentName) {
        agentPositions.remove(agentName);
        agentStates.remove(agentName);
        agentEnergyLevels.remove(agentName);
    }

    public Point getShipPosition() {
        return shipPosition;
    }

    public int[][] getStones() {
        return stones;
    }

    public boolean[][] getObstacles() {
        return obstaclesEnabled ? obstacles : null;
    }

    public float[][] getBeaconSignals() {
        return beaconsEnabled ? beaconSignals : null;
    }

    public Map<String, Point> getAgentPositions() {
        return agentPositions;
    }

    public Map<String, String> getAgentStates() {
        return agentStates;
    }

    public Map<String, Integer> getAgentEnergyLevels() {
        return agentEnergyLevels;
    }
}