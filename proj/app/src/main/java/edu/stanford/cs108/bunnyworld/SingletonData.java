package edu.stanford.cs108.bunnyworld;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

/**

 * Created by devin on 3/12/2018.
 */

public class SingletonData {

    private SQLiteDatabase DB;
    private String currentGame;
    private String currentPage;
    private ArrayList<String> currentGamePages;
    private ArrayList<String> currentGameShapes;

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    private static final SingletonData ourInstance = new SingletonData();

    static SingletonData getInstance() {
        return ourInstance;
    }


    private SingletonData() {

        DB = null; // NOTE: should probably initialize db here
        currentGame = null;
        currentPage = null;
        currentGamePages = new ArrayList<String>();
        currentGameShapes = new ArrayList<String>();
    }


    public void setDB(SQLiteDatabase db){
        DB = db;
    }

    public SQLiteDatabase getDB(){
        return DB;
    }

    public void setCurrentGamePages() {

        Cursor cursorPg = DB.rawQuery(
                "SELECT * FROM "+ getCurrentGame(), null);
        ArrayList<String> pageNames = new ArrayList<String>();

        // Add every page to the page names array -- Unsure whether we will ever use this
        while (cursorPg.moveToNext()) {
            String output = "";
            output += cursorPg.getString(0);
            if (!currentGamePages.contains(output))
                currentGamePages.add(output);
        }
    }



    public ArrayList<String> getCurrentGamePages() {
        return currentGamePages;
    }

    public void setCurrentGameShapes() {

        Cursor cursorPg = DB.rawQuery(
                "SELECT * FROM "+ getCurrentGame(), null);

        // Add every page to the page names array -- Unsure whether we will ever use this
        while (cursorPg.moveToNext()) {
            String output = "";
            output += cursorPg.getString(10);
            if (!currentGameShapes.contains(output))
                currentGameShapes.add(output);
        }
    }

    public ArrayList<String> getCurrentGameShapes() {
        return currentGameShapes;
    }




    }






