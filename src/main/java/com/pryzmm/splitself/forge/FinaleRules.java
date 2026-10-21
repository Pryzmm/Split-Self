package com.pryzmm.splitself.forge;

import java.util.*;
import java.util.function.DoubleSupplier;

final class FinaleRules {
    enum Effect{CLOUDS,SKY,HOTBAR,HOTBAR_SIZE,HEALTH,FOOD,ARMOR,EXPERIENCE,SCREEN,ITEMS,UI}
    static int[] dialogueTicks(String mode){return switch(mode){case "INTRO"->new int[]{100,190,280,370,460};case "DELETE"->new int[]{30,120,270,1800};case "CANCEL"->new int[]{30,110};default->new int[0];};}
    static int delay(Effect effect,boolean on,double random){return (int)(effect==Effect.SCREEN?(on?10+random*30:100+random*200):random*(on?30:50));}
    static final class Cycle{
        final Effect effect;long due;boolean on,finished;
        Cycle(Effect effect,long now,double random){this.effect=effect;due=now+Math.max(1,delay(effect,false,random));}
        /** Zero-delay callbacks advance on the next tick, never recurse in the current tick. */
        boolean advance(long tick,DoubleSupplier random,java.util.function.BiConsumer<Effect,Boolean> change){
            if(finished||tick<due)return false;on=!on;change.accept(effect,on);
            if(effect==Effect.SCREEN&&!on){finished=true;return true;} // Kept source bug: screen restarts experience, not screen.
            due=tick+Math.max(1,delay(effect,on,random.getAsDouble()));return false;
        }
    }
    private FinaleRules(){}
}
