package ca.bgoodfellow.pokedex;
/**
 * Created by goodf on 2018-03-19.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private static final int NUMBER_OF_POKEMON = 250;
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

    //Generation table
//    private static final String GENERATION_TABLE_NAME = "Generation";
//    private static final String GEN_NUMBER = "GenerationNumber";
//    private static final String GEN_REGION = "Region";

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

//    private static final String CREATE_GENERATION_TABLE = "CREATE TABLE " + GENERATION_TABLE_NAME + "(" +
//            GEN_NUMBER + " INTEGER NOT NULL PRIMARY KEY, " +
//            GEN_REGION + " TEXT ";

    private static final String CREATE_POKEMON_TYPE_TABLE = "CREATE TABLE " + POKEMON_TYPE_TABLE_NAME + "( " +
            PKN_ENTRY + " INTEGER NOT NULL, " +
            TYPE_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY(" + PKN_ENTRY + "," + TYPE_ID + "), " +
            "FOREIGN KEY(" + PKN_ENTRY + ") REFERENCES " + POKEMON_TABLE_NAME + "(" + PKN_ENTRY + "), " +
            "FOREIGN KEY(" + TYPE_ID + ") REFERENCES " + TYPE_TABLE_NAME + "(" + TYPE_ID + "))";

    private PokemonHttpHandler pokemonHandler;
    private PokemonHttpHandler newPokemonHandler;
    private int pokemonNumber = 152;
    private String typeUrl = "https://pokeapi.co/api/v2/type/";
    private String pokemonURL = "https://pokeapi.co/api/v2/pokemon/";

    /*
     * Drop table statements for all tables
     */
    private static final String DROP_POKEMON_TABLE = "DROP TABLE " + POKEMON_TABLE_NAME;
    private static final String DROP_TYPE_TABLE = "DROP TABLE " + TYPE_TABLE_NAME;
    //private static final String DROP_GEN_TABLE = "DROP TABLE " + GENERATION_TABLE_NAME;
    private static final String DROP_POKEMON_TYPE_TABLE = "DROP TABLE " + POKEMON_TYPE_TABLE_NAME;

    private ProgressBar progressBar;
    private Button loadData;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creates the following tables in a local database
        db.execSQL(CREATE_TYPE_TABLE);
        db.execSQL(CREATE_POKEMON_TABLE);
        db.execSQL(CREATE_POKEMON_TYPE_TABLE);

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
        //db.execSQL(DROP_GEN_TABLE);
        db.execSQL(DROP_TYPE_TABLE);
        onCreate(db);
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

            pokemon.add(new Pokemon(name, types, entry, color, hp, atk, def, spAtk, spDef, speed));

            pokemonCursor.moveToNext();
        }

        db.close();
        return pokemon;
    }

    public void insert() {

        TypeHttpHandler typeHandler = new TypeHttpHandler();

        typeHandler.execute(typeUrl);

        //PokemonHttpHandler pokemonHandler;

        pokemonHandler = new PokemonHttpHandler();
        pokemonHandler.execute(pokemonURL);


    }

    public void additionalInsert(int index) {
        //Sets pokemonNumber to the next Pokemon after the last one that was inserted
        pokemonNumber = index + 1;

        pokemonURL = "https://pokeapi.co/api/v2/pokemon/";

        newPokemonHandler = new PokemonHttpHandler();
        newPokemonHandler.execute(pokemonURL);
    }
    class PokemonHttpHandler extends AsyncTask {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(Object[] params) {
            Request.Builder builder = new Request.Builder();
            builder.url(pokemonURL + pokemonNumber);
            //builder.url(params[0].toString());
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

            if (o != null && pokemonNumber <= NUMBER_OF_POKEMON)
            {
                parsePokemonResponse(o.toString());

                pokemonNumber++;

                pokemonHandler = new PokemonHttpHandler();
                pokemonHandler.execute(pokemonURL);
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

            JSONArray stats = json.getJSONArray("stats");

            JSONArray types = json.getJSONArray("types");

            String name = json.getJSONArray("forms").getJSONObject(0).getString("name");

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

            SQLiteDatabase readableDB = this.getReadableDatabase();

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

            writableDB.close();
            readableDB.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertColorsIntoTypeTabe(SQLiteDatabase db)
    {
        ContentValues insertValues = new ContentValues();

        String[] singleType = new String[1];

        singleType[0] = "normal";
        insertValues.put(TYPE_COLOR, "#ffffec");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "fighting";
        insertValues.put(TYPE_COLOR, "#ff8566");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "water";
        insertValues.put(TYPE_COLOR, "#b3d1ff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "fire";
        insertValues.put(TYPE_COLOR, "#ffa366");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "grass";
        insertValues.put(TYPE_COLOR, "#b3e6cc");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "electric";
        insertValues.put(TYPE_COLOR, "#ffff4d");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "ice";
        insertValues.put(TYPE_COLOR, "#ccffff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "bug";
        insertValues.put(TYPE_COLOR, "#ccffb3");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "flying";
        insertValues.put(TYPE_COLOR, "#e6f7ff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "ghost";
        insertValues.put(TYPE_COLOR, "#dab3ff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "rock";
        insertValues.put(TYPE_COLOR, "#d9d9d9");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "ground";
        insertValues.put(TYPE_COLOR, "#e6cbb3");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "dragon";
        insertValues.put(TYPE_COLOR, "#8080ff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "fairy";
        insertValues.put(TYPE_COLOR, "#ffccff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "dark";
        insertValues.put(TYPE_COLOR, "#958884");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "psychic";
        insertValues.put(TYPE_COLOR, "#ff66a3");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "steel";
        insertValues.put(TYPE_COLOR, "#e6e6e6");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);

        insertValues = new ContentValues();
        singleType[0] = "poison";
        insertValues.put(TYPE_COLOR, "#d966ff");
        db.update(TYPE_TABLE_NAME, insertValues, "Name = ?", singleType);
    }

}
