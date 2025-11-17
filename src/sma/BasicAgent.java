package sma;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import java.awt.Point;
import java.util.Random;

public class BasicAgent extends Agent {

    private Point position;
    private int stonesCarried = 0;
    private static final int CARRY_CAPACITY = 10;
    private Environment env;
    private Random rand = new Random();
    private String currentIntention;

    private static final String STATE_EXPLORE = "Explore";
    private static final String STATE_COLLECT = "Collect";
    private static final String STATE_RETURN = "ReturnToShip";

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

        int dx = rand.nextInt(3) - 1;
        int dy = rand.nextInt(3) - 1;
        Point nextPosition = new Point(position.x + dx, position.y + dy);

        if (!env.isObstacle(nextPosition)) {
            position = nextPosition;
            env.moveAgent(getLocalName(), position);
        }
    }

    private void doCollect() {
        if (stonesCarried >= CARRY_CAPACITY) {
            currentIntention = STATE_RETURN;
            return;
        }
        if (env.getStonesAt(position) == 0) {
            currentIntention = STATE_EXPLORE;
            return;
        }
        if (env.pickUpStone(position)) {
            stonesCarried++;
        }
    }

    private void doReturnToShip() {
        if (position.distance(env.getShipPosition()) <= 1) {
            stonesCarried = 0;
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