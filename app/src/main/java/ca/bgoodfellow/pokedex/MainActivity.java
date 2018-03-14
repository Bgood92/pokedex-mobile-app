package ca.bgoodfellow.pokedex;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

        lvPokemon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), PokemonActivity.class);

                TextView entry = (TextView) view.findViewById(R.id.tvEntry);
                TextView name = (TextView) view.findViewById(R.id.tvName);
                TextView type = (TextView) view.findViewById(R.id.tvType);
                TextView gen = (TextView) view.findViewById(R.id.tvGen);
                TextView hp = (TextView) view.findViewById(R.id.tvHP);
                TextView atk = (TextView) view.findViewById(R.id.tvAtk);
                TextView def = (TextView) view.findViewById(R.id.tvDef);
                TextView spAtk = (TextView) view.findViewById(R.id.tvSpAtk);
                TextView spDef = (TextView) view.findViewById(R.id.tvSpDef);
                TextView spe = (TextView) view.findViewById(R.id.tvSpe);

                intent.putExtra("entry", entry.getText().toString());
                intent.putExtra("name", name.getText().toString());
                intent.putExtra("type", type.getText().toString());
                intent.putExtra("gen", gen.getText().toString());
                intent.putExtra("hp", hp.getText().toString());
                intent.putExtra("atk", atk.getText().toString());
                intent.putExtra("def", def.getText().toString());
                intent.putExtra("spAtk", spAtk.getText().toString());
                intent.putExtra("spDef", spDef.getText().toString());
                intent.putExtra("spe", spe.getText().toString());

                startActivity(intent);
            }
        });
    }

    private void generatePokemon(ArrayList<Pokemon> pokemonList)
    {
        pokemonList.add(new Pokemon("Bulbasaur", "Grass", "Poison", 1, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Ivysaur", "Grass", "Poison", 2, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Veuasaur", "Grass", "Poison", 3, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Charmander", "Fire", null, 4, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Charmeleon", "Fire", null, 5, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Charizard", "Fire", "Flying", 6, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Squirtle", "Water", null, 7, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Wartortle", "Water", null, 8, 1, 11,11,11,11,11,11));
        pokemonList.add(new Pokemon("Blastoise", "Water", null, 9, 1, 11,11,11,11,11,11));
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
                TextView type = (TextView) v.findViewById(R.id.tvType);
                TextView gen = (TextView) v.findViewById(R.id.tvGen);
                TextView hp = (TextView) v.findViewById(R.id.tvHP);
                TextView atk = (TextView) v.findViewById(R.id.tvAtk);
                TextView def = (TextView) v.findViewById(R.id.tvDef);
                TextView spAtk = (TextView) v.findViewById(R.id.tvSpAtk);
                TextView spDef = (TextView) v.findViewById(R.id.tvSpDef);
                TextView spe = (TextView) v.findViewById(R.id.tvSpe);
                if (entry != null) {
                    entry.setText(String.valueOf(p.getEntry()));
                }
                if (name != null) {
                    name.setText(p.getName());
                }
                if (type != null) {
                    if (p.getSecondType() != null && p.getSecondType() != "") {
                        type.setText(p.getFirstType() + ", " + p.getSecondType());
                    }
                    else {
                        type.setText(p.getFirstType());
                    }
                }
                if (gen != null) {
                    gen.setText(String.valueOf(p.getGeneration()));
                }
                if (hp != null) {
                    hp.setText(String.valueOf(p.getHp()));
                }
                if (atk != null) {
                    atk.setText(String.valueOf(p.getAtk()));
                }
                if (def != null) {
                    def.setText(String.valueOf(p.getDef()));
                }
                if (spAtk != null) {
                    spAtk.setText(String.valueOf(p.getSpAtk()));
                }
                if (spDef != null) {
                    spDef.setText(String.valueOf(p.getSpDef()));
                }
                if (spe != null) {
                    spe.setText(String.valueOf(p.getSpe()));
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
            return this.name;
        }

        public String getFirstType() {
            return this.firstType;
        }

        public String getSecondType() {
            return this.secondType;
        }

        public int getEntry() {
            return this.entry;
        }

        public int getGeneration() {
            return this.generation;
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
}
