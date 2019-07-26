import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Player extends Character {
    ArrayList<Projectile> bullets;
    Look bulletLook = new Look(p,new int[]{190, 206, 232},new PVector(5,5),new PVector(0,0));


    public Player(PApplet p, PVector position, Look look, float speed, float health) {
        super(p, position, look, speed, health);
        bullets = new ArrayList<>();
    }


    public void move(int[] control){
        // move using controls
        position.x += (control[2] - control[3]) * speed;
        position.y += (control[0] - control[1]) * speed * (-1);
    }

    public void shoot(){
        bullets.add(new Projectile(p,position.copy(),bulletLook,5,1));
        bullets.get(bullets.size()-1).rotation = this.rotation;
    }

    public void shootAndStop(int i){
        int radius = 200;
        for(int k = 0;k<i;k++) {
            bullets.add(new Projectile(p, position.copy(), bulletLook, 5, 1));
            bullets.get(bullets.size() - 1).speed = 0;
            bullets.get(bullets.size() - 1).position = new PVector(p.random(position.x-radius,position.x+radius),p.random(position.y-radius,position.y+radius));
            bullets.get(bullets.size() - 1).rotation = rotation;
        }
    }
}
