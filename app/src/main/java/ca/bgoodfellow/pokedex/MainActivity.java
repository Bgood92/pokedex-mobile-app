package ca.bgoodfellow.pokedex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView listviewPokemon;
    private ArrayList<Pokemon> pokemonList;
    private DatabaseHelper dbHelper;
    private Button loadData, additionalData;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private ImageView imageViewPokemon;
    boolean isDataLoaded, isDatabaseCreated;
    private TextView loadingText;

    private static final int NUMBER_OF_POKEMON = 802;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);

        progressBar = findViewById(R.id.pbLoadingData);
        loadingText = findViewById(R.id.tvLoadingText);
        listviewPokemon = findViewById(R.id.listviewPokemon);
        imageViewPokemon = findViewById(R.id.imageViewPokemon);

        progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#9f8686"), android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setScaleY(3f);

        dbHelper = new DatabaseHelper(this, listviewPokemon, progressBar, imageViewPokemon, loadingText);
        dbHelper.initiateHelper();

        sharedPreferences = getSharedPreferences("main", 0);

        isDatabaseCreated = sharedPreferences.getBoolean("database_created", false);
        isDataLoaded = sharedPreferences.getBoolean("loaded", false);

        if (isDataLoaded) {
            progressBar.setVisibility(View.INVISIBLE);
            dbHelper.loadData();
            Toast.makeText(this, "Successfully loaded all Pokemon.", Toast.LENGTH_LONG).show();
        }
        else {
            if (isDatabaseCreated)
                dbHelper.reset(progressBar, loadingText);
        }

        EventHandler handler = new EventHandler();
        listviewPokemon.setOnItemClickListener(handler);
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
        ImageView image = (ImageView) view.findViewById(R.id.imageViewPokemon);

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
            if (parent.getId() == R.id.listviewPokemon) {
                startPokemonActivity(view);
            }
        }
    }
}
