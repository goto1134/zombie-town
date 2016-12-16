package com.goto1134.zombietown;

import jade.core.AID;

/**
 * Created by Andrew
 * on 08.12.2016.
 */
public class Scared extends Human {
    @Override
    protected void takeDown() {
        cat.info(getName() + " ran away");
    }

    @Override
    protected void onZombieArrived(AID zombie) {
        doDelete();
    }
}
