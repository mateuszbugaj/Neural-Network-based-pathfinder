import javafx.beans.binding.ObjectBinding;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

import java.awt.*;
import java.util.ArrayList;

public class Obstacle {
    PApplet p;
    PVector position;
    float rotation;
    float width, height;
    PVector[][] lines;
    Polygon polygon;
    PShape obstacleShape;
    float spaceBetween = 150;
    int index;

    public Obstacle(PApplet p,PVector position, int index) {
        this.p = p;
        this.index = index;
        this.position = position;
        rotation = p.random(2*p.PI);
        this.width = p.random(20,50);
        this.height = p.random(20,50);

        lines = new PVector[4][2];
        polygon = new Polygon();
        obstacleShape = p.createShape();

        lines[0][0] = new PVector(-width/2+position.x,height/2+position.y);
        lines[0][1] = new PVector(-width/2+position.x,-height/2+position.y);
        lines[1][0] = lines[0][1];
        lines[1][1] = new PVector(width/2+position.x,-height/2+position.y);
        lines[2][0] = lines[1][1];
        lines[2][1] = new PVector(width/2+position.x,height/2+position.y);
        lines[3][0] = lines[2][1];
        lines[3][1] = lines[0][0];

        polygon.addPoint((int)lines[0][0].x,(int)lines[0][0].y);
        polygon.addPoint((int)lines[0][1].x,(int)lines[0][1].y);
        polygon.addPoint((int)lines[1][0].x,(int)lines[1][0].y);
        polygon.addPoint((int)lines[1][1].x,(int)lines[1][1].y);
        polygon.addPoint((int)lines[2][0].x,(int)lines[2][0].y);
        polygon.addPoint((int)lines[2][1].x,(int)lines[2][1].y);
        polygon.addPoint((int)lines[3][0].x,(int)lines[3][0].y);
        polygon.addPoint((int)lines[3][1].x,(int)lines[3][1].y);

        obstacleShape.beginShape();{
//            for(int i =0;i<lines.length;i++){
//                obstacleShape.vertex(lines[i][0].x,lines[i][0].y);
//            }
            obstacleShape.vertex(lines[0][0].x,lines[0][0].y);
            obstacleShape.vertex(lines[0][1].x,lines[0][1].y);
            obstacleShape.vertex(lines[1][0].x,lines[1][0].y);
            obstacleShape.vertex(lines[1][1].x,lines[1][1].y);
            obstacleShape.vertex(lines[2][0].x,lines[2][0].y);
            obstacleShape.vertex(lines[2][1].x,lines[2][1].y);
            obstacleShape.vertex(lines[3][0].x,lines[3][0].y);
            obstacleShape.vertex(lines[3][1].x,lines[3][1].y);
        }obstacleShape.endShape();

        //obstacleShape.rotate(rotation);
    }


    public void show(){
        p.pushMatrix();{
            p.strokeWeight(1);
            p.stroke(0);
            obstacleShape.setFill(p.color(181, 140, 29));
            obstacleShape.setStroke(1);
            p.shape(obstacleShape);
            p.noFill();
            //p.ellipse(position.x,position.y,spaceBetween,spaceBetween);
        }p.popMatrix();

    }


}
