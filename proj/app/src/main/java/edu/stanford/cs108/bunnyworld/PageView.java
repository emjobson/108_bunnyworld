package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.drawable.BitmapDrawable;

import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by jaydinsmallkrok on 3/3/18.
 */


public class PageView extends View {

    private static final float POSS_RATIO = .75f;

    private static final float POSS_RESIZE = .50f;

    private static final float NORM_RESIZE = 2.0f;

    // Mapping from page name to map of page's shapes which maps shape names to shapes
    private Map<String, Map<String, BShape>> pages;
    private String currentPage;

    // Master map containing all the shapes of all pages
    private Map<String, BShape> allShapes;

    // Maps names of currently possessed shapes to shapes
    private Map<String, BShape> possessions;

    // Visible shapes holds all shapes that the user can see -- this includes both
    // the shapes that are on the page (i.e. the top) and those that are within
    // the user's possessions.
    private List<BShape> currentVisibles;

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


    public PageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        init();

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
     * Creates a copy of the map of pages, initializes possessions and visible shapes
     * Devin
     */
    private void init() {

        // Get the database

        SingletonData singleton = SingletonData.getInstance();
        db = singleton.getDB();

        initDrawables();
        initMusic();

        String gameName = singleton.getCurrentGame();

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

            BShape newShape = BShape.buildShape(shapeDescription, drawableMap, 1.00);
            newShape.setSeeHiddenEditMode(false); // default all hidden edit modes to false because this is playmode
            String pageName = newShape.getPage();

            if (!pages_from_db.containsKey(pageName)){ // If the internal map does not yet exist, initialize it
                HashMap<String, BShape> nameToShapes = new HashMap<String, BShape>();
                pages_from_db.put(pageName, nameToShapes);
            }
            pages_from_db.get(pageName).put(newShape.getName(), newShape);
        }

        pages = new HashMap<String, Map<String, BShape>>();
        pages.putAll(pages_from_db);
        possessions = new HashMap<String, BShape>();
        currentVisibles = new ArrayList<BShape>();
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

