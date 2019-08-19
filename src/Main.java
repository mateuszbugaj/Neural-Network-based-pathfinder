import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Main extends PApplet {
    public static void main(String[] args){
        PApplet.main("Main", args);
    }
    public void settings(){
        size(900,900);
    }


    int[] control = {0,0,0,0}; //up, down, right, left // control array containing information about what key is pressed

    // information 'bout look like color, size or shadow tilt
    Look look = new Look(this,new int[]{0,0,0}, new PVector(10,10),new PVector(5,5));
    Look spawnerLook = new Look(this,new int[]{0,150,0}, new PVector(20,20),new PVector(5,5));
    Look playerLook = new Look(this,new int[]{214, 133, 47}, new PVector(10,10),new PVector(5,5));

    Player player;
    Spawner spawner;
    Road road;
    ArrayList<Obstacle> obstacles = new ArrayList<>(); // all obstacles
    PVector[][] allLines = new PVector[obstacles.size()*4][2]; // all lines making all obstacles


    int cycles = 1; // number of cycles program makes for every frame
    boolean routeDisplay = false; // boolean to show route enemies make

    public void setup(){
        rectMode(CENTER);
        imageMode(CENTER);
        player = new Player(this,new PVector(random(width),random(height)),playerLook,5,10);
        spawner = new Spawner(this,new PVector(width*0.4f,height*0.5f), spawnerLook,0,10, look);
        road = new Road(this,player.position.copy(),spawner.position.copy());
        generateNewObstacles(40);
    }


    public void draw(){
        for(int k = 0;k<cycles;k++) { // for certain number of cycles
            background(227, 217, 172);

            //if all enemies are dead and spawnNew is enabled
            // choose new random player's position, clear bullets array, update road, and generate new obstacles
            if(spawner.population.enemies.size()==0 && spawner.spawnNew){
                player.bullets.clear();
                player.position = new PVector(random(width),random(height));
                road = new Road(this,player.position.copy(),spawner.position.copy());
                //player.shootAndStop(15);
                generateNewObstacles(40);
            }

            road.show(); // shows road, creates road's polygon
            spawner.show();

            // show all obstacles
            for(Obstacle obstacle: obstacles){
                obstacle.show();
            }
            spawner.act(); // create new generation if conditions are met

            for (Enemy enemy : spawner.population.enemies) { // for every enemy
                // update frameCountFactor and target position
                enemy.frameCountFactor = cycles;
                enemy.updateTargetPos(player.position);
                enemy.update();

                // for every roadRay
                // update angle and cast using road lines
                for(Ray ray:enemy.roadRays){
                    ray.updateAngle(enemy.rotation);
                    ray.cast(road.lines);
                }

                // for every obstacleRay
                // update angle and cast using all obstacles lines
                for(Ray ray:enemy.obstacleRays){
                    ray.updateAngle(enemy.rotation);
                    ray.cast(allLines);
                }

                // for every obstacle check if position of enemy is in polygon
                // if it is, mark enemy as dead
                for(Obstacle obstacle:obstacles){
                    if(obstacle.polygon.contains(enemy.position.x,enemy.position.y)){
                        enemy.isDead = true;
                    }
                }

                enemy.marchingRay.moveRay(road,enemy.rotation); // move marching ray, cast it using road lines and update rotation
                enemy.checkForRoad(road.polygon); // check if enemy is on road
                enemy.move(); // move enemy
                enemy.show(); // show enemy
                enemy.route(routeDisplay); //calculate route, show only if the option is enabled
                enemy.findClosestBullet(player.bullets); // find closest bullet
                enemy.timeLived++; // update time lived

                // if enemy is dead while being close to the target, move it to the enemiesDead array
                if(enemy.isDead == true && enemy.targetDist<10){
                    spawner.population.enemiesDead.add(enemy);
                }
            }

            // show and move every bullet
            for(Projectile projectile:player.bullets){
                projectile.show();
                projectile.move();
            }

            spawner.population.enemies.removeIf(i -> i.isDead); // delete all enemies that are marked as dead
            player.bullets.removeIf(i -> i.isDead); // remove all bullets

            player.show(); // show player
            player.rotation = player.calculateAngleToTarget(new PVector(mouseX, mouseY)); //update player's rotation
            player.move(control); // move player using controls

            UI(); // show UI

        }
    }

    /////////////////////////////// control
    public void UI(){
        fill(0);
        textSize(15);
        text("Generation: " + spawner.population.generationCount, 0,45);
        if(spawner.population.enemies.size()>0){
            text("Time Lived: " + spawner.population.enemies.get(0).timeLived + " | Time limit : " + spawner.population.enemies.get(0).timeLimitActive, 0,60);
            text("Speed up times: " + spawner.population.enemies.get(0).frameCountFactor+"x", 0,75);
            text("The best now: " + spawner.theBestCurrentEnemy.index, 0,90);
            text("Best of prev. Gen.: " + spawner.theBestEnemyOfGeneration.index, 0,105);

            Enemy bestInDead = spawner.population.enemies.get(0);
            for(Enemy enemy:spawner.population.enemiesDead){
                if (enemy.distTraveled <= bestInDead.distTraveled) {
                    bestInDead = enemy;
                }
            }


            text("Dead enemies size: " + spawner.population.enemiesDead.size() + " | Best(" + bestInDead.index +")", 0,120);

        }


        pushMatrix();{
            translate(0,800);
            text("P [PAUSE]", 0,0);
            text("O [Speed up 20x]", 0,15);
            text("I [No time limit]", 0,30);
            text("M [Save best of generation to file]", 0,45);
            text("Y [Spawn from file without time limit]", 0,60);
            text("T [Stop spawning]", 0,75);
            text("R [Show route]", 0,90);
        } popMatrix();
    }

    public void generateNewObstacles(int amount){
        obstacles.clear(); // clear existing obstacles before generating new

        // for given amount of obstacles
        // create position vector, check if it overlaps with eny existing obstacle and
        // if it does, create new position vector and do it 1000 times or while it still overlaps
        // if created vector doesn't overlap, create obstacle using it as position
        // update allLines with new lines
        for(int i = 0;i<amount;i++){
            PVector c = new PVector(random(width), random(height));
            int tries = 1000;
            while(overlap(c) && tries>0){
                c = new PVector(random(width), random(height));
                tries--;
            }
            if(!overlap(c)){
                obstacles.add(new Obstacle(this,c,i));
            }
        }

        allLines = new PVector[obstacles.size()*4][2];
        int counter = 0;
        for(int i = 0;i<allLines.length;i=i+4){
            allLines[i] = obstacles.get(counter).lines[0];
            allLines[i+1] = obstacles.get(counter).lines[1];
            allLines[i+2] = obstacles.get(counter).lines[2];
            allLines[i+3] = obstacles.get(counter).lines[3];
            counter++;
        }
    }

    public boolean overlap(PVector c){
        // check if proposed position overlap with any existing obstacle
        for(Obstacle obstacle: obstacles){
            if(obstacle.position.dist(c)<obstacle.spaceBetween || player.position.dist(c)<80 || spawner.position.dist(c)<150){
                return true;
            }
        }
        return false;
    }

    public void keyReleased()
    {
        if (key == 'a')
            control[3] = 0;
        if (key == 'd')
            control[2] = 0;
        if (key == 'w')
            control[0] = 0;
        if (key == 's')
            control[1] = 0;
    }

    public void keyPressed()
    {
        if (key == 'a')
            control[3] = 1;
        if (key == 'd')
            control[2] = 1;
        if (key == 'w')
            control[0] = 1;
        if (key == 's')
            control[1] = 1;
    }

    public void keyTyped(){
        if(key == 'p'){
            if(looping){
                fill(0);
                textSize(30);
                text("PAUSE", 0,30);
                textSize(12);
                noLoop();

            } else {
                loop();
            }
        }

        if(key == 'o'){
            if(cycles==20){
                cycles = 1;
            } else {
                cycles = 20;
            }
        }

        if(key == 'i'){
            if(spawner.population.enemies.get(0).timeLimitActive){
                for(Enemy enemy:spawner.population.enemies){
                    enemy.timeLimitActive = false;
                }
            } else {
                for(Enemy enemy:spawner.population.enemies){
                    enemy.timeLimitActive = true;
                }
            }
        }

        if(key == 'm'){
            spawner.theBestEnemyOfGeneration.brain.writeToFile(); // write to file the best enemy of generation
        }

        if(key == 'y'){
            spawner.spawnSaved(); // spawn enemy with brain from file and disable it's time limit
        }

        if(key == 't'){
            if(spawner.spawnNew == true){
                spawner.spawnNew = false;
            } else{
                spawner.spawnNew = true;
            }
        }

        if(key == 'r'){
            if(routeDisplay == true){
                routeDisplay = false;
            } else{
                routeDisplay = true;
            }
        }

    }

    public void mousePressed(){
        if(mouseButton == LEFT){
            player.shoot();
        } else if (mouseButton == RIGHT){
            for(Enemy enemy:spawner.population.enemies){
                if(mouseX <= enemy.position.x+enemy.look.dimensions.x &&
                        mouseX >= enemy.position.x-enemy.look.dimensions.x &&
                        mouseY >= enemy.position.y-enemy.look.dimensions.y &&
                        mouseY <= enemy.position.y+enemy.look.dimensions.y){
                    for(Enemy enemy2:spawner.population.enemies) {
                        enemy2.showInfoBoolean = false;
                        }
                    enemy.showInfoBoolean = true;
                    break;
                    }
                }
            }
        }
}



