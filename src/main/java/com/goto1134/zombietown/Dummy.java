package com.goto1134.zombietown;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Created by Andrew
 * on 08.12.2016.
 */
public class Dummy extends Human {

    @Override
    protected void onZombieArrived(AID zombie) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(zombie);
        send(message);
    }

}
