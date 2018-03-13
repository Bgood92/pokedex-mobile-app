package ca.bgoodfellow.pokedex;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView lvPokemon;
    private PokemonAdapter pokemonAdapter;
    private ArrayList<Pokemon> pokemonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvPokemon = findViewById(R.id.lvPokemon);

        pokemonList = new ArrayList<>();

        generatePokemon(pokemonList);

        pokemonAdapter = new PokemonAdapter(this, R.layout.list_item, pokemonList);

        lvPokemon.setAdapter(pokemonAdapter);
    }

    private void generatePokemon(ArrayList<Pokemon> pokemonList)
    {
        pokemonList.add(new Pokemon("Bulbasaur", "Grass", "Poison", 1, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Charmander", "Fire", null, 1, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Bulbasaur", "Grass", "Poison", 1, 1, 11,11,11,11,11,11));
    }

    //!-------------------------Inner Classes-----------------------------!

    class PokemonAdapter extends ArrayAdapter<Pokemon> {
        private ArrayList<Pokemon> list;

        public PokemonAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Pokemon> pokemon) {
            super(context, resource, pokemon);
            this.list = pokemon;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            Pokemon p = list.get(position);
            if (p != null) {
                TextView entry = (TextView) v.findViewById(R.id.tvEntry);
                TextView name = (TextView) v.findViewById(R.id.tvName);
                if (entry != null) {
                    entry.setText(String.valueOf(p.getEntry()));
                }
                if (name != null) {
                    name.setText(p.getName());
                }
            }
            return v;
        }
    }

    class Pokemon {
        private String name, firstType, secondType;
        private int entry, generation, hp, atk, def, spAtk, spDef, spe;

        public Pokemon(String name, String firstType, String secondType, int entry, int gen,
                       int hp, int atk, int def, int spAtk, int spDef, int spe) {
            this.name = name;
            this.firstType = firstType;
            this.secondType = secondType;
            this.entry = entry;
            this.generation = gen;
            this.hp = hp;
            this.atk = atk;
            this.def = def;
            this.spAtk = spAtk;
            this.spDef = spDef;
            this.spe = spe;
        }

        public String getName() {
            return name;
        }

        public String getFirstType() {
            return firstType;
        }

        public String getSecondType() {
            return secondType;
        }

        public int getEntry() {
            return entry;
        }

        public int getGeneration() {
            return generation;
        }

        public int getHp() {
            return hp;
        }

        public int getAtk() {
            return atk;
        }

        public int getDef() {
            return def;
        }

        public int getSpAtk() {
            return spAtk;
        }

        public int getSpDef() {
            return spDef;
        }

        public int getSpe() {
            return spe;
        }
    }
}
