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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String PKN_GEN_ID = "GenerationId";
    private static final String PKN_TYPE_ID = "TypeId";

    //Type table
    private static final String TYPE_TABLE_NAME = "Type";
    private static final String TYPE_ID = "TypeId";
    private static final String TYPE_NAME = "Name";

    //Generation table
    private static final String GENERATION_TABLE_NAME = "Generation";
    private static final String GEN_NUMBER = "GenerationNumber";
    private static final String GEN_REGION = "Region";

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
            TYPE_NAME + " TEXT NOT NULL )";

    private static final String CREATE_GENERATION_TABLE = "CREATE TABLE " + GENERATION_TABLE_NAME + "(" +
            GEN_NUMBER + " INTEGER NOT NULL PRIMARY KEY, " +
            GEN_REGION + " TEXT ";

    private static final String CREATE_POKEMON_TYPE_TABLE = "CREATE TABLE " + POKEMON_TYPE_TABLE_NAME + "(" +
            PKN_ENTRY + " INTEGER NOT NULL, " +
            TYPE_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY(" + PKN_ENTRY + "," + TYPE_ID + "), " +
            "FOREIGN KEY(" + PKN_ENTRY + ") REFERENCES " + POKEMON_TABLE_NAME + "(" + PKN_ENTRY + "), " +
            "FOREIGN KEY(" + TYPE_ID + ") REFERENCES " + TYPE_TABLE_NAME + "(" + TYPE_ID + ")";

    /*
     * The following constants are only to be changed when Pokemon are added
     * to the Pokedex and/or more types are added
     */
    private static final int NUMBER_OF_POKEMON = 802;
    private static final int NUMBER_OF_TYPES = 18;
    private static final int NUMBER_OF_GENERATIONS = 7;
    private String url;

    /*
     * Drop table statements for all tables
     */
    private static final String DROP_POKEMON_TABLE = "DROP TABLE " + POKEMON_TABLE_NAME;
    private static final String DROP_TYPE_TABLE = "DROP TABLE " + TYPE_TABLE_NAME;
    private static final String DROP_GEN_TABLE = "DROP TABLE " + GENERATION_TABLE_NAME;
    private static final String DROP_POKEMON_TYPE_TABLE = "DROP TABLE " + POKEMON_TYPE_TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creates the following tables in a local database
        db.execSQL(CREATE_GENERATION_TABLE);
        db.execSQL(CREATE_TYPE_TABLE);
        db.execSQL(CREATE_POKEMON_TABLE);
        db.execSQL(CREATE_TYPE_TABLE);

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
        db.execSQL(DROP_GEN_TABLE);
        db.execSQL(DROP_TYPE_TABLE);
        onCreate(db);
    }

    public void insert() {
        OkHttpHandler handler = new OkHttpHandler();

        url = "https://pokeapi.co/api/v2/type/";
        handler.execute(url);

        handler = new OkHttpHandler();

        url = "https://pokeapi.co/api/v2/region/";
        handler.execute(url);

        for (int i = 1; i <= NUMBER_OF_POKEMON; i++) {
            url = "https://pokeapi.co/api/v2/pokemon/" + i;
            handler = new OkHttpHandler();
            handler.execute(url);
        }
    }

    public class OkHttpHandler extends AsyncTask {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(Object[] params) {
            Request.Builder builder = new Request.Builder();
            builder.url(params[0].toString());
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

            if (url.contains("https://pokeapi.co/api/v2/pokemon")) {
                parsePokemonResponse(o.toString());
            }

            if (url.contains("https://pokeapi.co/api/v2/type")) {
                parseTypeResponse(o.toString());
            }

            if (url.contains("https://pokeapi.co/api/v2/region")) {
                parseRegionResponse(o.toString());
            }
        }
    }

    private void parsePokemonResponse(String response) {
        try{
            JSONObject json = new JSONObject(response);

            String name = json.getJSONArray("forms").getJSONObject(0).getString("name");

            int entry = json.getInt("id");

            JSONArray stats = json.getJSONArray("stats");

            JSONArray types = json.getJSONArray("types");

            String[] typesList;

            if (types.length() > 1) {
                typesList = new String[2];
                for (int i = 0; i < types.length() ; i++) {
                    typesList[i] = types.getJSONObject(i).getJSONObject("type").getString("name");
                }
            }
            else {
                typesList = new String[1];
                typesList[0] = types.getJSONObject(0).getJSONObject("type").getString("name");
            }

            int[] statsList = new int[6];

            SQLiteDatabase writableDB = this.getWritableDatabase();

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

            writableDB.insert(POKEMON_TABLE_NAME, null, insertValues);

            SQLiteDatabase readableDB = this.getReadableDatabase();

            String[] selection = { TYPE_ID };

            Cursor c = readableDB.query(TYPE_TABLE_NAME,
                                        selection,
                                        TYPE_NAME,
                                        typesList,
                                        null,
                                        null,
                                        TYPE_ID);

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

    private void parseTypeResponse(String response) {
        try{
            JSONObject json = new JSONObject(response);

            String[] typeList = new String[NUMBER_OF_TYPES];

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues insertValues = new ContentValues();

            for (int i = 0; i < typeList.length; i++) {
                typeList[i] = json.getJSONArray("results").getJSONObject(i).getString("name");
                insertValues.put(TYPE_NAME, typeList[i]);
                db.insert(TYPE_TABLE_NAME, null, insertValues);
            }

            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseRegionResponse(String response) {
        try{
            JSONObject json = new JSONObject(response);

            String[] regionList = new String[NUMBER_OF_GENERATIONS];

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues insertValues = new ContentValues();

            for (int i = 0; i < regionList.length; i++) {
                regionList[i] = json.getJSONArray("results").getJSONObject(i).getString("name");
                insertValues.put(GEN_NUMBER, i+1);
                insertValues.put(GEN_REGION, regionList[i]);
                db.insert(GENERATION_TABLE_NAME, null, insertValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
