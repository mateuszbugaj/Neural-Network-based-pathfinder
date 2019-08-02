import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.beans.PropertyVetoException;

public class Character {
    // Class acting as a base containing basic variables and functions for
    // many other beings in app

    PApplet p;
    PVector position; // position of the character
    float rotation = 0; // rotation of the character
    Look look; // basic look features
    float speed;
    float health;
    boolean isDead = false;

    public Character(PApplet p, PVector position, Look look, float speed, float health) {
        this.p = p;
        this.position = position;
        this.look = look;
        this.speed = speed;
        this.health = health;
    }


    public void show(){
        p.pushMatrix();{
            // show main figure
            p.translate(position.x,position.y);
            p.rotate(rotation);
            p.noStroke();
            p.fill(look.color[0], look.color[1], look.color[2]);
            p.rect(0,0,look.dimensions.x,look.dimensions.y);
        } p.popMatrix();
    }


    public void move(){
        position.x += p.cos(rotation) * speed;
        position.y += p.sin(rotation) * speed;

        inRange(); // if outside the range mark as dead
    }

    public void inRange(){
        if(position.x>-p.width&&position.x<2*p.width&&position.y>-p.height&&position.y<2*p.height){
        } else {
            isDead = true;
        }

    }

    public float calculateAngleToTarget(PVector targetPos){
        return p.atan2(position.y - targetPos.y, position.x - targetPos.x) + p.PI;
    }


}
