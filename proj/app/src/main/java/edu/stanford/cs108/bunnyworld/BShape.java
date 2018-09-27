package edu.stanford.cs108.bunnyworld;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import java.util.*;

import static android.graphics.Color.LTGRAY;

/**
 * Created by devin on 3/3/2018.
 */

public class BShape {

    // Constants used for resizing between edit view and play view
    public static final double EDIT_TO_PLAY = 1.48854961832;
    public static final double PLAY_TO_EDIT = 0.67179487179;


    private String page;

    private static final int POSSESSION_WIDTH = 100;

    private static final int DEFAULT_TEXT_SIZE = 50;

    // Location of shape, origin at top left
    private float x;
    private float y;

    // Strings storing the names of the image file and the txt file
    private BitmapDrawable img;
    private String text;
    private float textSize;
    private String imgName;

    // Name of BShape, its dimensions
    private String name;
    private int height;
    private int width;

    // Store original height and width for easy resizing between possessions and page items
    private int originalHeight;
    private int originalWidth;

    // Variables to keep track of its mode
    private boolean visible;
    private boolean movable;
    private boolean isGlowing;
    private boolean isClicked;
    private boolean seeHiddenEditMode;
    private boolean possessionSized;

    // Scripts for behavior given different triggers
  //  private List<String> onDropScript;
    private Map<String, List<String>> onDropScript;
    private List<String> onEnterScript;
    private List<String> onClickScript;

    // Holds the names of the shapes which can be dropped on this
 //   private Set<String> droppableShapes;

    // Color, initialized to light grey
    private Paint shapeColor;

    // Color used for the glow effect
    private Paint glowColor;
    private Paint seeHiddenColor;
    private Paint translucentPaint;

    // Location of shape, at beginning of drag movement
    private float preDragX;
    private float preDragY;

    // Offset to account for difference between origin and region of
    // shape which user has touched
    private float offset_x;
    private float offset_y;

    /**
     * Constructor for BShapes
     * @param page
     * @param name
     * @param height
     * @param width
     * @param visible
     * @param movable
     * @param x
     * @param y
     * devin
     */
    public BShape(String page, String name, int height, int width, boolean visible, boolean movable, float x, float y, BitmapDrawable img, String imgName, String txtName, String script) {
        this.page = page;
        this.name = name;

        this.imgName = imgName;
        this.originalHeight = this.height = height;
        this.originalWidth = this.width = width;
        this.visible = visible;
        this.movable = movable;
        this.isGlowing = false;
        this.isClicked = false;
        this.possessionSized = false;
        this.x = x;
        this.y = y;
        this.text = txtName;
        this.img = img;
        this.seeHiddenEditMode = true; // Default to false

        glowColor = new Paint();
        glowColor.setStyle(Paint.Style.STROKE);
        glowColor.setColor(Color.GREEN);
        glowColor.setStrokeWidth(5.0f);

        seeHiddenColor = new Paint();
        seeHiddenColor.setStyle(Paint.Style.STROKE);
        seeHiddenColor.setColor(Color.BLUE);
        seeHiddenColor.setStrokeWidth(5.0f);

        translucentPaint = new Paint();
        translucentPaint.setAlpha(64);

        shapeColor = new Paint();
        shapeColor.setColor(Color.LTGRAY);

        // Initialize default values for text
        this.textSize = DEFAULT_TEXT_SIZE;

    //    onDropScript = new ArrayList<String>();
        onDropScript = new HashMap<String, List<String>>();
        onEnterScript = new ArrayList<String>();
        onClickScript = new ArrayList<String>();
    //    droppableShapes = new HashSet<String>();

        if (!script.equals("null") && !script.equals("")){
            setScript(script);
        }
    }

    /*
     * Private helper method converts the script arrays to text, to be used as the script
     * field in the SQL command.
     * Elliott
     */
    private String scriptArraysToText() {

        String ret = "'";

        if (!onClickScript.isEmpty()) {
            ret += "on click";
            for (String str : onClickScript) {
                ret += " " + str;
            }
            ret += ";";
        }
        if (!onEnterScript.isEmpty()) {
            ret += "on enter";
            for (String str : onEnterScript) {
                ret += " " + str;
            }
            ret += ";";
        }

        if (!onDropScript.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : onDropScript.entrySet()) {
                ret += "on drop " + entry.getKey();
                for (String cmd : entry.getValue()) {
                    ret += " " + cmd;
                }
                ret += ";";
            }
        }

