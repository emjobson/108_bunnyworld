package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.app.backup.BackupHelper;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by devin on 3/15/2018.
 */

public class EditPageView extends View {

    // Mapping from page name to map of page's shapes which maps shape names to shapes
    private Map<String, Map<String, BShape>> pages;
    private String currentEditingPage;

    private String gameName;

    // Master map containing all the shapes of all pages
    private Map<String, BShape> allShapes;

    // shape which is currently clicked/selected by the user for editing
    private BShape selectedShape;


    // Shapes on page holds all shapes from the page
    private List<BShape> shapesOnPage;

    //Y co-ordinate of the border between the "possesions view" and "page view"
    private float possBorderY;

    //gives the width of the entire page view
    private float pageWidth;
    private float pageHeight;

    //paint to draw the border line between possesions view and page view
    private Paint paint;

    private BShape heldShape;

    // Singleton and Database for shape/page information
    private SingletonData singletonData;
    private SQLiteDatabase db;

    private Map<String,BitmapDrawable> drawableMap;
    private Map<String, Integer> soundRes;


    public EditPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        init();

    }

    /**
     * Private helper method called whenever the user hits "save." Updates our database
     * by storing all the shapes in allShapes.
     */
    public void updateDB() {

        // clear database
        String resetString = "DROP TABLE IF EXISTS " + gameName;
        db.execSQL(resetString);
        MainActivity.setupDatabase(db, gameName);

        String insertString = "INSERT INTO " + gameName + " VALUES";

        // iterate through allShapes, adding where necessary

        for (Map.Entry<String, BShape> entry : allShapes.entrySet()) {

            BShape shape = entry.getValue();

            double newHeight = shape.getHeight() * BShape.EDIT_TO_PLAY;
            int newHeightInt = (int) Math.round(newHeight);
            shape.setHeight(newHeightInt);

            double newWidth = shape.getWidth() * BShape.EDIT_TO_PLAY;
            int newWidthInt = (int) Math.round(newWidth);
            shape.setWidth(newWidthInt);

            double newX = shape.getX() * BShape.EDIT_TO_PLAY;
            int newXInt = (int) Math.round(newX);
            shape.setX((float)newXInt);

            double newY = shape.getY() * BShape.EDIT_TO_PLAY;
            int newYInt = (int) Math.round(newY);
            shape.setY((float)newYInt);


            insertString += shape.constructDBString() + ",";
        }

        insertString = insertString.substring(0, insertString.length() - 1); // remove final comma
        insertString += ";";


        db.execSQL(insertString);
        BShape storage = null;
        if (selectedShape != null) {
            storage = selectedShape;
        }
        init();
        selectedShape = storage;
    }

    /*
     * Private helper method initializes the BitmapDrawable objects and stores them
     * in our PageView's ivars.
     */
    private void initDrawables() {

        drawableMap = new HashMap<String, BitmapDrawable>();

        BitmapDrawable carrotDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.carrot);
        BitmapDrawable carrot2Drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.carrot2);
        BitmapDrawable deathDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.death);
        BitmapDrawable duckDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.duck);
        BitmapDrawable fireDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.fire);
        BitmapDrawable mysticDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.mystic);

        drawableMap.put("carrot", carrotDrawable);
        drawableMap.put("carrot2", carrot2Drawable);
        drawableMap.put("death", deathDrawable);
        drawableMap.put("duck", duckDrawable);
        drawableMap.put("fire", fireDrawable);
        drawableMap.put("mystic", mysticDrawable);
    }

    private void initMusic(){
        soundRes = new HashMap<String, Integer>();

        soundRes.put("munching", R.raw.munching);
        soundRes.put("carrotcarrotcarrot", R.raw.carrotcarrotcarrot);
        soundRes.put("evillaugh", R.raw.evillaugh);
        soundRes.put("fire", R.raw.fire);
        soundRes.put("hooray", R.raw.hooray);
        soundRes.put("munch", R.raw.munch);
        soundRes.put("woof", R.raw.woof);
    }

    /**
     * Creates a copy of the map of pages, initializes shapes
     * Devin
     */
    private void init() {

        // Get the database

        SingletonData singleton = SingletonData.getInstance();
        db = singleton.getDB();

        initDrawables();
        initMusic();

        gameName = singleton.getCurrentGame();

        Cursor cursorShp = db.rawQuery(
                "SELECT * FROM " + gameName, null);
        // Stores the details of each shape for the shape to be constructed, delimited by commas
        ArrayList<String> shapeDetails = new ArrayList<String>();

        while (cursorShp.moveToNext()) {
            String output = "";
            for (int i = 0; i <= 10; i++){

                output += cursorShp.getString(i);
                if (i < 10){ // So that there is no comma after the final value
                    output += ',';
                }
            }
            shapeDetails.add(output);
        }

        HashMap<String, Map<String, BShape>> pages_from_db = new HashMap<String, Map<String, BShape>>();
        // Loop through every shape, construct it, then add it to the pages_from_db with its proper page
        for (String shapeDescription : shapeDetails){
            BShape newShape = BShape.buildShape(shapeDescription, drawableMap, BShape.PLAY_TO_EDIT);
            String pageName = newShape.getPage();

            if (!pages_from_db.containsKey(pageName)){ // If the internal map does not yet exist, initialize it
                HashMap<String, BShape> nameToShapes = new HashMap<String, BShape>();
                pages_from_db.put(pageName, nameToShapes);
            }
            pages_from_db.get(pageName).put(newShape.getName(), newShape);
        }

        pages = new HashMap<String, Map<String, BShape>>();
        pages.putAll(pages_from_db);
        shapesOnPage = new ArrayList<BShape>();
        heldShape = null;

        allShapes = new HashMap<String, BShape>();
        // Loop through all shapes of the map and add them to allShapes if they are not
        // yet there
        for (String pageName: pages.keySet()){
            for (BShape shape: pages.get(pageName).values()){
                if (!allShapes.containsKey(shape.getName())){
                    allShapes.put(shape.getName(), shape);
                }
            }
        }

        currentEditingPage = singleton.getCurrentPage();
        // BUGFIX: the only shapes that should be in the shapesOnPage are those which are set to visible
        for (BShape shape : pages.get(currentEditingPage).values()){
                shapesOnPage.add(shape);
        }
        invalidate();

        selectedShape = null; // Defaults to null

    }



    /**
     * Returns true if two shapes overlap by
     * checking each of the four corners of each of the two
     * shapes and testing if they are within the bounds of the
     * other shape
     * @param shape1
     * @param shape2
     * @return
     * Elliott
     */
    private boolean shapesOverlap(BShape shape1, BShape shape2) {

        // check if a corner of shape1 is within shape2
        if (shape2.containsCoords(shape1.getX(), shape1.getY())) return true;
        if (shape2.containsCoords(shape1.getX() + shape1.getWidth(), shape1.getY())) return true;
        if (shape2.containsCoords(shape1.getX() + shape1.getWidth(), shape1.getY()+ shape1.getHeight())) return true;
        if (shape2.containsCoords(shape1.getX(), shape1.getY()+ shape1.getHeight())) return true;

        // check if a corner of shape2 is within shape1
        if (shape1.containsCoords(shape2.getX(), shape2.getY())) return true;
        if (shape1.containsCoords(shape2.getX() + shape2.getWidth(), shape2.getY())) return true;
        if (shape1.containsCoords(shape2.getX() + shape2.getWidth(), shape2.getY()+ shape2.getHeight())) return true;
        if (shape1.containsCoords(shape2.getX(), shape2.getY()+ shape2.getHeight())) return true;

        return false;
    }

    /*
     * Private helper takes in shape being dragged. Iterates through list of all shapes on the page,
     * returning the shape that overlaps with our dragShape. Returns null
     * if no shape overlaps.
     * Elliott, edited for this view by Devin
     */
    private BShape findSecondShape(BShape dragShape) {
        // We want to include the invisible shapes
        for (int i = shapesOnPage.size() - 1; i >= 0; i--) {         // because they are semi-visible during
            BShape curShape = shapesOnPage.get(i);                   // editing
            if (shapesOverlap(curShape, dragShape) && !curShape.equals(dragShape)) return curShape;
        }
        return null;
    }

    /**
     * Method overrides onTouchEvent for our Custom EditView, overriding the behavior for drags
     *
     * Drag: Changes location of current BShape, sets BShapes for which the current BShape
     * is droppable as glowing, and redraws the visible images.
     * Release: Handle onDrop for the appropriate shapes.
     * @param event
     * @return
     * devin and elliott -- Edited by Devin for the edit page view
     *
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){

        //x and y locations representing where user clicks

        float xClicked = event.getX();
        float yClicked = event.getY();
        BShape clickedShape = findShape(xClicked, yClicked);


        if (event.getAction() == MotionEvent.ACTION_UP) heldShape = null;
        if (clickedShape == null) {
            EditShapeActivity EditAS = (EditShapeActivity) ((Activity) getContext());
            selectedShape = allShapes.get(EditAS.getShapeSpinnerText());
            return true; // clicked on whitespace
        }




        // start of drag or click for selection
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            clickedShape.setClicked(true);
            selectedShape = clickedShape;
            displaySelected();
            heldShape = clickedShape;
            clickedShape.setPreDrag();
            clickedShape.setOffset(xClicked - clickedShape.getX(),
                    yClicked - clickedShape.getY());

            // end of drag
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            handleEndDrag(xClicked, yClicked, clickedShape);
            clickedShape.setClicked(false);
            invalidate();
            displaySelected();

            // during drag
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && clickedShape.getMovable()) { // handling redrawing during drag

            if (clickedShape.getClicked()) {
                clickedShape.setLocation(event.getX(), event.getY());
                invalidate();
                displaySelected();
            }
        }

        return true;
    }


    /*
     * Private helper method takes in the final motion event coordinates, along with the shape
     * that was initially clicked-on. Handles the end of drag by placing the shape in the appropriate
     * final location (either snapping back to its original location or remaining where it sits
     * in whitespace)
     *     *
     * Note: does not call invalidate
     */
    private void handleEndDrag(float x, float y, BShape clickedShape) {

        BShape underneathShape = findSecondShape(clickedShape);
        if (underneathShape != null) { // if overlap with other shape

            if (underneathShape.getText() == "") { // only reset if the underneath shape is an img, not text
                clickedShape.resetToPreDrag(); // NOTE: always sends object back to pre-location, even when interacting with shape underneath
            }
        }
    }


    protected void printArr(List<String> arr) { // changed to protected for use in BShape
        System.out.println("printing arr:");
        for (String elem : arr) System.out.println(elem + " ");
    }

    /**
     * Takes the currently selected shape and displays all of its details on the
     * edit shape page. Must be manually called every time that a new shape is selected
     */
    public void displaySelected(){

        if (selectedShape == null) return;

        EditText shapeText = (EditText) ((Activity) getContext()).findViewById(R.id.shapeText);
        shapeText.setText(selectedShape.getText());

        EditText heightText = (EditText) ((Activity) getContext()).findViewById(R.id.heightText);
        double newHeight = selectedShape.getHeight() * BShape.EDIT_TO_PLAY;
        int newHeightInt = (int) Math.round(newHeight);
        String newHeightStr = String.valueOf(newHeightInt);
        heightText.setText(newHeightStr);

        EditText widthText = (EditText) ((Activity) getContext()).findViewById(R.id.widthText);
        double newWidth = selectedShape.getWidth() * BShape.EDIT_TO_PLAY;
        int newWidthInt = (int) Math.round(newWidth);
        String newWidthStr = String.valueOf(newWidthInt);
        widthText.setText(newWidthStr);

        EditText x_text = (EditText) ((Activity) getContext()).findViewById(R.id.xText);
        double newX = selectedShape.getX() * BShape.EDIT_TO_PLAY;
        int newXInt = (int) Math.round(newX);
        String newXStr = String.valueOf(newXInt);
        x_text.setText(newXStr);

        EditText y_text = (EditText) ((Activity) getContext()).findViewById(R.id.yText);
        double newY = selectedShape.getY() * BShape.EDIT_TO_PLAY;
        int newYInt = (int) Math.round(newY);
        String newYStr = String.valueOf(newYInt);
        y_text.setText(newYStr);

        EditText shape_name = (EditText) ((Activity) getContext()).findViewById(R.id.shape_name);
        shape_name.setText(selectedShape.getName());

        Switch visibleSwitch = (Switch) ((Activity) getContext()).findViewById(R.id.visible_id);
        visibleSwitch.setChecked(selectedShape.getVisible());

        Switch movableSwitch = (Switch) ((Activity) getContext()).findViewById(R.id.movable_id);
        movableSwitch.setChecked(selectedShape.getMovable());

        Spinner imgSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.images_spinner);
        String imageName = selectedShape.getImgName();
        EditShapeActivity EditAS = (EditShapeActivity) ((Activity) getContext());

        if (imageName == "null" || imageName  == ""){
            imgSpinner.setSelection(0);
        } else {
            ArrayList<String> imgNames = EditAS.getImageNames();
            int indexOfImage = imgNames.indexOf(imageName);
            imgSpinner.setSelection(indexOfImage);
        }

        Spinner shapesSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.main_shapes_spinner);
        ArrayList<String> spinnerShapes = EditAS.getShapesArr();

        int shapeNames = spinnerShapes.indexOf(selectedShape.getName());
        shapesSpinner.setSelection(shapeNames);
    }

    public void clearScript() {
        if (selectedShape != null) {
            selectedShape.clearScripts();
        }
    }

    public BShape getSelectedShape() {
        return selectedShape;
    }

    public void setSelectedShape(String shapeName){
        selectedShape = allShapes.get(shapeName);
    }

    public void editSelectedShape(String shapeScript, boolean scriptFinished){


        if (selectedShape != null) {
            EditText shapeText = (EditText) ((Activity) getContext()).findViewById(R.id.shapeText);
            selectedShape.setText(shapeText.getText().toString());

            Spinner imageSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.images_spinner);
            selectedShape.setIMG(drawableMap.get(imageSpinner.getSelectedItem().toString()));
            selectedShape.setImgName(imageSpinner.getSelectedItem().toString());

            EditText heightText = (EditText) ((Activity) getContext()).findViewById(R.id.heightText);
            double newHeight = Float.parseFloat(heightText.getText().toString()) * BShape.PLAY_TO_EDIT;
            int newHeightInt = (int) Math.round(newHeight);
            selectedShape.setHeight(newHeightInt);

            EditText widthText = (EditText) ((Activity) getContext()).findViewById(R.id.widthText);
            double newWidth = Float.parseFloat(widthText.getText().toString()) * BShape.PLAY_TO_EDIT;
            int newWidthInt = (int) Math.round(newWidth);
            selectedShape.setWidth(newWidthInt);

            EditText x_text = (EditText) ((Activity) getContext()).findViewById(R.id.xText);
            double newX = Float.parseFloat(x_text.getText().toString()) * BShape.PLAY_TO_EDIT;
            selectedShape.setX((float) newX);

            EditText y_text = (EditText) ((Activity) getContext()).findViewById(R.id.yText);
            double newY = Float.parseFloat(y_text.getText().toString()) * BShape.PLAY_TO_EDIT;
            selectedShape.setY((float) newY);

            EditText shape_name = (EditText) ((Activity) getContext()).findViewById(R.id.shape_name);
            selectedShape.setName(shape_name.getText().toString());

            Switch visibleSwitch = (Switch) ((Activity) getContext()).findViewById(R.id.visible_id);
            selectedShape.setVisible(visibleSwitch.isChecked());

            Switch movableSwitch = (Switch) ((Activity) getContext()).findViewById(R.id.movable_id);
            selectedShape.setMovable(movableSwitch.isChecked());

            if (scriptFinished){ // If the script is complete, add it to the shape
                selectedShape.setScript(shapeScript);
            }


            updateDB();


            invalidate();

        }

    }

    /**
     * Handles switching to new page, updating of the visible shapes
     * set for that page.
     * @param destinationPage
     * Devin
     */
    private void switchToPage(String destinationPage){

        heldShape = null; // Drop any held shape as user transitions to different page.

        shapesOnPage.clear(); // empty visible shapes
        currentEditingPage = destinationPage;
        for (BShape shape : pages.get(currentEditingPage).values()){ // for all the shapes of the new page

            if (shape.getVisible()){ // if the shape is visible,
                shapesOnPage.add(shape); // add it to current visible shapes
            }
        }
        invalidate(); // Redraw page

    }


    /*
     * Private helper method takes in x and y coordinates as input, returning the uppermost
     * shape found in shapesOnPage.
     * Devin and Elliott
     */
    private BShape findShape(float x, float y){

        // ensure that heldShape is returned if shapes are overlapping
        if (heldShape != null) {
            if (heldShape.containsCoords(x, y)) return heldShape;
        }

        for (int i = shapesOnPage.size() - 1; i >= 0; i--){
            BShape curShape = shapesOnPage.get(i);
            if (curShape.containsCoords(x, y)){
                return curShape;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        for (int i = 0; i < shapesOnPage.size(); i++){
            BShape shape = shapesOnPage.get(i);
            shape.drawShape(canvas);
        }

        if (heldShape != null) heldShape.drawShape(canvas);
    }

    /*
     * determines the size of the page view, only is called when view is
     * resized
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pageWidth = w;
        pageHeight = h;


    }

    /*
     * Public helper method called by editor to determine whether or not a new shape can
     * be created and placed on the custom view. Returns true if the new shape doesn't overlap
     * with any other shapes currently existing on the page, false otherwise.
     * Elliott
     */
    public boolean newShapePositionLegal(BShape shape) {
        for (BShape otherShape : shapesOnPage) {
            if (otherShape.shapesOverlap(shape)) return false;
        }
        return true;
    }

    public void addShape(BShape toAdd){
        allShapes.put(toAdd.getName(), toAdd);
        shapesOnPage.add(toAdd);
        invalidate();
    }

    public void renameShape(String oldShapeName, String newName) {

        selectedShape.setName(newName);
        BShape shape = allShapes.get(oldShapeName);

        if (allShapes.containsValue(shape)) {
            allShapes.get(shape.getName()).setName(newName);
        }

        if (shapesOnPage.contains(shape)) {
            shapesOnPage.get(shapesOnPage.indexOf(shape)).setName(newName);
        }
    }

    public void removeShape(String shapeName) {

        if (allShapes.containsKey(shapeName)) {
            allShapes.remove(shapeName);
        }

        BShape toRemove = allShapes.get(shapeName);
        if (shapesOnPage.contains(toRemove)) {
            shapesOnPage.remove(toRemove);
        }

        invalidate();
    }

    public Map<String, BitmapDrawable> getDrawableMap() {
        return drawableMap;
    }

    public Map<String, Integer> getSoundRes() {
        return soundRes;
    }
}
