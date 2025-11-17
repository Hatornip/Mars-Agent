package sma;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import java.awt.Point;
import java.util.Map;
import java.util.Random;

public class EnergyAgent extends Agent {

    private Point position;
    private int stonesCarried = 0;
    private static final int CARRY_CAPACITY = 10;
    private Environment env;
    private Random rand = new Random();
    private String currentIntention;
    
    private int energyLevel = Environment.MAX_ENERGY;
    private int beaconIgnoreCooldown = 0;
    private static final int BEACON_IGNORE_DURATION = 30;

    private static final String STATE_EXPLORE = "Explore";
    private static final String STATE_COLLECT = "Collect";
    private static final String STATE_RETURN = "ReturnToShip";
    private static final float BEACON_STRENGTH = 20.0f;
    private static final float EXPLORATION_CHANCE = 0.02f;

    protected void setup() {
        this.env = Environment.getInstance();
        this.env.registerAgent(getLocalName());
        this.position = new Point(env.getShipPosition());
        this.currentIntention = STATE_EXPLORE;

        addBehaviour(new WakerBehaviour(this, env.agentTickDelay) {
            protected void onWake() {
                if (env.isPaused) {
                    reset(200);
                    return;
                }
                
                energyLevel--;
                env.updateAgentEnergy(getLocalName(), energyLevel);

                if (energyLevel <= 0) {
                    System.out.println(getLocalName() + " est tombe en panne d'energie !");
                    env.removeAgent(getLocalName());
                    doDelete();
                    return;
                }
                
                double distanceToShip = position.distance(env.getShipPosition());
                int criticalEnergy = (int) (distanceToShip * 1.5);
                
                if (energyLevel < criticalEnergy && !currentIntention.equals(STATE_RETURN)) {
                    System.out.println(getLocalName() + " en mode panique: retour au vaisseau pour recharge !");
                    currentIntention = STATE_RETURN;
                }

                if (beaconIgnoreCooldown > 0) beaconIgnoreCooldown--;
                env.updateAgentState(myAgent.getLocalName(), currentIntention);
                switch (currentIntention) {
                    case STATE_EXPLORE: doExplore(); break;
                    case STATE_COLLECT: doCollect(); break;
                    case STATE_RETURN: doReturnToShip(); break;
                }
                
                reset(env.agentTickDelay);
            }
        });
    }

    private void doExplore() {
        if (env.getStonesAt(position) > 0) {
            currentIntention = STATE_COLLECT;
            return;
        }

        Point moveDirection;

        if (beaconIgnoreCooldown > 0) {
            moveDirection = new Point(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
        } else {
            Map<Point, Float> signals = env.getNearbyBeaconSignals(position);
            if (!signals.isEmpty()) {
                if (rand.nextFloat() < EXPLORATION_CHANCE) {
                    moveDirection = new Point(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
                } else {
                    moveDirection = chooseProbabilisticMove(signals);
                }
            } else {
                moveDirection = new Point(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
            }
            
            if (env.getBeaconSignalAt(position) > BEACON_STRENGTH * 0.5f) {
                beaconIgnoreCooldown = BEACON_IGNORE_DURATION;
                moveDirection = new Point(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
            }
        }
        
        Point nextPosition = new Point(position.x + moveDirection.x, position.y + moveDirection.y);
        if (!env.isObstacle(nextPosition)) {
            position = nextPosition;
            env.moveAgent(getLocalName(), position);
        }
    }
    
    private Point chooseProbabilisticMove(Map<Point, Float> signals) {
        if (signals.isEmpty()) return new Point(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
        float totalSignal = 0;
        for (float signal : signals.values()) totalSignal += signal;
        float randomChoice = rand.nextFloat() * totalSignal;
        float currentSum = 0;
        for (Map.Entry<Point, Float> entry : signals.entrySet()) {
            currentSum += entry.getValue();
            if (randomChoice <= currentSum) return entry.getKey();
        }
        return new Point(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
    }

    private void doCollect() {
        if (stonesCarried >= CARRY_CAPACITY) { currentIntention = STATE_RETURN; return; }
        if (env.getStonesAt(position) == 0) { currentIntention = STATE_EXPLORE; return; }
        if (env.pickUpStone(position)) stonesCarried++;
    }

    private void doReturnToShip() {
        env.dropBeacon(position, BEACON_STRENGTH);
        
        if (position.distance(env.getShipPosition()) <= 1) {
            if (stonesCarried > 0) {
                System.out.println(getLocalName() + " a depose " + stonesCarried + " pierres.");
                stonesCarried = 0;
            }
            System.out.println(getLocalName() + " se recharge au vaisseau.");
            energyLevel = Environment.MAX_ENERGY;
            currentIntention = STATE_EXPLORE;
            return;
        }
        Point gradient = env.getGradientDirection(position);
        Point nextPosition = new Point(position.x + gradient.x, position.y + gradient.y);
        if (gradient.x == 0 && gradient.y == 0 || env.isObstacle(nextPosition)) {
            Point randomNextPos = new Point(position.x + (rand.nextInt(3) - 1), position.y + (rand.nextInt(3) - 1));
            if (!env.isObstacle(randomNextPos)) {
                position = randomNextPos;
                env.moveAgent(getLocalName(), position);
            }
        } else {
            position = nextPosition;
            env.moveAgent(getLocalName(), position);
        }
    }
}