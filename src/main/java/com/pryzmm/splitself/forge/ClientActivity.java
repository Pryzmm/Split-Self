package com.pryzmm.splitself.forge;

/** Source ACTIVE_EVENT is a shared boolean: the last finishing effect clears it, including overlaps. */
final class ClientActivity {
    private static boolean active;
    static void set(boolean value){active=value;}
    static boolean active(){return active;}
    private ClientActivity(){}
}
