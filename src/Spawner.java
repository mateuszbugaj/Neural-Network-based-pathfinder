import basicneuralnetwork.NeuralNetwork;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Spawner extends Character{
    Population population;
    Enemy theBestCurrentEnemy;
    Enemy theBestEnemyOfGeneration;
    Look enemyLook;
    boolean spawnNew = true;


    public Spawner(PApplet p, PVector position, Look look, float speed, float health,Look enemyLook) {
        super(p, position, look, speed, health);
        this.enemyLook = enemyLook;
        population = new Population(p,position.copy(),enemyLook,50);
        population.newPopulation();
        theBestCurrentEnemy = population.enemies.get(0);
        theBestEnemyOfGeneration = population.enemies.get(0);
    }

    public void act(){
        if(population.enemies.size()==0){
            findBestFromDead();
            if(spawnNew) {
                population.enemiesDead.clear();
                population.newPopulation(theBestEnemyOfGeneration);
                theBestCurrentEnemy = population.enemies.get(0);
            }
        } else if(population.generationCount>0){
            for(Enemy enemy:population.enemies){
                if(enemy.score>=theBestCurrentEnemy.score){
                    theBestCurrentEnemy = enemy;
                    enemy.isBest = true;
                } else {
                    enemy.isBest = false;
                }
            }
        }
    }

    public void findBestFromDead(){

        // if enemies dead is empty (non of the dead enemies was close enough to be added while dead
        // take the best current and add it to the array
        if(population.enemiesDead.size()==0){
            population.enemiesDead.add(theBestCurrentEnemy);
        }

        theBestEnemyOfGeneration = population.enemiesDead.get(0); //

        // find which one of dead enemies that was very close to the player traveled less distance
        for(Enemy enemy:population.enemiesDead){
            if (enemy.distTraveled <= theBestEnemyOfGeneration.distTraveled) {
                theBestEnemyOfGeneration = enemy;
            }
        }
    }

    public void spawnSaved(){
        // add new enemy to array list but with brain loaded from file
        Enemy enemyToSpawn = new Enemy(p,position.copy(),enemyLook,2,1);
        NeuralNetwork newBrain = NeuralNetwork.readFromFile();
        enemyToSpawn.brain = newBrain;
        enemyToSpawn.timeLimitActive = false;
        population.enemies.add(enemyToSpawn);
    }


}
