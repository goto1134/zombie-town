package com.goto1134.zombietown;

import jade.core.AID;

/**
 * Created by Andrew
 * on 08.12.2016.
 */
public class Armed extends Human {
    private int weaponStrength = 4;

    @Override
    protected void onZombieArrived(AID zombie) {
        tryToFight(zombie);
    }

    @Override
    protected void onAttackedByZombie(AID zombie) {
        tryToFight(zombie);
    }

    private void tryToFight(AID zombie) {
        if (--weaponStrength > 0) {
            sendKillMessage(zombie);
        } else {
            turn();
        }
    }
}
