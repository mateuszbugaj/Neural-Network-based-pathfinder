import processing.core.PApplet;
import processing.core.PVector;

public class Ray {
    PApplet p;
    PVector pos;
    PVector dir;
    PVector pointPos;
    float dist = 0;
    float angle;
    float raySight = 200;

    float smallestDistToRoad;
    float bestAngle;

    public Ray(PApplet p, PVector pos, float angle,float raySight){
        this.p = p;
        this.pos = pos;
        this.angle = angle;
        this.raySight = raySight;
        dir = PVector.fromAngle(angle);
        pointPos = new PVector();
    }

    public Ray(PApplet p, PVector pos,float raySight){
        this.p = p;
        this.pos = pos;
        smallestDistToRoad = p.width;
        bestAngle = 0;
        this.angle = -180;
        this.raySight = raySight;
        dir = PVector.fromAngle(angle);
        pointPos = new PVector();
    }

    public void show(){
        // Show a ray
        p.stroke(0,p.map(dist,raySight,0,20,255));
        p.strokeWeight(2);

        p.pushMatrix();{
            p.translate(pos.x,pos.y);
            p.line(0,0, dir.x*dist,dir.y*dist);
            dist = 0;
        } p.popMatrix();

    }

    public void updateAngle(float angle){
        dir = PVector.fromAngle(angle-this.angle);
    }

    public void cast(Road road){

        float smallestDsit = p.width;

        for(PVector[] linia:road.lines){
            float x1 = linia[0].x;
            float y1 = linia[0].y;
            float x2 = linia[1].x;
            float y2 = linia[1].y;

            float x3 = pos.x;
            float y3 = pos.y;
            float x4 = pos.x + dir.x;
            float y4 = pos.y + dir.y;

            float den = (x1-x2)*(y3-y4)-(y1-y2)*(x3-x4);
            if(den == 0){
                return;
            }

            float t = ((x1-x3)*(y3-y4)-(y1-y3)*(x3-x4))/den;
            float u = -((x1-x2)*(y1-y3)-(y1-y2)*(x1-x3))/den;
            if(t>0&&t<1&&u>0){

                pointPos.x = x1 + t * (x2-x1);
                pointPos.y = y1 + t * (y2-y1);
                if(pos.dist(pointPos)<smallestDsit){
                    smallestDsit = pos.dist(pointPos);
                }
            }

        }
        if(smallestDsit>raySight){
            dist = raySight;
        } else {
            dist = smallestDsit; // dist to the closest
        }

    }

    public void moveRay(Road road, float rotation){

        smallestDistToRoad = p.width;
        PVector bestDir;
                for(int i = -180;i<180;i++) {
                    updateAngle(p.degrees(rotation)+i);
                    cast(road);
                    if(dist<smallestDistToRoad){
                        smallestDistToRoad = dist;
                        bestDir = dir;
                        bestAngle = bestDir.heading();
                    }
                }

//                p.noFill();
//                p.strokeWeight(1);
//                p.stroke(232, 48, 12);
//                p.ellipse(pos.x,pos.y,smallestDistToRoad*2,smallestDistToRoad*2);
//                    p.pushMatrix();{
//                        p.translate(pos.x,pos.y);
//                        p.stroke(60, 207, 37);
//                        p.line(0,0, p.cos(bestAngle)*smallestDistToRoad,p.sin(bestAngle)*smallestDistToRoad);
//                    } p.popMatrix();
    }
}
