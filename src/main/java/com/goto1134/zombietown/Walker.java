package com.goto1134.zombietown;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.goto1134.zombietown.WalkerType.HUMAN;
import static com.goto1134.zombietown.WalkerType.ZOMBIE;
import static jade.lang.acl.MessageTemplate.*;

/**
 * Created by Andrew
 * on 08.12.2016.
 */
abstract class Walker extends Agent {

    @SuppressWarnings("WeakerAccess")
    protected static final Logger cat = LoggerFactory.getLogger(Walker.class);
    private AID jadeTopic;
    private WalkerType type;

    Walker(WalkerType type) {
        super();
        this.type = type;
    }

    @Override
    protected void setup() {
        try {
            TopicManagementHelper topicHelper = (TopicManagementHelper)
                    getHelper(TopicManagementHelper.SERVICE_NAME);
            jadeTopic = topicHelper.createTopic("ZombieTown");
            //регистрируемся в топике
            topicHelper.register(jadeTopic);

            addBehaviour(new TopicReceiverBehaviour());
            addBehaviour(new FightRequestBehaviour());
            addBehaviour(new InformDummyReceiver());
            sendStatusMessage();
            cat.info("Arrived");
        } catch (ServiceException e) {
            cat.error("Could not notify", e);
        }
    }

    private void sendStatusMessage() {
        AID jadeTopic = this.jadeTopic;
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        sendTypedMessage(jadeTopic, msg);
    }

    private void sendTypedMessage(AID target, ACLMessage msg) {
        msg.addReceiver(target);
        try {
            msg.setContentObject(getType());
            send(msg);
        } catch (IOException e) {
            cat.error("Could not notify", e);
        }
    }

    private WalkerType getType() {
        return type;
    }

    private void setType(WalkerType type) {
        this.type = type;
    }

    @Override
    protected void takeDown() {
        cat.info("Died");
    }

    private void onHumanArrived(AID human) {
        if (getType().equals(ZOMBIE)) {
            sendKillMessage(human);
        }
    }

    void sendKillMessage(AID target) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        sendTypedMessage(target, msg);
        cat.info("Request kill " + target.getName());
    }

    protected void onZombieArrived(AID zombie) {
    }

    protected void onAttackedByZombie(AID zombie) {
        turn();
    }

    void turn() {
        if (type == HUMAN) {
            cat.info("Turned");
            setType(ZOMBIE);
            sendStatusMessage();
        }
    }

    private class TopicReceiverBehaviour extends CyclicBehaviour {
        private final MessageTemplate newArrivals = MatchTopic(jadeTopic);

        TopicReceiverBehaviour() {
            super(Walker.this);
        }

        public void action() {
            ACLMessage msg = myAgent.receive(newArrivals);
            if (msg != null) {
                WalkerType contentObject = null;
                try {
                    contentObject = (WalkerType) msg.getContentObject();
                } catch (UnreadableException e) {
                    cat.error("", e);
                }
                if (!getType().equals(contentObject)) {
                    if (HUMAN.equals(contentObject)) {
                        onHumanArrived(msg.getSender());
                    } else {
                        onZombieArrived(msg.getSender());
                    }
                }
            } else {
                block();
            }
        }
    }

    public class FightRequestBehaviour extends CyclicBehaviour {

        private MessageTemplate fightRequest = MatchPerformative(ACLMessage.REQUEST);

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(fightRequest);
            if (msg != null) {
                try {
                    WalkerType contentObject = (WalkerType) msg.getContentObject();
                    if (!getType().equals(contentObject)) {
                        if (getType().equals(ZOMBIE)) {
                            doDelete();
                        } else {
                            onAttackedByZombie(msg.getSender());
                        }
                    }

                } catch (UnreadableException e) {
                    cat.error("", e);
                }
            } else {
                block();
            }
        }
    }

    public class InformDummyReceiver extends CyclicBehaviour {

        private MessageTemplate dummyInform = and(MatchPerformative(ACLMessage.INFORM), not(MatchTopic(jadeTopic)));

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(dummyInform);
            if (msg != null) {
                if (getType().equals(ZOMBIE)) {
                    sendKillMessage(msg.getSender());
                }
            } else {
                block();
            }
        }
    }
}
