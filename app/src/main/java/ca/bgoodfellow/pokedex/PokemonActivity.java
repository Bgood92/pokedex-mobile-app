package ca.bgoodfellow.pokedex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class PokemonActivity extends AppCompatActivity {
    private TextView tvEntryResult, tvNameResult, tvTypeResult, tvGenResult,
                    tvHPResult, tvAtkResult, tvDefResult,
                    tvSpAtkResult, tvSpDefResult, tvSpeResult;
    private ImageView ivPokeball;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        tvEntryResult = findViewById(R.id.tvEntryResult);
        tvNameResult = findViewById(R.id.tvNameResult);
        tvTypeResult = findViewById(R.id.tvTypeResult);
        tvGenResult = findViewById(R.id.tvGenResult);
        tvHPResult = findViewById(R.id.tvHPResult);
        tvAtkResult = findViewById(R.id.tvAtkResult);
        tvDefResult = findViewById(R.id.tvDefResult);
        tvSpAtkResult = findViewById(R.id.tvSpAtkResult);
        tvSpDefResult = findViewById(R.id.tvSpDefResult);
        tvSpeResult = findViewById(R.id.tvSpeResult);


        String entry = getIntent().getStringExtra("entry");
        String name = getIntent().getStringExtra("name");
        String type = getIntent().getStringExtra("type");
        String gen = getIntent().getStringExtra("gen");
        String hp = getIntent().getStringExtra("hp");
        String atk = getIntent().getStringExtra("atk");
        String def = getIntent().getStringExtra("def");
        String spAtk = getIntent().getStringExtra("spAtk");
        String spDef = getIntent().getStringExtra("spDef");
        String spe = getIntent().getStringExtra("spe");

        tvEntryResult.setText("#" + entry);
        tvNameResult.setText(name);
        tvTypeResult.setText(type);
        tvGenResult.setText(gen);
        tvHPResult.setText(hp);
        tvAtkResult.setText(atk);
        tvDefResult.setText(def);
        tvSpAtkResult.setText(spAtk);
        tvSpDefResult.setText(spDef);
        tvSpeResult.setText(spe);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