        currentPage = "page1";
        // BUGFIX: the only shapes that should be in the currentVisibles are those which are set to visible
        for (BShape shape : pages.get(currentPage).values()){
            if (shape.getVisible()) {
                currentVisibles.add(shape);
            }
        }
        invalidate();

    }


    /*
     * Private helper takes in shape being dragged. Iterates through list of visible shapes,
     * returning the uppermost visible shape that overlaps with our dragShape. Returns null
     * if no shape overlaps.
     * Elliott
     */
    private BShape findSecondShape(BShape dragShape) {
        System.out.println(currentVisibles.size());
        for (int i = currentVisibles.size() - 1; i >= 0; i--) {
            BShape curShape = currentVisibles.get(i);
            System.out.println(curShape.getName());
            if (curShape.shapesOverlap(dragShape) && !curShape.equals(dragShape)) return curShape;
        }
        return null;
    }

    /**
     * Method overrides onTouchEvent for our Custom View, overriding the behavior for mouse
     * clicks, drags, and releases.
     *
     * Click: Set information necessary for changing the location of the BShape later on.
     * Drag: Changes location of current BShape, sets BShapes for which the current BShape
     * is droppable as glowing, and redraws the visible images.
     * Release: Handle onClick and onDrop for the appropriate shapes.
     * @param event
     * @return
     * devin and elliott
     *
     * Bugs to fix:
     * -drag shape-shape interaction/behavior currently doesn't take into account whether one of the overlapping objects is simply invisible
     * -drag/touch behavior currently doesn't look at visibility (easy to fix)
     * -TODO: think about cases where isClicked is set at one point, and might affect things in the wrong place
     * -fix onClick (not working for bunny) -> setScript currently crashes
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){

        System.out.println("start of motion event");
        //x and y locations representing where user clicks

        float xClicked = event.getX();
        float yClicked = event.getY();
        BShape clickedShape = findShape(xClicked, yClicked);


        if (event.getAction() == MotionEvent.ACTION_UP) heldShape = null;
        if (clickedShape == null) return true; // clicked on whitespace


        // note: not entirely sure why drag->move behavior was allowed, given the line above

        System.out.println("start of event involving shape: " + clickedShape.getName());
        System.out.println("movability for current shape: " + String.valueOf(clickedShape.getMovable()));


        // start of drag
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            setFamilyGlow(clickedShape, true); // Turn on glow

            clickedShape.setClicked(true);
            heldShape = clickedShape;
            handleOnClick(clickedShape);

            if (clickedShape.getMovable()) {
                // set pre drag location, calculate and set offset
                clickedShape.setPreDrag();
                clickedShape.setOffset(xClicked - clickedShape.getX(),
                        yClicked - clickedShape.getY());
            }

            // end of drag or click
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            setFamilyGlow(clickedShape, false); // turn off glow

            // only want to move shape or handle onClick if we STARTED our click on the shape
            // i.e. don't wanna do stuff if you click outside shape on whitespace, then move to shape
            if (clickedShape.getClicked()) {
                if (clickedShape.getMovable()) {
                    handleEndDrag(xClicked, yClicked, clickedShape);

                }
            }

            clickedShape.setClicked(false);
            invalidate();

            // during drag
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && clickedShape.getMovable()) { // handling redrawing during drag

            if (clickedShape.getClicked()) {
                clickedShape.setLocation(event.getX(), event.getY());
                invalidate();
            }
        }

        return true;
    }

    private void setFamilyGlow(BShape clickedShape, boolean glowing) {
        for (BShape shape: currentVisibles){ // TODO: check to see if this is problematic (slows rendering during drag)
            if (shape.getDroppableShapes().contains(clickedShape.getName())){
                shape.setGlowing(glowing);
            }
        }
    }

    /*
     * Private helper method takes in the final motion event coordinates, along with the shape
     * that was initially clicked-on. Handles the end of drag by placing the shape in the appropriate
     * final location (either snapping back to its original location, remaining where it sits
     * in whitespace, or if it overlapped with the possessions line, snapping into possessions or into
     * the game).
     *
     * Also removes the glowing attribute of other shapes.
     *
     * Note: does not call invalidate
     */
    private void handleEndDrag(float x, float y, BShape clickedShape) {

        // TODO: Handle snapping when over a TEXT -- it seems to only respect the left portion
        // TODO: of the text. So if you drop it halfway through it won't snap, but if you drop it on the
        // TODO: far left it will snap
        // handle final location
        BShape underneathShape = findSecondShape(clickedShape);
        System.out.println("Clicked shape is: " + clickedShape.getName());
        if (underneathShape != null) {
            System.out.println("Found underneath shape: " + underneathShape.getName());
        }
        if (underneathShape != null) { // if overlap with other shape

            if (underneathShape.getDroppableShapes().contains(clickedShape.getName())) {
                System.out.println("Shape " + clickedShape.getName() + " dropped on shape " + underneathShape.getName());
                Map<String,List<String>> onDropCommand = underneathShape.getOnDropScript();
             //   System.out.println("Here is the on drop command_____________");
            //    printArr(onDropCommand);
                executeScript(onDropCommand.get(clickedShape.getName()));
            }

            if (underneathShape.getText() == "") { // only reset if the underneath shape is an img, not text
                clickedShape.resetToPreDrag(); // NOTE: always sends object back to pre-location, even when interacting with shape underneath
            }
        } else { // no overlap with other shape, so drop where it stands

            boolean overlapsPossLine;
            if (clickedShape.isPossessionSized()){ // handling edgecase for when a small-sized possession is placed near the border but not on it
                overlapsPossLine = (clickedShape.getY() <= possBorderY && clickedShape.getY() + clickedShape.getOriginalHeight() >= possBorderY);
            } else {
                overlapsPossLine = clickedShape.containsCoords(x, possBorderY);
            }

            if (overlapsPossLine){ // If it is on top of the border, snap back
                float shapeMidline = clickedShape.getY() + (float) (0.5 * clickedShape.getHeight());
                clickedShape.setOffset(clickedShape.getOffset_x(), 0);
                if (shapeMidline > possBorderY){ // If midline is above poss border then its not in possessions now
                    addToPossessions(clickedShape);
                    clickedShape.setLocation(x, possBorderY);
                } else { // if midline is below poss border, it is now in possessions
                    removeFromPossessions(clickedShape);
                    clickedShape.setLocation(x, possBorderY - clickedShape.getHeight());
                }
                clickedShape.setOffset(0, 0);
            } else {
                clickedShape.setLocation(x, y);
                clickedShape.setOffset(0, 0); // reset offset because shape's location is finalized
                if (y > possBorderY){ // If it is greater than the border, it must be in possessions.
                                      // NOTE: It will truly be less than the border b/c otherwise it would've snapped back
                    addToPossessions(clickedShape);
                    pages.get(currentPage).remove(clickedShape);
                } else { // it must not be in possessions
                    removeFromPossessions(clickedShape);
                }
            }
        }

        // 'unglow' all other shapes since no longer dragging
        setFamilyGlow(clickedShape, false);
    }

    private void addToPossessions(BShape nowPossession){
        possessions.put(nowPossession.getName(), nowPossession);
        if (!nowPossession.isPossessionSized()){ // If not yet possession-sized
            nowPossession.resizeDown();
            nowPossession.setPossessionSized(true);
        }

    }

    private void removeFromPossessions(BShape nonePossession){
        possessions.remove(nonePossession.getName());
        if (nonePossession.isPossessionSized()) { // If possession-sized
            nonePossession.resizeUp();
            nonePossession.setPossessionSized(false);
        }
    }

    private void handleOnClick(BShape clickedShape) {
        System.out.println("handling onClick");
        // iterate through shape's onClicked methods
        List<String> onClickCommand = clickedShape.getOnClickScript();
        printArr(onClickCommand); // TODO: remove this after fixing onClick bug
        executeScript(onClickCommand);
        System.out.println("The onClickCommand is " + onClickCommand.toString());
    }

    protected void printArr(List<String> arr) { // changed to protected for use in BShape
        System.out.println("printing arr:");
        for (String elem : arr) System.out.println(elem + " ");
    }



    /**
     * Interprets and executes scripts -- called after some sort of
     * trigger event. Makes the assumption that the list of commands
     * is composed of two-command strings, i.e. of the form:
     * 'goto bunnyPage'
     * 'play squeakSound'
     * etc.
     * Devin
     *
     * Note: client (not user, but client) should refer to sounds within commands
     * by the resource ID. Can have the user select sounds by intuitive
     * names which map to resource IDs. I.e. user selects Carrot which makes to R.ID.CarrotSound etc.
     * @param commands
     */
    private void executeScript(List<String> commands){
        printArr(commands);
        for (int i = 0; i < commands.size(); i++){
            String[] thisCommand = commands.get(i).split(" ");
            String commandType = thisCommand[0]; // The first word should correspond to goto, play, hide, or show
            String commandParam = thisCommand[1]; // The second word should correspond to the command param, i.e.
                                                  // some sound name, some page destination, etc.

            switch(commandType){
                case "goto":
                    String destination = commandParam;
                    if (pages.containsKey(destination)){ // If it is a valid page
                        switchToPage(destination);
                    }
                    break;

                case "play":
                    String soundName = commandParam;
                    int soundResourceID = soundRes.get(soundName); // Switching to int b/c has to be so
                    System.out.println("The soundResourceID is " + soundResourceID);
                    final MediaPlayer mp = MediaPlayer.create(getContext(),soundResourceID);
                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mp.release(); // finish current activity
                        }
                    });
                    break;

                case "hide":
                    BShape toHide = allShapes.get(commandParam); // Find the shape
                    if (toHide != null) { // Failsafe in case the shape has actually been deleted in editing and the script not updated
                        toHide.setVisible(false);
                        if (currentVisibles.contains(toHide)) { // Check if it is a currently visible shape (i.e. on page/possessions)
                            currentVisibles.remove(toHide);
                        }
                    }
                    invalidate();
                    break;

                case "show":
                    BShape toShow = allShapes.get(commandParam); // Find the shape
                    if (toShow != null) { // Failsafe in case the shape has actually been deleted in editing and the script not yet updated
                        if (pages.get(currentPage).containsKey(toShow.getName())) { // if its a shape of the current page
                            if (!currentVisibles.contains(toShow)) {
                                toShow.setVisible(true);
                                currentVisibles.add(toShow); // if it's not yet a currently visible shape, indicate that it is
                            }
                        } else { // If its a shape of the current page
                            toShow.setVisible(true);
                        }
                    }
                    invalidate();
                    break;
            }
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

        currentVisibles.clear(); // empty visible shapes
        currentPage = destinationPage;
        System.out.println("shapes for this page are: ");
        for (BShape shape : pages.get(currentPage).values()){ // for all the shapes of the new page
            System.out.println(shape.getName());

            if (shape.getVisible()){ // if the shape is visible,
                currentVisibles.add(shape); // add it to current visible shapes
            }
        }

        currentVisibles.addAll(possessions.values()); // add back in all of the possessions
        for (BShape shape : currentVisibles){
            System.out.println(shape.getName());
        }
        invalidate(); // Redraw page

        for (BShape visibleShape : currentVisibles){
            if (!visibleShape.getOnEnterScript().equals("")){
                executeScript(visibleShape.getOnEnterScript());
            }
        }

    }


    /*
     * Private helper method takes in x and y coordinates as input, returning the uppermost
     * shape found in currentVisibles.
     * Devin and Elliott
     */
    private BShape findShape(float x, float y){

        // ensure that heldShape is returned if shapes are overlapping
        if (heldShape != null) {
            if (heldShape.containsCoords(x, y)) return heldShape;
        }

        for (int i = currentVisibles.size() - 1; i >= 0; i--){
            BShape curShape = currentVisibles.get(i);
            if (curShape.containsCoords(x, y)){
                System.out.println("found shape!" + curShape.getName());
                return curShape;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        for (BShape shape : currentVisibles) {
            shape.drawShape(canvas);
        }

        for (String shapeName: possessions.keySet()) {
            BShape shape = possessions.get(shapeName);
            shape.drawShape(canvas);
        }

        //draws border line between the possesions view and page vi
        canvas.drawLine(0.0f, possBorderY, pageWidth, possBorderY, paint);
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

        System.out.println(possBorderY);
        System.out.println(pageHeight);
        possBorderY = POSS_RATIO * pageHeight;

        System.out.println("Page width: " + pageWidth);
        System.out.println("Page height: " + pageHeight);

    }

}
