import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Population {
    PApplet p;
    ArrayList<Enemy> enemies;
    ArrayList<Enemy> enemiesDead;
    PVector initialPosition;
    Look enemyLook;
    int populationNumber;
    int generationCount = 0;

    public Population(PApplet p, PVector initialPosition, Look enemyLook, int populationNumber) {
        this.p = p;
        this.initialPosition = initialPosition;
        this.enemyLook = enemyLook;
        this.populationNumber = populationNumber;
        enemies = new ArrayList<>();
        enemiesDead = new ArrayList<>();
    }

    public void newPopulation(){
        for(int i = 0;i<populationNumber;i++){
            enemies.add(new Enemy(p,initialPosition.copy(),enemyLook,2,1));
            enemies.get(i).index = i;
        }
        generationCount++;
    }

    public void newPopulation(Enemy enemyToClone){
        for(int i = 0;i<populationNumber;i++){
            enemies.add(new Enemy(p,initialPosition.copy(),enemyLook,2,1,enemyToClone.brain.copy()));
            enemies.get(i).index = i;
            enemies.get(i).brain.mutate(0.08);
        }
        generationCount++;
    }


}
