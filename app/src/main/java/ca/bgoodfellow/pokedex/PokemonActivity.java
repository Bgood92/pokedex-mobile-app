package ca.bgoodfellow.pokedex;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class PokemonActivity extends AppCompatActivity {
    private TextView tvEntryResult, tvNameResult, tvTypeResult,
                    tvHPResult, tvAtkResult, tvDefResult,
                    tvSpAtkResult, tvSpDefResult, tvSpeResult;
    private ImageView ivPokemonImg;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);


        tvEntryResult = findViewById(R.id.tvEntryResult);
        tvNameResult = findViewById(R.id.tvNameResult);
        tvTypeResult = findViewById(R.id.tvTypeResult);
        tvHPResult = findViewById(R.id.tvHPResult);
        tvAtkResult = findViewById(R.id.tvAtkResult);
        tvDefResult = findViewById(R.id.tvDefResult);
        tvSpAtkResult = findViewById(R.id.tvSpAtkResult);
        tvSpDefResult = findViewById(R.id.tvSpDefResult);
        tvSpeResult = findViewById(R.id.tvSpeResult);
        ivPokemonImg = findViewById(R.id.ivPokemonImg);


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
        tvHPResult.setText(hp);
        tvAtkResult.setText(atk);
        tvDefResult.setText(def);
        tvSpAtkResult.setText(spAtk);
        tvSpDefResult.setText(spDef);
        tvSpeResult.setText(spe);

        Glide
            .with(this)
            .load("http://play.pokemonshowdown.com/sprites/xyani/" + name.toLowerCase() + ".gif")
            .asGif()
            .error(R.drawable.pokeball)
            .into(ivPokemonImg);


        switch (name) {
            case "Nidoran-m":
                name = "Nidoran_m";
                break;
            case "Nidoran-f":
                name = "Nidoran_f";
                break;
        }

        playSound(name.toLowerCase());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    void playSound(String dir){
        String filename = "android.resource://" + this.getPackageName() + "/raw/" + dir;

        mp = new MediaPlayer();
        try { mp.setDataSource(this, Uri.parse(filename)); } catch (Exception e) {}
        try { mp.prepare(); } catch (Exception e) {}
        mp.start();
    }
}
