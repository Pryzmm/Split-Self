package com.pryzmm.splitself.forge;

import java.util.*;
import net.minecraft.core.BlockPos;

final class EmptyStructureRules{
    static final List<String> NAMES=List.of("es_house","es_burial","es_mines","es_pillar","es_creeper","es_escape");
    static int radius(String name){return name.equals("es_escape")?6:name.equals("es_pillar")?4:8;}
    static BlockPos origin(String name,BlockPos flat){return switch(name){case "es_house"->flat.offset(-10,-1,-10);case "es_mines"->flat.offset(-10,-12,-10);case "es_creeper"->flat.below(3);case "es_escape"->new BlockPos(flat.getX(),20,flat.getZ());default->flat.below();};}
    private EmptyStructureRules(){}
}
