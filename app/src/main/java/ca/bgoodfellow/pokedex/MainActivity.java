package ca.bgoodfellow.pokedex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView lvPokemon;
    private PokemonAdapter pokemonAdapter;
    private ArrayList<Pokemon> pokemonList;
    private DatabaseHelper dbHelper;
    private Button loadData, additionalData;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;

    private static final int NUMBER_OF_POKEMON = 802;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);

        //progressBar = findViewById(R.id.pbLoadingData);
        //loadData = findViewById(R.id.btnLoadData);

        dbHelper = new DatabaseHelper(this);

        pokemonList = new ArrayList<>();

        lvPokemon = findViewById(R.id.lvPokemon);
        loadData = findViewById(R.id.btnLoadData);

        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pokemonList = dbHelper.loadData();
                    pokemonAdapter = new PokemonAdapter(MainActivity.this, R.layout.list_item, pokemonList);
                    lvPokemon.setAdapter(pokemonAdapter);
            }
        });
        additionalData = findViewById(R.id.btnAdditionalData);
        additionalData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pokemonList.size() == NUMBER_OF_POKEMON) {
                    additionalData.setVisibility(additionalData.INVISIBLE);
                }
                else
                {
                    dbHelper.additionalInsert(pokemonList.size());
                    Toast.makeText(MainActivity.this, "Connecting to https://pokeapi.co/", Toast.LENGTH_SHORT).show();
                }

            }
        });

        EventHandler handler = new EventHandler();

        lvPokemon.setOnItemClickListener(handler);
    }

    // !----------------------------Methods-------------------------------!

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.help:
                //TODO: Create a help activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startPokemonActivity(View view) {
        Intent intent = new Intent(view.getContext(), PokemonActivity.class);

        TextView entry = (TextView) view.findViewById(R.id.tvEntry);
        TextView name = (TextView) view.findViewById(R.id.tvName);
        TextView type = (TextView) view.findViewById(R.id.tvType);
        TextView hp = (TextView) view.findViewById(R.id.tvHP);
        TextView atk = (TextView) view.findViewById(R.id.tvAtk);
        TextView def = (TextView) view.findViewById(R.id.tvDef);
        TextView spAtk = (TextView) view.findViewById(R.id.tvSpAtk);
        TextView spDef = (TextView) view.findViewById(R.id.tvSpDef);
        TextView spe = (TextView) view.findViewById(R.id.tvSpe);

        intent.putExtra("entry", entry.getText().toString());
        intent.putExtra("name", name.getText().toString());
        intent.putExtra("type", type.getText().toString());
        intent.putExtra("hp", hp.getText().toString());
        intent.putExtra("atk", atk.getText().toString());
        intent.putExtra("def", def.getText().toString());
        intent.putExtra("spAtk", spAtk.getText().toString());
        intent.putExtra("spDef", spDef.getText().toString());
        intent.putExtra("spe", spe.getText().toString());

        startActivity(intent);
    }
    // !------------------------Inner Classes-----------------------------!

    class EventHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getId() == R.id.lvPokemon) {
                startPokemonActivity(view);
            }
        }
    }

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
                String color = p.getColor();
                v.setBackgroundColor(Color.parseColor(color));
                TextView entry = (TextView) v.findViewById(R.id.tvEntry);
                TextView name = (TextView) v.findViewById(R.id.tvName);
                TextView type = (TextView) v.findViewById(R.id.tvType);
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
                    String capitalizedName = p.getName().substring(0,1).toUpperCase() + p.getName().substring(1);
                    name.setText(capitalizedName);
                }
                if (type != null) {
                    String type1, type2;
                    if (p.getTypes().length > 1) {
                        type1 = p.getTypes()[1].substring(0,1).toUpperCase() + p.getTypes()[1].substring(1);
                        type2 = p.getTypes()[0].substring(0,1).toUpperCase() + p.getTypes()[0].substring(1);
                        type.setText(type1 + ", " + type2);
                    }
                    else {
                        type1 = p.getTypes()[0].substring(0,1).toUpperCase() + p.getTypes()[0].substring(1);
                        type.setText(type1);
                    }
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
}
