/**
 * Created by goodf on 2018-03-19.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.IOException;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database name and version number
    //Change these variable values if the database name or version number change
    private static final String DATABASE_NAME = "Pokedex";
    private static final int DATABASE_VERSION = 1;

    /*
     * SQL statements for creation of the following tables and table columns:
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
    private static final String TYPE_NAME = "Name";

    //Generation table
    private static final String GENERATION_TABLE_NAME = "Generation";
    private static final String GEN_NUMBER = "GenerationNumber";
    private static final String GEN_REGION = "Region";

    /*
     * Create Statements for all tables in this database
     */
    private static final String CREATE_POKEMON_TABLE = "CREATE TABLE " + POKEMON_TABLE_NAME + "(" +
            PKN_ENTRY + " Integer NOT NULL, " +
            PKN_NAME + " TEXT NOT NULL, " +
            PKN_HP + " Integer, " +
            PKN_ATK + " Integer, " +
            PKN_DEF + " Integer, " +
            PKN_SP_ATK + " Integer, " +
            PKN_SP_DEF + " Integer, " +
            PKN_SPEED + " Integer ) ";

    private static final String CREATE_TYPE_TABLE = "CREATE TABLE " + TYPE_TABLE_NAME + "(" +
            TYPE_NAME + " TEXT NOT NULL )";

    private static final String createGenerationTable = "CREATE TABLE " + GENERATION_TABLE_NAME + "(" +
            GEN_NUMBER + " Integer NOT NULL, " +
            GEN_REGION + " TEXT ";

    /*
        Drop table statements for all tables
     */
    private static final String DROP_POKEMON_TABLE = "DROP TABLE " + POKEMON_TABLE_NAME;
    private static final String DROP_TYPE_TABLE = "DROP TABLE " + TYPE_TABLE_NAME;
    private static final String DROP_GEN_TABLE = "DROP TABLE " + GENERATION_TABLE_NAME;

    //API url
    private static final String URL = "https://pokeapi.co/api/v2/pokemon/?limit=802";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // execute the create table code

        db.execSQL(CREATE_POKEMON_TABLE);

        String createTypeTable = "CREATE TABLE " + TYPE_TABLE_NAME + "(" +
                TYPE_NAME + " TEXT NOT NULL )";

        db.execSQL(createTypeTable);

        String createGenerationTable = "CREATE TABLE " + GENERATION_TABLE_NAME + "(" +
                GEN_NUMBER + " Integer NOT NULL, " +
                GEN_REGION + " TEXT ";

        db.execSQL(createGenerationTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //drop the table and recreate it
        db.execSQL(DROP_POKEMON_TABLE);
        db.execSQL(DROP_GEN_TABLE);
        db.execSQL(DROP_TYPE_TABLE);
        onCreate(db);
    }

    public void insert() {
        SQLiteDatabase db = this.getWritableDatabase();

        //TODO: Connect to an online API and insert data into database

        String insertTypes = "INSERT INTO " + POKEMON_TABLE_NAME +
                " VALUES (";

        String insertGens = "INSERT INTO " + POKEMON_TABLE_NAME +
                " VALUES (";

        String insertPokemon = "INSERT INTO " + POKEMON_TABLE_NAME +
                " VALUES (";
    }

}
