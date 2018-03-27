package ca.bgoodfellow.pokedex;

import java.util.ArrayList;

/**
 * Created by goodf on 2018-03-21.
 */

public class Pokemon {
    private String name;
    private String[] types;
    private int entry, hp, atk, def, spAtk, spDef, spe;

    public Pokemon(String name, String[] types, int entry,
                   int hp, int atk, int def, int spAtk, int spDef, int spe) {
        this.name = name;
        this.types = types;
        this.entry = entry;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.spAtk = spAtk;
        this.spDef = spDef;
        this.spe = spe;
    }

    public String getName() {
        return this.name;
    }

    public String[] getTypes() {
        return this.types;
    }

    public int getEntry() {
        return this.entry;
    }

    public int getHp() {
        return this.hp;
    }

    public int getAtk() {
        return this.atk;
    }

    public int getDef() {
        return this.def;
    }

    public int getSpAtk() {
        return this.spAtk;
    }

    public int getSpDef() {
        return this.spDef;
    }

    public int getSpe() {
        return this.spe;
    }
}
