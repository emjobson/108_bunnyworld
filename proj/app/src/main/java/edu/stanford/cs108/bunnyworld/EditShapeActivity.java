package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditShapeActivity extends AppCompatActivity {

    private ArrayList<String> shapes;
    private ArrayList<String> triggers;
    private ArrayList<String> actions;
    private ArrayList<String> imageNames;
    private ArrayList<String> soundNames;
    private ArrayList<String> contDropChoices;
    private String shapeScript;
    private boolean scriptFinished;
    private boolean onDrop;
    private Map<String, BitmapDrawable> drawableMap;
    private Map<String, Integer> soundRes;

    SingletonData singleton;

    public ArrayList<String> getShapesArr() {
        return shapes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shape);
        singleton = SingletonData.getInstance();
        TextView currentPage = findViewById(R.id.currentPage);
        singleton.setCurrentGamePages();
        singleton.setCurrentGameShapes();
        currentPage.setText("Current Page: " + singleton.getCurrentPage());
        shapes = new ArrayList<String>();
        imageNames = new ArrayList<String>();
        soundNames = new ArrayList<String>();
        contDropChoices = new ArrayList<String>();
        shapeScript = "";
        scriptFinished = false;
        onDrop = false;
        loadResources();
        setResourcesArrays();
        setTriggers();
        setActions();
        initShapesArray();
        setShapeSpinner();
        setScriptSpinner();
        setImageSpinner();

        final Spinner shapeSpinner = (Spinner) findViewById(R.id.main_shapes_spinner);
        shapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
                editPV.setSelectedShape(shapeSpinner.getSelectedItem().toString());
                editPV.displaySelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public ArrayList<String> getImageNames() {
        return imageNames;
    }

    public String getShapeScript() {
        return shapeScript;
    }

    public boolean isScriptFinished() {
        return scriptFinished;
    }

    private void setResourcesArrays() {
        imageNames.add("No Image");
        for (String imageName : drawableMap.keySet()) {
            imageNames.add(imageName);
        }

        for (String soundName : soundRes.keySet()) {
            soundNames.add(soundName);
        }
        contDropChoices.add("continue onDrop script"); // TODO: ADD MORE
    }

    private void setImageSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.images_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, imageNames);
        spinner.setAdapter(adapter);
    }

    private void loadResources() {
        EditPageView editP = (EditPageView) findViewById(R.id.EditPageView);
        drawableMap = editP.getDrawableMap();
        soundRes = editP.getSoundRes();
    }

    private void setActions() {
        actions = new ArrayList<String>();
        actions.add("goto ");
        actions.add("play ");
        actions.add("hide ");
        actions.add("show ");
    }

    private void setTriggers() {
        triggers = new ArrayList<String>();
        triggers.add("on click ");
        triggers.add("on enter ");
        triggers.add("on drop ");
    }

    private void initShapesArray() {

        Cursor cursorPg = singleton.getDB().rawQuery(
                "SELECT * FROM "+ singleton.getCurrentGame() +" WHERE page_name = '" + singleton.getCurrentPage() + "'", null);

        // Add every page to the page names array -- Unsure whether we will ever use this
        while (cursorPg.moveToNext()) {
            String output = "";
            output += cursorPg.getString(10);
            if (!shapes.contains(output) && !output.contains("new_Page_Text") && !output.contains("new_Game_Text")){
                shapes.add(output);
            }
        }
    }

    // sets the shape spinner to be whatever's in the shapes array
    private void setShapeSpinner() {
        EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
        BShape storage = null;
        if (editPV.getSelectedShape() != null){
            storage = editPV.getSelectedShape();
        }
        Spinner spinner = (Spinner) findViewById(R.id.main_shapes_spinner);

        SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shapes);

        spinner.setAdapter(adapter);

        if (storage != null) editPV.setSelectedShape(storage.getName());


    }

    private void setScriptSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.script_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, triggers);
        spinner.setAdapter(adapter);

    }

    public String getShapeSpinnerText() {
        Spinner spinner = (Spinner) findViewById(R.id.script_spinner);
        String selectedChoice = spinner.getSelectedItem().toString();
        return selectedChoice;
    }

    // PROBLEM: might be editing (in which case we need to change selectedShape's script
    // or, might be preparing to add a new shape)
    public void onScriptSpinnerClick(View view) {

        Spinner spinner = (Spinner) findViewById(R.id.script_spinner);
        String selectedChoice = spinner.getSelectedItem().toString();

        TextView scriptText = findViewById(R.id.script_text);


        if (scriptFinished) {
            onDrop = false;
            scriptFinished = false;
        }

        if (selectedChoice.equals("on click ") || selectedChoice.equals("on enter ")) {

            continueScript(selectedChoice, spinner, scriptText, actions, "actions");

        } else if (selectedChoice.equals("on drop ")) {

            onDrop = true;
            continueScript(selectedChoice, spinner, scriptText, singleton.getCurrentGameShapes(), "droppable shape");

        } else if (selectedChoice.equals("goto ")) {

            continueScript(selectedChoice, spinner, scriptText, singleton.getCurrentGamePages(), "goto pages");

        } else if (selectedChoice.equals("play ")) {

            continueScript(selectedChoice, spinner, scriptText, soundNames, "sounds to play");

        } else if (selectedChoice.equals("hide ") || selectedChoice.equals("show ")) {

            continueScript(selectedChoice, spinner, scriptText, singleton.getCurrentGameShapes(), "hide/show shapes");

            // potential end of on-drop ... how to change to make it
        } else if (onDrop && singleton.getCurrentGameShapes().contains(selectedChoice)) {

            continueScript(selectedChoice, spinner, scriptText, actions, "actions");
            shapeScript += " "; // Space because the added shape2 doesn't have a space after it -> fine to leave this always if this is okay: "on drop carrot hide carrot show door ;" (space at end)
            onDrop = false; // Set ondrop to false here to stop endless loop

        } else if (singleton.getCurrentGameShapes().contains(selectedChoice)) {
            terminateScript(scriptText, spinner, selectedChoice);

        } else if (singleton.getCurrentGamePages().contains(selectedChoice)) {
            terminateScript(scriptText, spinner, selectedChoice);

        } else if (soundNames.contains(selectedChoice)) {
            terminateScript(scriptText, spinner, selectedChoice);
        }
    }

    /*
     * Private helper method called when proceed is clicked and there remains another drop-down
     * in the current script setup.
     *
     * Appends to the shapeScript, sets the SpinnerAdapter to its new list, and displays the new
     * text labelling the spinner.
     * Elliott
     */
    private void continueScript(String selectedChoice, Spinner spinner, TextView scriptText, ArrayList<String> spinnerItems, String textDisplay) {

        shapeScript += selectedChoice;
        SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItems);
        spinner.setAdapter(adapter);
        scriptText.setText(textDisplay);
    }

    /*
     * Private helper method called when spinners finish creating a script entry.
     * Terminates the shapescript with a ";", resets the appropriate Views, and displays a toast.
     * Elliott
     */
    private void terminateScript(TextView scriptText, Spinner spinner, String selectedChoice) {
        shapeScript += selectedChoice + ";";
        SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, triggers);
        spinner.setAdapter(adapter);
        scriptText.setText("Create Script");
        scriptFinished = true;
        Toast toast = Toast.makeText(getApplicationContext(), "script created", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void savePage(View view) {
        EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
        editPV.updateDB();
        Intent intent = new Intent(this, EditPageActivity.class);
        startActivity(intent);
        shapeScript = "";
    }

    public void deleteShape(View view) {
        Spinner sp = findViewById(R.id.main_shapes_spinner);

        if (sp.getSelectedItem() == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "No shape to delete!", Toast.LENGTH_SHORT);
            toast.show();

        } else {
            String selectedShape = sp.getSelectedItem().toString();

            if (shapes.isEmpty()) {
                Toast toast2 = Toast.makeText(getApplicationContext(), "There is no shape to delete", Toast.LENGTH_SHORT);
                toast2.show();

            } else {
                //enter FUNCTIONALTY
                // TODO: actually remove from necessary data structures (allShapes and shapesOnPage (from editpageview), singleton's currentGameShapes,

                // update EditPageView
                EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
                editPV.removeShape(selectedShape);

                // update spinner/shapes
                shapes.remove(selectedShape);
                setShapeSpinner();

                // update
                editPV.updateDB();
                singleton.setCurrentGamePages(); // NEW
                singleton.setCurrentGameShapes();

                Toast toast = Toast.makeText(getApplicationContext(), selectedShape + " deleted", Toast.LENGTH_SHORT);
                toast.show();

                //    zeroFields();
            }
          }
        }


    public void renameShape(View view) {

        EditText newShape = (EditText) findViewById(R.id.shape_name);
        String newName = newShape.getText().toString();
        Spinner sp = findViewById(R.id.main_shapes_spinner);
        String selectedShapeName = sp.getSelectedItem().toString();



        if (newName.equals("")) { // blank name entry
            Toast toast = Toast.makeText(getApplicationContext(), "Please Enter a Shape Name!", Toast.LENGTH_SHORT);
            toast.show();

        } else if (shapes.contains(newName)) { // shape with this name already exists
            Toast toast = Toast.makeText(getApplicationContext(), newName + " already exists", Toast.LENGTH_SHORT);
            toast.show();

        } else { // changing shape name

            Toast toast = Toast.makeText(getApplicationContext(), selectedShapeName + " is now named " + newName, Toast.LENGTH_SHORT);
            toast.show();

            // TODO: actually change the name of the shape
            // update EditPageView (allShapes and shapesOnPage)
            EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
            editPV.renameShape(selectedShapeName, newName);

            // update spinner/shapes
            shapes.remove(selectedShapeName);
            shapes.add(newName);
            setShapeSpinner();

            // update singleton
        }
        newShape.setText("");
    }

    public void shapeText(View view) {
        EditText shapeText = findViewById(R.id.shapeText);
        String text = shapeText.getText().toString();

    }

    private void zeroFields() {

        EditText shapeName = findViewById(R.id.shape_name);
        EditText xText = findViewById(R.id.xText);
        EditText yText = findViewById(R.id.yText);
        EditText heightText = findViewById(R.id.heightText);
        EditText widthText = findViewById(R.id.widthText);
        EditText shapeText = findViewById(R.id.shapeText);

        shapeName.setText("");
        xText.setText("");
        yText.setText("");
        heightText.setText("");
        widthText.setText("");
        shapeText.setText("");
    }


    /*
    * 1. check to see if shape already exists
    * 2. give it a default name, if necessary
    * 3. display toast saying shape has been created
    * 4. add shape to allShapes, shapesOnPage
    */
    // BShape(String page, String name, int height, int width, boolean visible, boolean movable, float x, float y, BitmapDrawable img, String imgName, String txtName, String script)
    public void createNewShape(View view) { // TODO: add to allShapes and shapesOnPage from EditPageView

        EditText newShape = (EditText)findViewById(R.id.shape_name);
        String shapeEntered = newShape.getText().toString();
        EditText xText = findViewById(R.id.xText);
        EditText yText = findViewById(R.id.yText);
        EditText heightText = findViewById(R.id.heightText);
        EditText widthText = findViewById(R.id.widthText);
        Switch visibleSwitch = findViewById(R.id.visible_id);
        Switch movableSwitch = findViewById(R.id.movable_id);
        Spinner imgSpinner = findViewById(R.id.images_spinner);
        EditText shapeText = findViewById(R.id.shapeText);

        String page = singleton.getCurrentPage(); // TODO: when placeholder removed, access from EditText View
        String x = xText.getText().toString();
        String y = yText.getText().toString();
        String width = heightText.getText().toString();
        String height = widthText.getText().toString();

        if (x.equals("") || y.equals("") || height.equals("") || width.equals("")) {
            Toast toast4 = Toast.makeText(getApplicationContext(), "X, Y, Width, Height MUST all have VALUES!", Toast.LENGTH_SHORT);
            toast4.show();
        } else if (shapes.contains(shapeEntered)) {
            Toast toast = Toast.makeText(getApplicationContext(), shapeEntered + " already exists", Toast.LENGTH_SHORT);
            toast.show();
            return;
        } else {

            if (shapeEntered.toString().equals("")) shapeEntered = "shape" + (singleton.getCurrentGameShapes().size() + 1);


            boolean visible = visibleSwitch.isChecked();
            boolean movable = movableSwitch.isChecked();
            BitmapDrawable img = null;
            String imageName = "";
            if (imgSpinner.getSelectedItem() != null) {
                imageName = imgSpinner.getSelectedItem().toString();
                img = drawableMap.get(imageName);
            }
            String script = shapeScript;
            String txtName = "";
            if (shapeText.getText() != null) {
                txtName = shapeText.getText().toString();
            }


            int heightInt = Integer.parseInt(height);
            heightInt *= BShape.PLAY_TO_EDIT;

            int widthInt = Integer.parseInt(width);
            widthInt *= BShape.PLAY_TO_EDIT;

            float xF = Float.parseFloat(x);
            xF *= BShape.PLAY_TO_EDIT;

            float yF = Float.parseFloat(y);
            yF *= BShape.PLAY_TO_EDIT;

            BShape shape = new BShape(page, shapeEntered, heightInt, widthInt,
                    visible, movable, xF, yF, img, imageName, txtName, script);

            EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
            if (editPV.newShapePositionLegal(shape)) {

                editPV.addShape(shape);
                shapes.add(shapeEntered);
                Toast toast2 = Toast.makeText(getApplicationContext(), shapeEntered + " has been created!", Toast.LENGTH_SHORT);
                toast2.show();
                setShapeSpinner();
                newShape.setText("");

            } else {
                Toast toast3 = Toast.makeText(getApplicationContext(), "Cannot create new shape that overlaps with existing shapes.", Toast.LENGTH_SHORT);
                toast3.show();
            }

            shapeScript = "";
            scriptFinished = false;

            EditPageView EditPV = (EditPageView) findViewById(R.id.EditPageView);
            EditPV.updateDB();
            singleton.setCurrentGamePages();
        //    setShapeSpinner();
            // zero out fields
            xText.setText("");
            yText.setText("");
            widthText.setText("");
            heightText.setText("");
        }
    }

    /*
     * Clears the script of the currently-selected shape.
     */
    public void clearScript(View view) {

        EditPageView EditPV = findViewById(R.id.EditPageView);
        EditPV.clearScript();

        // zero out spinners
        Spinner spinner = (Spinner) findViewById(R.id.script_spinner);
        TextView scriptText = findViewById(R.id.script_text);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, triggers);
        spinner.setAdapter(adapter);
        scriptText.setText("Create Script");
        scriptFinished = true;
        shapeScript = "";
        onDrop = false;
    }

    public void editShape(View view) {

        EditPageView editPV = (EditPageView) findViewById(R.id.EditPageView);
        if (editPV.getSelectedShape() == null){
            return;
        }


        EditPageView EditPV = (EditPageView) findViewById(R.id.EditPageView);
        int spinnerInd = shapes.indexOf(EditPV.getSelectedShape().getName());
        EditPV.editSelectedShape(shapeScript, scriptFinished);


        String newName = ((EditText) findViewById(R.id.shape_name)).getText().toString();

        shapeScript = "";
        scriptFinished = false;

        shapes.set(spinnerInd, newName);
        setShapeSpinner();

        BShape storage = null;

        if (editPV.getSelectedShape() != null) {
            storage = editPV.getSelectedShape();
        }

        singleton.setCurrentGamePages();
        singleton.setCurrentGameShapes();

        if (editPV.getSelectedShape() != null) {
            storage = editPV.getSelectedShape();
            editPV.setSelectedShape(storage.getName());
        }
        editPV.displaySelected();
    }

}









