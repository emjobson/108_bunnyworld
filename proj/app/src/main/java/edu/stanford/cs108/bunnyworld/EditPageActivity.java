package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EditPageActivity extends AppCompatActivity {

    private ArrayList<String> pages;
    SingletonData singleton;
    String previousDeletedPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);
        singleton = SingletonData.getInstance();
        TextView currentGame = findViewById(R.id.currentGame);
        currentGame.setText("Current Game: " + singleton.getCurrentGame());
        pages = new ArrayList<String>();
        init();
    }


    private void init() {
        setPages();
        Spinner spinner = (Spinner) findViewById(R.id.main_pages_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,pages);
        spinner.setAdapter(adapter);

    }

    private void setPages() {

        if (singleton.getDB() == null) System.out.println("singleton database null");
        Cursor cursorPg = singleton.getDB().rawQuery(
                "SELECT * FROM "+ singleton.getCurrentGame(), null);
        ArrayList<String> pageNames = new ArrayList<String>();

        // Add every page to the page names array -- Unsure whether we will ever use this
        while (cursorPg.moveToNext()) {
            String output = "";
            output += cursorPg.getString(0);
            if (!pages.contains(output))
                pages.add(output);
        }
    }


    public void deletePage(View view) {
        Spinner sp = findViewById(R.id.main_pages_spinner);
        String page = sp.getSelectedItem().toString();
        if (page.equals("page1")) {
            Toast toast = Toast.makeText(getApplicationContext(), "Cannot delete page1! This is the Starter Page", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            previousDeletedPage = "SELECT* FROM " + singleton.getCurrentGame() + " WHERE page_name = '" + page + "';";
            String deleteStr = "DELETE FROM " + singleton.getCurrentGame() + " WHERE page_name = '" + page + "';";
            singleton.getDB().execSQL(deleteStr);
            pages.remove(page);
            init();
            Toast toast = Toast.makeText(getApplicationContext(), page + " deleted", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void createNewPage(View view) {

        EditText newPage = (EditText)findViewById(R.id.page_name);
        String pageEntered = newPage.getText().toString();
       if(pages.contains(pageEntered)) {
            Toast toast = Toast.makeText(getApplicationContext(),pageEntered + " already exists", Toast.LENGTH_SHORT);
            toast.show();
        } else {

           if (pageEntered.toString().equals("")) {
               pageEntered = "page" + (pages.size() + 1);

           }
           String newPageEmptyShapeName = "new_Page_Text" + (pages.size() + 1);

           String newPageStr = "INSERT INTO "+ singleton.getCurrentGame() + " VALUES"
                    + "('" + pageEntered  + "',NULL, NULL, "
                    + "1, 0, NULL, 15, 60, 0, 00,'" + newPageEmptyShapeName + "') ";

           singleton.getDB().execSQL(newPageStr);
            Toast toast = Toast.makeText(getApplicationContext(),pageEntered + " has been created!", Toast.LENGTH_SHORT);
            toast.show();
            init();
           newPage.setText("");
        }
    }

    public void renamePage(View view) {
        EditText newPage = (EditText) findViewById(R.id.page_name);
        String pageEntered = newPage.getText().toString();
        Spinner sp = findViewById(R.id.main_pages_spinner);
        String selectedPage = sp.getSelectedItem().toString();

        if (selectedPage.equals("page1")) {
            Toast toast = Toast.makeText(getApplicationContext(), "Cannot rename Page1! This is the Starter Page", Toast.LENGTH_SHORT);
            toast.show();
        } else {

            if (pageEntered.toString().equals("")) {
                Toast toast = Toast.makeText(getApplicationContext(), "Please Enter a Page Name!", Toast.LENGTH_SHORT);
                toast.show();
            } else if (pages.contains(pageEntered)) {
                Toast toast = Toast.makeText(getApplicationContext(), pageEntered + " already exists", Toast.LENGTH_SHORT);
                toast.show();
            } else {

                String newPageName = "UPDATE " + singleton.getCurrentGame() + " SET page_name = '" + pageEntered + "' WHERE page_name = '" + selectedPage + "'";
                singleton.getDB().execSQL(newPageName);
                Toast toast = Toast.makeText(getApplicationContext(), selectedPage + " is now named " + pageEntered, Toast.LENGTH_SHORT);
                toast.show();
                pages.remove(selectedPage);
                init();
            }
        }
        newPage.setText("");
    }

    public void editPage(View view) {
        Spinner sp = (Spinner)findViewById(R.id.main_pages_spinner);
        String page = sp.getSelectedItem().toString();
        singleton.setCurrentPage(page);
        System.out.println(page);
        Intent intent = new Intent(this,EditShapeActivity.class);
        startActivity(intent);
    }

    public void returnHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}



