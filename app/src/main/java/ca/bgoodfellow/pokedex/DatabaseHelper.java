package ca.bgoodfellow.pokedex;
/**
 * Created by goodf on 2018-03-19.
 */
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database name and version number
    //Change the following values if the database name or version number changes
    private static final String DATABASE_NAME = "Pokedex";
    private static final int DATABASE_VERSION = 1;

    /*
     * The following constants are only to be changed when Pokemon are added
     * to the Pokedex and/or more types are added
     */
    private static final int NUMBER_OF_POKEMON = 151;
    private static final int NUMBER_OF_TYPES = 18;

    /*
     * Below are SQL Strings used to create the following tables
     * and their column names, as well as the CREATE statements
     */

    //Pokemon table
    private static final String POKEMON_TABLE_NAME = "Pokemon";
    private static final String PKN_ENTRY = "PokedexEntry";
    private static final String PKN_NAME = "Name";
    private static final String PKN_HP = "HP";
    private static final String PKN_ATK = "Attack";
    private static final String PKN_DEF = "Defense";
    private static final String PKN_SP_ATK = "SpecialAttack";
    private static final String PKN_SP_DEF = "SpecialDefense";
    private static final String PKN_SPEED = "Speed";

    //Type table
    private static final String TYPE_TABLE_NAME = "Type";
    private static final String TYPE_ID = "TypeId";
    private static final String TYPE_NAME = "Name";
    private static final String TYPE_COLOR = "Color";

    //PokemonType table (join table between Pokemon and Type)
    //Primary keys for this table will be the Pokemon entry and
    //Type id which are both defined above, which will also be
    //the foreign keys
    private static final String POKEMON_TYPE_TABLE_NAME = "PokemonType";

    private static final String CREATE_POKEMON_TABLE = "CREATE TABLE " + POKEMON_TABLE_NAME + "(" +
            PKN_ENTRY + " INTEGER NOT NULL PRIMARY KEY, " +
            PKN_NAME + " TEXT NOT NULL, " +
            PKN_HP + " INTEGER, " +
            PKN_ATK + " INTEGER, " +
            PKN_DEF + " INTEGER, " +
            PKN_SP_ATK + " INTEGER, " +
            PKN_SP_DEF + " INTEGER, " +
            PKN_SPEED + " INTEGER )";

    private static final String CREATE_TYPE_TABLE = "CREATE TABLE " + TYPE_TABLE_NAME + "(" +
            TYPE_ID + " INTEGER NOT NULL PRIMARY KEY, " +
            TYPE_NAME + " TEXT NOT NULL, " +
            TYPE_COLOR + " TEXT )";

    private static final String CREATE_POKEMON_TYPE_TABLE = "CREATE TABLE " + POKEMON_TYPE_TABLE_NAME + "( " +
            PKN_ENTRY + " INTEGER NOT NULL, " +
            TYPE_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY(" + PKN_ENTRY + "," + TYPE_ID + "), " +
            "FOREIGN KEY(" + PKN_ENTRY + ") REFERENCES " + POKEMON_TABLE_NAME + "(" + PKN_ENTRY + "), " +
            "FOREIGN KEY(" + TYPE_ID + ") REFERENCES " + TYPE_TABLE_NAME + "(" + TYPE_ID + "))";

    private PokemonHttpHandler pokemonHandler;
    private PokemonHttpHandler newPokemonHandler;
    private int pokemonNumber = 1;
    private String typeUrl = "https://pokeapi.co/api/v2/type/";
    private String pokemonURL = "https://pokeapi.co/api/v2/pokemon/";

    /*
     * Drop table statements for all tables
     */
    private static final String DROP_POKEMON_TABLE = "DROP TABLE " + POKEMON_TABLE_NAME;
    private static final String DROP_TYPE_TABLE = "DROP TABLE " + TYPE_TABLE_NAME;
    private static final String DROP_POKEMON_TYPE_TABLE = "DROP TABLE " + POKEMON_TYPE_TABLE_NAME;

    private ProgressBar progressBar;
    private MainActivity mainActivity;
    private PokemonAdapter pokemonAdapter;
    private ArrayList<Pokemon> pokemonList;
    private ListView listviewPokemon;
    private ImageView imageViewPokemon;
    private boolean allDataIsLoaded;
    private SharedPreferences sharedPreferences;
    private boolean isDatabaseCreated;
    private TextView loadingText;

    public DatabaseHelper(Context context, ListView listviewPokemon, ProgressBar progressBar, ImageView imageViewPokemon, TextView loadingText) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mainActivity = (MainActivity)context;
        pokemonList = new ArrayList<>();
        this.listviewPokemon = listviewPokemon;
        this.imageViewPokemon = imageViewPokemon;
        allDataIsLoaded = false;
        this.progressBar = progressBar;
        this.loadingText = loadingText;
        this.loadingText.setText("0/" + NUMBER_OF_POKEMON + " items loaded...");
        sharedPreferences = context.getSharedPreferences("main", 0);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creates the following tables in a local database
        db.execSQL(CREATE_TYPE_TABLE);
        db.execSQL(CREATE_POKEMON_TABLE);
        db.execSQL(CREATE_POKEMON_TYPE_TABLE);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(NUMBER_OF_POKEMON);
        progressBar.setProgress(0);

        //Calls a method to asyncronously fetch data from an online API and insert
        //the retrieved data into the approprate tables
        insert();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Drop all tables and call the onCreate method to recreate the database
        db.execSQL(DROP_POKEMON_TYPE_TABLE);
        db.execSQL(DROP_POKEMON_TABLE);
        db.execSQL(DROP_TYPE_TABLE);
        onCreate(db);
    }

    public void initiateHelper() {
        SQLiteDatabase db = getReadableDatabase();
        db.close();
    }

    public void reset(ProgressBar progressBar, TextView loadingText) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(POKEMON_TYPE_TABLE_NAME, null, null);
        db.delete(POKEMON_TABLE_NAME, null, null);
        db.delete(TYPE_TABLE_NAME, null, null);
        db.close();

        this.progressBar = progressBar;
        this.progressBar.setVisibility(View.VISIBLE);
        this.progressBar.setMax(NUMBER_OF_POKEMON);
        this.progressBar.setProgress(0);

        this.loadingText = loadingText;
        this.loadingText.setText("0/" + NUMBER_OF_POKEMON + " items loaded...");

        insert();
    }

    public ArrayList<Pokemon> loadData()
    {
        ArrayList<Pokemon> pokemon = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] pokemonSelection = { PKN_ENTRY, PKN_NAME, PKN_HP, PKN_ATK, PKN_DEF, PKN_SP_ATK, PKN_SP_DEF, PKN_SPEED};

        Cursor pokemonCursor = db.query(POKEMON_TABLE_NAME, pokemonSelection, null, null, null, null, null);

        pokemonCursor.moveToFirst();

        int entry;
        String name;
        String color;
        int hp, atk, def, spAtk, spDef, speed;
        String[] queryArgs = new String[1];
        String query =  "SELECT t.Name, t.Color" +
                        " FROM Pokemon p JOIN PokemonType pt ON p.PokedexEntry = pt.PokedexEntry" +
                        " JOIN Type t ON pt.TypeId = t.TypeId" +
                        " AND p.Name = ?";

        for (int i = 0; i < pokemonCursor.getCount(); i++) {
            entry = pokemonCursor.getInt(0);
            name = pokemonCursor.getString(1);
            hp = pokemonCursor.getInt(2);
            atk = pokemonCursor.getInt(3);
            def = pokemonCursor.getInt(4);;
            spAtk = pokemonCursor.getInt(5);
            spDef = pokemonCursor.getInt(6);
            speed = pokemonCursor.getInt( 7);

            queryArgs[0] = name;
            Cursor typeCursor = db.rawQuery(query, queryArgs);

            String[] types = new String[typeCursor.getCount()];

            typeCursor.moveToFirst();

            color = "";

            for (int j = 0; j < typeCursor.getCount(); j++) {
                types[j] = typeCursor.getString(0);
                color = typeCursor.getString(1);
                typeCursor.moveToNext();
            }

            pokemonList.add(new Pokemon(name, types, entry, color, hp, atk, def, spAtk, spDef, speed));

            pokemonCursor.moveToNext();
        }

        db.close();

        pokemonAdapter = new PokemonAdapter(mainActivity, R.layout.list_item, pokemonList);
        listviewPokemon.setAdapter(pokemonAdapter);

        return pokemon;
    }

    public void insert() {
        Toast.makeText(mainActivity, "Please wait while we attempt to load all Pokemon.", Toast.LENGTH_LONG).show();

        TypeHttpHandler typeHandler = new TypeHttpHandler();
        typeHandler.execute(typeUrl);

        pokemonHandler = new PokemonHttpHandler();
        pokemonHandler.execute(pokemonURL);
    }

    class PokemonHttpHandler extends AsyncTask {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(Object[] params) {
            Request.Builder builder = new Request.Builder();
            builder.url(pokemonURL + pokemonNumber);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (pokemonNumber <= NUMBER_OF_POKEMON) {
                if (o != null) {
                    parsePokemonResponse(o.toString());

                    isDatabaseCreated = true;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("database_created", isDatabaseCreated);
                    editor.apply();

                    progressBar.setProgress(pokemonNumber);
                    loadingText.setText(pokemonNumber + "/" + NUMBER_OF_POKEMON + " items loaded...");

                    pokemonNumber++;

                    pokemonHandler = new PokemonHttpHandler();
                    pokemonHandler.execute(pokemonURL);
                }
                else {
                    Toast.makeText(mainActivity, "Attempting to reconnect to server.", Toast.LENGTH_SHORT).show();
                    newPokemonHandler = new PokemonHttpHandler();
                    newPokemonHandler.execute(pokemonURL);
                }
            }
            else {
                progressBar.setVisibility(progressBar.INVISIBLE);
                loadingText.setVisibility(loadingText.INVISIBLE);
                allDataIsLoaded = true;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("loaded", allDataIsLoaded);
                editor.apply();

                loadData();

                Toast.makeText(mainActivity, "Successfully loaded all Pokemon.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class TypeHttpHandler extends AsyncTask {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(Object[] params) {
            Request.Builder builder = new Request.Builder();
            builder.url(typeUrl);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (o != null)
                parseTypeResponse(o.toString());

        }
    }

    private void parseTypeResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);

            String[] typeList = new String[NUMBER_OF_TYPES];

            for (int i = 0; i < typeList.length; i++) {
                typeList[i] = json.getJSONArray("results").getJSONObject(i).getString("name");
            }

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues insertValues;

            for (int i = 0; i < typeList.length; i++) {
                insertValues = new ContentValues();
                insertValues.put(TYPE_ID, i+1);
                insertValues.put(TYPE_NAME, typeList[i]);
                db.insert(TYPE_TABLE_NAME, null, insertValues);
            }

            insertColorsIntoTypeTabe(db);

            db.close();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePokemonResponse(String response) {
        try{
            JSONObject json = new JSONObject(response);
            String name = json.getJSONArray("forms").getJSONObject(0).getString("name");

            SQLiteDatabase readableDB = getReadableDatabase();

            String[] args = { name };
            String[] columns = { PKN_NAME };

            Cursor toSeeIfPokemonExists = readableDB.query(POKEMON_TABLE_NAME, columns, "Name LIKE ?", args, null, null, null);
            toSeeIfPokemonExists.moveToFirst();

            if (toSeeIfPokemonExists.getCount() <= 0) {
                JSONArray stats = json.getJSONArray("stats");

                JSONArray types = json.getJSONArray("types");

                int entry = json.getInt("id");

                ArrayList<String> typesList = new ArrayList<>();

                int[] statsList = new int[6];

                for (int i = 0; i < types.length() ; i++) {
                    typesList.add(types.getJSONObject(i).getJSONObject("type").getString("name"));
                }

                for (int i = 0; i < statsList.length; i++) {
                    statsList[i] = stats.getJSONObject(i).getInt("base_stat");
                }
                ContentValues insertValues = new ContentValues();

                insertValues.put(PKN_ENTRY, entry);
                insertValues.put(PKN_NAME, name);
                insertValues.put(PKN_SPEED, statsList[0]);
                insertValues.put(PKN_SP_DEF, statsList[1]);
                insertValues.put(PKN_SP_ATK, statsList[2]);
                insertValues.put(PKN_DEF, statsList[3]);
                insertValues.put(PKN_ATK, statsList[4]);
                insertValues.put(PKN_HP, statsList[5]);

                SQLiteDatabase writableDB = this.getWritableDatabase();

                writableDB.insert(POKEMON_TABLE_NAME, null, insertValues);

                String[] selection = { TYPE_ID };
                String[] typesArray;

                if (typesList.size() > 1)
                {
                    typesArray = new String[2];
                }
                else
                {
                    typesArray = new String[1];
                }

                for (int i = 0; i < typesList.size(); i++) {
                    typesArray[i] = typesList.get(i);
                }

                Cursor c;

                if (typesList.size() > 1) {
                    c = readableDB.query(TYPE_TABLE_NAME,
                            selection,
                            "Name IN (?,?)",
                            typesArray,
                            null, null, null);
                }
                else {
                    c = readableDB.query(TYPE_TABLE_NAME,
                            selection,
                            "Name IN (?)",
                            typesArray,
                            null, null, null);
                }

                c.moveToFirst();

                for (int i = 0; i < c.getCount(); i++) {
                    insertValues = new ContentValues();

                    insertValues.put(PKN_ENTRY, entry);
                    insertValues.put(TYPE_ID, c.getInt(0));

                    writableDB.insert(POKEMON_TYPE_TABLE_NAME, null, insertValues);

                    c.moveToNext();
                }

                String[] colorArg = { TYPE_COLOR };
                String[] typeArg = new String[1];

                if (typesArray.length > 1) {
                    typeArg[0] = typesArray[1];
                }
                else {
                    typeArg[0] = typesArray[0];
                }

//                Cursor colorCursor = readableDB.query(TYPE_TABLE_NAME, colorArg, "Name LIKE ?", typeArg, null, null, null);
//
//                colorCursor.moveToFirst();
//
//                String color = colorCursor.getString(0);
//
//                pokemonList.add(new Pokemon(name, typesArray, entry, colorCursor.getString(0), statsList[5], statsList[4], statsList[3], statsList[2], statsList[1], statsList[0]));

                writableDB.close();
                readableDB.close();
            }
            else {
                pokemonNumber--;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertColorsIntoTypeTabe(SQLiteDatabase db)
    {
        ContentValues insertValues = new ContentValues();

        String[] singleType = new String[1];

        singleType[0] = "normal";
        insertEachColorIntoTable("#ffffec", insertValues, singleType, db);

        singleType[0] = "fighting";
        insertEachColorIntoTable("#ff8566", insertValues, singleType, db);

        singleType[0] = "water";
        insertEachColorIntoTable("#b3d1ff", insertValues, singleType, db);

        singleType[0] = "fire";
        insertEachColorIntoTable("#ffa366", insertValues, singleType, db);

        singleType[0] = "grass";
        insertEachColorIntoTable("#b3e6cc", insertValues, singleType, db);

        singleType[0] = "electric";
        insertEachColorIntoTable("#ffff4d", insertValues, singleType, db);

        singleType[0] = "ice";
        insertEachColorIntoTable("#ccffff", insertValues, singleType, db);

        singleType[0] = "bug";
        insertEachColorIntoTable("#ccffb3", insertValues, singleType, db);

        singleType[0] = "flying";
        insertEachColorIntoTable("#e6f7ff", insertValues, singleType, db);

        singleType[0] = "ghost";
        insertEachColorIntoTable("#dab3ff", insertValues, singleType, db);

        singleType[0] = "rock";
        insertEachColorIntoTable("#d9d9d9", insertValues, singleType, db);

        singleType[0] = "ground";
        insertEachColorIntoTable("#e6cbb3", insertValues, singleType, db);

        singleType[0] = "dragon";
        insertEachColorIntoTable("#8080ff", insertValues, singleType, db);

        singleType[0] = "fairy";
        insertEachColorIntoTable("#ffccff", insertValues, singleType, db);

        singleType[0] = "dark";
        insertEachColorIntoTable("#958884", insertValues, singleType, db);

        singleType[0] = "psychic";
        insertEachColorIntoTable("#ff66a3", insertValues, singleType, db);

        singleType[0] = "steel";
        insertEachColorIntoTable("#e6e6e6", insertValues, singleType, db);

        singleType[0] = "poison";
        insertEachColorIntoTable("#d966ff", insertValues, singleType, db);

    }

    private void insertEachColorIntoTable(String color, ContentValues insertValues, String[] singleType, SQLiteDatabase db) {
        insertValues = new ContentValues();
        insertValues.put(TYPE_COLOR, color);
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);
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
                LayoutInflater vi = (LayoutInflater)mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                ImageView image = (ImageView) v.findViewById(R.id.imageViewPokemon);
                if (entry != null) {
                    entry.setText(String.valueOf(p.getEntry()));
                }
                if (name != null) {
                    String capitalizedName = p.getName().substring(0,1).toUpperCase() + p.getName().substring(1);
                    if (p.getName().equals("mr-mime"))
                        capitalizedName = "Mr. Mime";
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
                if (image != null) {
                    String pknName = p.getName();

                    switch (pknName) {
                        case "nidoran-m":
                            pknName = "nidoranm";
                            break;
                        case "Nidoran-f":
                            pknName = "nidoranf";
                            break;
                        case "mr-mime":
                            pknName = "mrmime";
                            break;
                    }

                    Glide
                        .with(getContext())
                        .load("http://play.pokemonshowdown.com/sprites/xyani/" + pknName + ".gif")
                        .asGif()
                        .error(R.drawable.pokeball)
                        .into(image);
                }
            }
            return v;
        }
    }
}
