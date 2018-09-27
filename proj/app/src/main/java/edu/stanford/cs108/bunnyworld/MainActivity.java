package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SingletonData singleton;
    ArrayList<String> allTables;
    private static int PAGE_WIDTH = 768;
    private static int PAGE_HEIGHT = 1022;
    private static int BOX_SIZE = 125;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        singleton = SingletonData.getInstance();
        dbInit();
        init();
    }

    private void init() {
        allTables = getAllTable();
        Spinner spinner = (Spinner) findViewById(R.id.main_games_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,allTables);
        spinner.setAdapter(adapter);

    }


    private void dbInit() {

        if (singleton.getDB() == null) {

            singleton.setDB(openOrCreateDatabase("bunnyWorldDB", MODE_PRIVATE, null));

            Cursor tablesCursor = singleton.getDB().rawQuery(
                    "SELECT * FROM sqlite_master WHERE type='table' AND name='bunnyworld';",
                    null);


            // if (tablesCursor.getCount() == 0) {
            //      resetDatabase();
            // }

            // TODO: decide whether or not to remove this
            // currently forces initialization and population of new db
            resetDatabase();
        }
    }

    protected static void setupDatabase(SQLiteDatabase db, String gameName){


        String setupStr = "CREATE TABLE " + gameName + " ("
                + "page_name TEXT, image_name TEXT, text TEXT,"
                + "visible BIT, movable BIT, shape_script TEXT,"
                + "x FLOAT, y FLOAT, width INT, height INT,"
                + " shape_name TEXT PRIMARY KEY"
                + ");";

        db.execSQL(setupStr);
    //    singleton.getDB().execSQL(setupStr);

    }

    private void populateDatabaseBunnyTest() {

        String mazeStr1 = "'You are in a maze of twisty '";
        String mazeStr2 = "'little passages – all alike!'";
        String box1X = String.valueOf(.25 * PAGE_WIDTH - .5 * BOX_SIZE);
        String box2X = String.valueOf(.5 * PAGE_WIDTH - .5 * BOX_SIZE);
        String box3X = String.valueOf(.75 * PAGE_WIDTH - .5 * BOX_SIZE);

        String page1Str = "INSERT INTO bunnyworld VALUES"
                + "('page1', NULL, 'Bunny World!', "
                + "1, 0, NULL, 15, 60, 0, 0, 'bunnyWorldText'), "
                + "('page1', NULL, " + mazeStr1 + ", "
                + "1, 0, NULL, 40, 325, 10, 10, 'mazeText1'), "
                + "('page1', NULL, " + mazeStr2 + ", "
                + "1, 0, NULL, 40, 385, 10, 10, 'mazeText2'), "
                + "('page1', NULL, NULL, "
                + "1, 0, 'on click goto page2', "
                + box1X + ", 500, " + BOX_SIZE + ", " + BOX_SIZE + ", 'box1'),"
                + "('page1', NULL, NULL, "
                + "0, 0, 'on click goto page3', " // TODO: change first int back to 0 (invisible)
                + box2X + ", 500, " + BOX_SIZE + ", " + BOX_SIZE + ", 'box2'),"
                + "('page1', NULL, NULL, "
                + "1, 0, 'on click goto page4', "
                + box3X + ", 500, " + BOX_SIZE + ", " + BOX_SIZE + ", 'box3'),"
                + "('page1', NULL, NULL, "
                + "1, 0, NULL, 15, 60, 0, 00, 'new_Game_Text');";

        singleton.getDB().execSQL(page1Str);

        String mysticScript = "'on click hide carrot play munching;"
                + "on enter show box2;'";
        String bunnyText1 = "'Mystic Bunny – Rub my tummy'";
        String bunnyText2 = "'for a big surprise!'";
        String doorScript = "'on click goto page1;'";

        String page2Str = "INSERT INTO bunnyworld VALUES"
                + "('page2', 'mystic', NULL,"
                + "1, 0, " + mysticScript + ","
                + "280, 200, 300, 300,"
                + "'mysticBunny'),"
                + "('page2', NULL, " + bunnyText1 + ","
                + "1, 0, NULL, 40, 650, 0, 0,"
                + "'mysticBunnyText1'),"
                + "('page2', NULL, " + bunnyText2 + ","
                + "1, 0, NULL, 200, 710, 0, 0,"
                + "'mysticBunnyText2'),"
                + "('page2', NULL, NULL,"
                + "1, 0, " + doorScript + ","
                + "30, 310, " + BOX_SIZE + ", " + BOX_SIZE + ", 'box5'),"
                +  "('page2', NULL, NULL, "
                + "1, 0, NULL, 15, 60, 0, 00, 'new_Page_Text2');";

        singleton.getDB().execSQL(page2Str);

        String eekText1 = "'Eek! Fire-Room.'";
        String eekText2 = "'Run away!'";
        String page3Str = "INSERT INTO bunnyworld VALUES"
                + "('page3', 'fire', NULL,"
                + "1, 0, 'on enter play fire;',"
                + "85, 85, 600, 300, 'fire'),"
                + "('page3', NULL, " + eekText1 + ", "
                + "1, 0, NULL, 95, 545, 0, 0, 'eekText1'),"
                + "('page3', NULL, " + eekText2 + ", "
                + "1, 0, NULL, 110, 605, 0, 0, 'eekText2'),"
                + "('page3', NULL, NULL,"
                + "1, 0, 'on click goto page2',"
                + "410, 605, " + BOX_SIZE + ", " + BOX_SIZE + ", 'box4'),"
                + "('page3', 'carrot', NULL,"
                + "1, 1, NULL,"
                + "540, 440, 150, 150, 'carrot'),"
                + "('page3', NULL, NULL, "
                + "1, 0, NULL, 15, 60, 0, 00, 'new_Page_Text3');";
        singleton.getDB().execSQL(page3Str);

        String deathScript = "'on enter play evillaugh;"
                + "on drop carrot hide carrot play munching hide deathBunny show box6;"
                + "on click play evillaugh'";

        String deathText1 = "'You must appease the'";
        String deathText2 = "'Bunny of Death!'";
        String page4Str = "INSERT INTO bunnyworld VALUES"
                + "('page4', 'death', NULL,"
                + "1, 0, " + deathScript + ","
                + "200, 100, 400, 400, 'deathBunny'),"
                + "('page4', NULL, " + deathText1 + ", "
                + "1, 0, NULL, 120, 580, 0, 0, 'deathText1'),"
                + "('page4', NULL, " + deathText2 + ", "
                + "1, 0, NULL, 180, 640, 0, 0, 'deathText2'),"
                + "('page4', NULL, NULL,"
                + "0, 0, 'on click goto page5',"
                + "500, 350, 100, 100, 'box6'),"
                + "('page4', NULL, NULL, "
                + "1, 0, NULL, 15, 60, 0, 00, 'new_Page_Text4');";
        singleton.getDB().execSQL(page4Str);

        String page5Str = "INSERT INTO bunnyworld VALUES"
                + "('page5', 'carrot', NULL,"
                + "1, 0, NULL, 50, 50, 75, 75, 'carrot2'),"
                + "('page5', 'carrot', NULL,"
                + "1, 0, NULL, 150, 150, 75, 75, 'carrot3'),"
                + "('page5', 'carrot', NULL,"
                + "1, 0, NULL, 250, 50, 75, 75, 'carrot4'),"
                + "('page5', NULL, 'You Win! Yay!',"
                + "1, 0, NULL, 50, 400, 0, 0, 'victoryText'),"
                + "('page5', NULL, NULL, "
                + "1, 0, NULL, 15, 60, 0, 00, 'new_Page_Text5');";
        singleton.getDB().execSQL(page5Str);

    }


    protected void resetDatabase() {

        String resetStr = "DROP TABLE IF EXISTS bunnyworld;";
        singleton.getDB().execSQL(resetStr);

        setupDatabase(singleton.getDB(), "bunnyworld");
        populateDatabaseBunnyTest();
    }

    public void createNewGame(View view) {
        EditText newGame = (EditText)findViewById(R.id.game_name);
        String gameEntered = newGame.getText().toString();
        if (gameEntered.toString().equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),"Please Enter a Game Name!", Toast.LENGTH_SHORT);
            toast.show();
        } else if(allTables.contains(gameEntered)) {
            Toast toast = Toast.makeText(getApplicationContext(),gameEntered + " already exists", Toast.LENGTH_SHORT);
            toast.show();
        } else {

            String setupStr = "CREATE TABLE " +  gameEntered + " ("
                    + "page_name TEXT, image_name TEXT, text TEXT,"
                    + "visible BIT, movable BIT, shape_script TEXT,"
                    + "x FLOAT, y FLOAT, width INT, height INT,"
                    + " shape_name TEXT PRIMARY KEY"
                    + ");";
            singleton.getDB().execSQL(setupStr);

            String page1Str = "INSERT INTO "+ gameEntered + " VALUES"
                    + "('page1', NULL, NULL, "
                    + "1, 0, NULL, 15, 60, 0, 00, 'new_Game_Text')";

            singleton.getDB().execSQL(page1Str);

            Toast toast = Toast.makeText(getApplicationContext(),gameEntered + " has been created!", Toast.LENGTH_SHORT);
            toast.show();
            init();
            newGame.setText("");
        }
    }


    public void playGame(View view) {
        Spinner sp = (Spinner)findViewById(R.id.main_games_spinner);
        String game = sp.getSelectedItem().toString();

            singleton.setCurrentGame(game);
            Intent intent = new Intent(this, PageActivity.class);
            startActivity(intent);

    }

    public void editGame(View view) {
        Spinner sp = (Spinner)findViewById(R.id.main_games_spinner);
        String game = sp.getSelectedItem().toString();
            singleton.setCurrentGame(game);
            Intent intent = new Intent(this, EditPageActivity.class);
            startActivity(intent);

    }

        /*
    code is gotten from stackOverflow at the below URL:
    https://stackoverflow.com/questions/27884310/get-all-table-name-list-into-array-from-sqlite-database-android
     */

    public ArrayList<String> getAllTable() {

        ArrayList<String> arrTblNames = new ArrayList<String>();
        Cursor c = singleton.getDB().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            arrTblNames.add(c.getString(c.getColumnIndex("name")));
            c.moveToNext();
        }
        // make sure to close the cursor
        c.close();
        arrTblNames.remove("android_metadata");
        arrTblNames.remove("sqlite_sequence");
        return arrTblNames;
    }

    public void deleteGame(View view) {
        Spinner sp = findViewById(R.id.main_games_spinner);
        String game = sp.getSelectedItem().toString();

        if(game.equals("bunnyworld")) {
            Toast toast = Toast.makeText(getApplicationContext(),"Cannot delete bunnyworld!", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            String deleteStr = "DROP TABLE IF EXISTS " + game + ";";
            singleton.getDB().execSQL(deleteStr);
            allTables.remove(game);
            init();
            Toast toast = Toast.makeText(getApplicationContext(), game + " deleted", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