        ret += "'";
        if (ret.equals("''")) ret = "NULL";
        return ret;
    }

    /*
     * Public method uses the BShape's ivars to construct a string that represents the shape
     * as a SQL insertion entry.
     */
    public String constructDBString() {

        String imgName = this.imgName.isEmpty() ? "NULL" : "'" + this.imgName + "'";
        String txt = this.text.isEmpty() ? "NULL" : "'" + this.text + "'";
        String visible = this.visible ? "1" : "0";
        String movable = this.movable ? "1" : "0";
        String scriptTxt = scriptArraysToText();


        String ret = "('" + page + "'," + imgName + "," + txt + ","
                + visible + "," + movable + "," + scriptTxt + ","
                + String.valueOf((int)this.x) + "," + String.valueOf((int)this.y) + ","
                + String.valueOf(this.width) + "," + String.valueOf(this.height) + ","
                + "'" + this.name + "')";

        return ret;


    }

    public void setClicked(boolean clicked) {
        this.isClicked = clicked;
    }

    public boolean getClicked() {
        return this.isClicked;
    }

    public void setIMG(BitmapDrawable img){
        this.img = img;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public void setTXT(String text){
        this.text = text;
    }

    public void setVisible(boolean decision){
       this.visible = decision;
    }

    public boolean getVisible(){
        return this.visible;
    }

    public boolean getMovable() {
        return this.movable;
    }


    public void setMovable(boolean decision){
        this.movable = decision;
    }

    /**
     * Sets the location of the shape, correcting for the offset between user's
     * touch-point and origin.
     * @param x
     * @param y
     */
    public void setLocation(float x, float y) {
        this.x = x - offset_x;
        this.y = y - offset_y;
    }

    public void setFontSize(float fontsize){
        this.textSize = fontsize;
    }

    public void setShapeColor(Paint shapeColor) {
        this.shapeColor = shapeColor;
    }

    public void resetToPreDrag(){
        this.x = this.preDragX;
        this.y = this.preDragY;

    }



    /**
     * Returns true if two shapes overlap by
     * checking each of the four corners of each of the two
     * shapes and testing if they are within the bounds of the
     * other shape
     * @param shape2
     * @return
     * Elliott
     */
    public boolean shapesOverlap(BShape shape2) {

        // check if a corner of shape1 is within shape2
        if (shape2.containsCoords(this.getX(), this.getY())) return true;
        if (shape2.containsCoords(this.getX() + this.getWidth(), this.getY())) return true;
        if (shape2.containsCoords(this.getX() + this.getWidth(), this.getY()+ this.getHeight())) return true;
        if (shape2.containsCoords(this.getX(), this.getY()+ this.getHeight())) return true;

        // check if a corner of shape2 is within shape1
        if (this.containsCoords(shape2.getX(), shape2.getY())) return true;
        if (this.containsCoords(shape2.getX() + shape2.getWidth(), shape2.getY())) return true;
        if (this.containsCoords(shape2.getX() + shape2.getWidth(), shape2.getY()+ shape2.getHeight())) return true;
        if (this.containsCoords(shape2.getX(), shape2.getY()+ shape2.getHeight())) return true;

        return false;
    }

    /**
     *  Takes in the parameter of a string describing the dimensions, details of the shape
     *  then builds that shape using the constructor. Assumes that the string is provided
     *  with commas between details
     * @param shapeDescription
     * @return the contructed bshape
     * Devin
     */

    public static BShape buildShape(String shapeDescription, Map<String,BitmapDrawable> drawableMap, double factor){

        String[] shapeDetails = shapeDescription.split(","); // split it up into each detail separately
        String pageName = shapeDetails[0];
        String imgName = shapeDetails[1].equals("null") ? "" : shapeDetails[1];
        String txtName = shapeDetails[2].equals("null") ? "" : shapeDetails[2]; // TODO: make sure this doesn't cause problems
        String shapeName = shapeDetails[10];
        String script = shapeDetails[5];
        int height = Integer.parseInt(shapeDetails[9]);
        height *= factor;
        int width = Integer.parseInt(shapeDetails[8]);
        width *= factor;
        float x = Float.parseFloat(shapeDetails[6]);
        x *= factor;

        float y = Float.parseFloat(shapeDetails[7]);
        y *= factor;
        boolean visible, movable;
        String visibleBit = shapeDetails[3];
        String movableBit = shapeDetails[4];

        visible = visibleBit.equals("1");
        movable = movableBit.equals("1");

        BitmapDrawable img = null;
        if (drawableMap.containsKey(imgName)) {
            img = drawableMap.get(imgName);
        }

        BShape newShape = new BShape(pageName, shapeName, height, width, visible, movable, x, y, img, imgName, txtName, script);

        String dbCommand = newShape.constructDBString();


        newShape.textSize *= factor;
        return newShape;
    }



    /**
     * Draws the shape. If the shape has text, text will take precedence over any
     * associated image. If there is neither an image nor text, then the method
     * will simply draw a grey rectangle.
     * @param canvas
     * Devin and Elliott
     */
    public void drawShape(Canvas canvas) {

        if (seeHiddenEditMode && !this.visible) { // If the shape is invisible and we are in edit see mode draw an outline
            RectF seeHiddenRect = new RectF(this.x, this.y, this.x + this.width, this.y + this.height);
            canvas.drawRect(seeHiddenRect, this.seeHiddenColor);
            if (this.img != null){
                Bitmap bitmap = this.img.getBitmap();
                canvas.drawBitmap(bitmap, null, new RectF(x, y, x + width, y + height), translucentPaint);
            }
        }

        if (!this.visible) return;

        if (!this.text.isEmpty()) { // print text
            Paint pnt = new Paint();
            pnt.setColor(Color.BLACK);
            pnt.setTextSize(textSize);
            // Note: this will draw the baseline at location y
            // it may be necessary to displace this by the height
            // of the text later on
            canvas.drawText(this.text, this.x, this.y, pnt);

        } else if (this.img != null) {

            Bitmap bitmap = this.img.getBitmap();
            canvas.drawBitmap(bitmap, null,
                    new RectF(x, y, x + width, y + height),
                    null);
        } else { // draw grey rectangle
            RectF rect = new RectF(this.x, this.y, this.x + this.width, this.y + this.height);
            canvas.drawRect(rect, this.shapeColor);
        }

        // Draw 'glow' rectangle around the shape, if it is one which can react with the currently
        // selected shape
        if (isGlowing){
            RectF glowRect = new RectF(this.x, this.y, this.x + this.width, this.y + this.height);
            canvas.drawRect(glowRect, this.glowColor);
        }
    }


    public void setPreDrag(){
        this.preDragX = this.x;
        this.preDragY = this.y;
    }

    public void resizeDown(){
        double factor = (this.width * 1.0) / (POSSESSION_WIDTH * 1.0);
        this.height /= factor;
        this.width = POSSESSION_WIDTH;
    }

    public void resizeUp(){
        this.height = this.originalHeight;
        this.width = this.originalWidth;
    }

    public void clearScripts() {
        onClickScript.clear();
        onEnterScript.clear();
        onDropScript.clear();
    }

    /**
     * Method to take in client entered scripts for shape behavior and
     * condition them into strings which are then compiled in the three lists
     * for onDrop, onEnter, and onClick. Assuming that client is inputting a
     * script of clauses where each begins with 'on click', 'on enter', or 'on drop'
     * @param script
     * Devin and Elliott
     *
     * EDIT: Made public so that one can edit shape scripts from the editor
     */

    public void setScript(String script){

        clearScripts(); // calling setScript will delete previous script entries


        if (script.isEmpty()) return;

        String[] clauses = script.split(";");
        for (int i = 0; i < clauses.length; i++) {
            String clause = clauses[i];

            String[] commands = clause.split(" ");


            switch (commands[1]){
                case "click":
                    for (int j = 2; j < commands.length; j += 2){
                        String concatenated = commands[j] + " " + commands[j + 1];
                        onClickScript.add(concatenated);
                    }
                    break;
                case "drop":

                    List<String> cmdList = new ArrayList<String>(); // create command list
                    for (int j = 3; j < commands.length; j += 2) {
                        String concatenated = commands[j] + " " + commands[j + 1];
                        cmdList.add(concatenated);
                    }

                    // either add to existing mapping, or create new mapping
                    if (onDropScript.get(commands[2]) != null) {
                        onDropScript.get(commands[2]).addAll(cmdList);
                    } else {
                        onDropScript.put(commands[2], cmdList);
                    }

                    break;
                case "enter":
                    for (int j = 2; j < commands.length; j += 2){
                        String concatenated = commands[j] + " " + commands[j + 1];
                        onEnterScript.add(concatenated);
                    }
                    break;
                default:

                    break;
            }
        }
    }

    public boolean containsCoords(float x, float y){

        return ((y >= this.y && y <= this.y + height) && (x >= this.x && x <= this.x + width));
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,List<String>> getOnDropScript() {
        return onDropScript;
    }

    public void setOnDropScript(Map<String,List<String>> onDropScript) {
        this.onDropScript = onDropScript;
    }

    public List<String> getOnEnterScript() {
        return onEnterScript;
    }

    public void setOnEnterScript(List<String> onEnterScript) {
        this.onEnterScript = onEnterScript;
    }

    public List<String> getOnClickScript() {
        return onClickScript;
    }

    public void setOnClickScript(List<String> onClickScript) {
        this.onClickScript = onClickScript;
    }

    public Set<String> getDroppableShapes() {
    //    return droppableShapes;
        return onDropScript.keySet();
    }

    public void setGlowing(boolean glowing) {
        isGlowing = glowing;
    }

    public boolean isPossessionSized() {
        return possessionSized;
    }

    public void setPossessionSized(boolean possessionSized) {
        this.possessionSized = possessionSized;
    }

    public void setSeeHiddenEditMode(boolean seeHiddenEditMode) {
        this.seeHiddenEditMode = seeHiddenEditMode;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public int getWidth() {
        return width;
    }

    public float getOffset_x() {
        return offset_x;
    }

    public float getOffset_y() {
        return offset_y;
    }

    public String getImgName() {
        return imgName;
    }

    public void setOffset(float offset_x, float offset_y) {
        this.offset_x = offset_x;
        this.offset_y = offset_y;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BShape bShape = (BShape) o;

        if (Float.compare(bShape.x, x) != 0) return false;
        if (Float.compare(bShape.y, y) != 0) return false;
        if (Float.compare(bShape.textSize, textSize) != 0) return false;
        if (height != bShape.height) return false;
        if (width != bShape.width) return false;
        if (Float.compare(bShape.preDragX, preDragX) != 0) return false;
        if (Float.compare(bShape.preDragY, preDragY) != 0) return false;
        if (page != null ? !page.equals(bShape.page) : bShape.page != null) return false;
        if (img != null ? !img.equals(bShape.img) : bShape.img != null) return false;
        if (text != null ? !text.equals(bShape.text) : bShape.text != null) return false;
        if (name != null ? !name.equals(bShape.name) : bShape.name != null) return false;
        if (onDropScript != null ? !onDropScript.equals(bShape.onDropScript) : bShape.onDropScript != null)
            return false;
        if (onEnterScript != null ? !onEnterScript.equals(bShape.onEnterScript) : bShape.onEnterScript != null)
            return false;
        if (onClickScript != null ? !onClickScript.equals(bShape.onClickScript) : bShape.onClickScript != null)
            return false;
        if (shapeColor != null ? !shapeColor.equals(bShape.shapeColor) : bShape.shapeColor != null)
            return false;
        return glowColor != null ? glowColor.equals(bShape.glowColor) : bShape.glowColor == null;
    }

    @Override
    public int hashCode() {
        int result = page != null ? page.hashCode() : 0;
        result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (img != null ? img.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (textSize != +0.0f ? Float.floatToIntBits(textSize) : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + (onDropScript != null ? onDropScript.hashCode() : 0);
        result = 31 * result + (onEnterScript != null ? onEnterScript.hashCode() : 0);
        result = 31 * result + (onClickScript != null ? onClickScript.hashCode() : 0);
        result = 31 * result + (shapeColor != null ? shapeColor.hashCode() : 0);
        result = 31 * result + (glowColor != null ? glowColor.hashCode() : 0);
        result = 31 * result + (preDragX != +0.0f ? Float.floatToIntBits(preDragX) : 0);
        result = 31 * result + (preDragY != +0.0f ? Float.floatToIntBits(preDragY) : 0);
        return result;
    }
}

