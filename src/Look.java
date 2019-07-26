import processing.core.PApplet;
import processing.core.PVector;

public class Look {

    // only contain basic appearance features

    PApplet p;
    int[] color; // [r,g,b]
    PVector dimensions; // (x,y)
    public PVector shadowShift; // how much shift shadow relative to original figure

    public Look(PApplet p, int[] color, PVector dimentions, PVector shadowShift) {
        this.p = p;
        this.color = color;
        this.dimensions = dimentions;
        this.shadowShift = shadowShift;
    }

    public void changeColor(int[] newColor){
        this.color = newColor;
    }
}