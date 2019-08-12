import basicneuralnetwork.NeuralNetwork;
import processing.core.PApplet;
import processing.core.PVector;


public class Spawner extends Character{
    Population population;
    Enemy theBestCurrentEnemy;
    Enemy theBestEnemyOfGeneration;
    Look enemyLook;
    boolean spawnNew = true;


    public Spawner(PApplet p, PVector position, Look look, float speed, float health,Look enemyLook) {
        super(p, position, look, speed, health);
        this.enemyLook = enemyLook;
        population = new Population(p,position.copy(),enemyLook,80);
        population.newPopulation();
        theBestCurrentEnemy = population.enemies.get(0);
        theBestEnemyOfGeneration = population.enemies.get(0);
    }

    public void act(){
        if(population.enemies.size()==0){
            if(spawnNew) {
                findBestFromDead();
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

    private void findBestFromDead(){

        // if enemies dead is empty (non of the dead enemies was close enough to be added while dead
        // take the best current and add it to the array
        if(population.enemiesDead.size()==0){
            population.enemiesDead.add(theBestCurrentEnemy);
        }


        // calculate shortest and longest dist from enemies that reached player
        float shortestTraveledDist = Integer.MAX_VALUE, longestTraveledDist = 0;
        for(Enemy enemy:population.enemiesDead){
            if(enemy.distTraveled<shortestTraveledDist){
                shortestTraveledDist = enemy.distTraveled;
            }
            if(enemy.distTraveled>longestTraveledDist){
                longestTraveledDist = enemy.distTraveled;
            }
        }

        // calculate route compared to others and general store for each enemy
        for(Enemy enemy:population.enemiesDead){
            enemy.routeComparedToOthers = ((longestTraveledDist-enemy.distTraveled)/(longestTraveledDist-shortestTraveledDist))*100;
            enemy.generalScore = (enemy.routeComparedToOthers * 1 + enemy.percentOfDistOnRoad * 3)/(1+3); // "3" after percent of dist on road is the weight
        }

        //set the best enemy of generation as the first enemy in enemiesDead array
        theBestEnemyOfGeneration = population.enemiesDead.get(0);

        // find enemy with best general store and set it as the best enemy of generation
        for(Enemy enemy:population.enemiesDead){
            if(enemy.generalScore>theBestEnemyOfGeneration.generalScore){
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
