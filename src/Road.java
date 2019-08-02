import processing.core.PApplet;
import processing.core.PVector;

import java.awt.*;

import static processing.core.PConstants.CLOSE;

public class Road {
    PApplet p;
    PVector playerPos, spawnerPos;
    PVector[][] lines;
    PVector[][] centerLines;
    PVector point1A, point1B, point2A, point2B,point3A,point3B;
    float roadWidth = 80;
    float angle;
    Polygon polygon;


    public Road(PApplet p,PVector playerPos, PVector spawnerPos){
        this.p = p;
        playerPos = new PVector(playerPos.x,playerPos.y);
        spawnerPos = new PVector(spawnerPos.x,spawnerPos.y);
        int ori =(int) p.random(-1,1);
        if(ori == 0) ori = 1;
        float dist = p.random(50,150);

        if((playerPos.x<p.width/2 && playerPos.y<p.height/2) || (playerPos.x>p.width/2 && playerPos.y>p.height/2)){
            point1A = new PVector(roadWidth/2,0);
            point1B = new PVector(-roadWidth/2, 0);
            point2A = new PVector(p.width+roadWidth/2,p.height);
            point2B = new PVector(p.width-roadWidth/2,p.height);
            point3A = new PVector(0,0);
            point3B = new PVector(p.width,p.height);
        }
        if((playerPos.x<p.width/2 && playerPos.y>p.height/2) || (playerPos.x>p.width/2 && playerPos.y<p.height/2)){
            point1A = new PVector(p.width-roadWidth/2,0);
            point1B = new PVector(p.width+roadWidth/2, 0);
            point2A = new PVector(-roadWidth/2,p.height);
            point2B = new PVector(roadWidth/2,p.height);
            point3A = new PVector(p.width,0);
            point3B = new PVector(0,p.height);
        }

        lines = new PVector[2][2];
        lines[0][0] = point1A;
        lines[0][1] = point2A;
        lines[1][0] = point1B;
        lines[1][1] = point2B;

        centerLines = new PVector[1][2];
        centerLines[0][0] = point3A;
        centerLines[0][1] = point3B;
    }

    public void show(){
        p.fill(189, 174, 79);
        p.stroke(0);
        p.strokeWeight(2);

        polygon = new Polygon();
        polygon.addPoint((int)point1A.x,(int)point1A.y);
        polygon.addPoint((int)point1B.x,(int)point1B.y);
        polygon.addPoint((int)point2B.x,(int)point2B.y);
        polygon.addPoint((int)point2A.x,(int)point2A.y);



        p.beginShape();
            for(int i = 0;i<polygon.npoints;i++){
                p.vertex(polygon.xpoints[i],polygon.ypoints[i]);
            }
        p.endShape(CLOSE);

        p.stroke(0,100);
        p.strokeWeight(1);
        p.line(point3A.x,point3A.y,point3B.x,point3B.y);

    }
}
