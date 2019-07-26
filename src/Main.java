import processing.core.PApplet;
import processing.core.PVector;

public class Main extends PApplet {
    public static void main(String[] args){
        PApplet.main("Main", args);
    }
    public void settings(){
        size(900,900);
    }


    int[] control = {0,0,0,0}; //up, down, right, left // control array containing information about what key is pressed
    Look look = new Look(this,new int[]{0,0,0}, new PVector(10,10),new PVector(5,5));
    Look spawnerLook = new Look(this,new int[]{0,150,0}, new PVector(20,20),new PVector(5,5));
    Look playerLook = new Look(this,new int[]{214, 133, 47}, new PVector(10,10),new PVector(5,5));
    Player player;
    Spawner spawner;
    Road road;
    int cycles = 1;
    boolean routeDisplay = false;

    public void setup(){
        rectMode(CENTER);
        imageMode(CENTER);
        player = new Player(this,new PVector(random(width),random(height)),playerLook,5,10);
        spawner = new Spawner(this,new PVector(width*0.4f,height*0.5f), spawnerLook,0,10, look);
        road = new Road(this,player.position.copy(),spawner.position.copy());
    }

    public void draw(){
        for(int k = 0;k<cycles;k++) {
            background(227, 217, 172);

            if(spawner.population.enemies.size()==0 && spawner.spawnNew){
                player.bullets.clear();
                player.position = new PVector(random(width),random(height));
                road = new Road(this,player.position.copy(),spawner.position.copy());
                //player.shootAndStop(15);
            }

            road.show();
            spawner.show();
            spawner.act();



            for (Enemy enemy : spawner.population.enemies) {
                enemy.frameCountFactor = cycles;
                enemy.updateTargetPos(player.position);

                for(Ray ray:enemy.rays){
                    ray.updateAngle(enemy.rotation);
                    ray.cast(road);
                    //ray.show();
                }

                enemy.checkForRoad(road.polygon);
                enemy.update();
                enemy.move();
                enemy.show();
                enemy.showRoute(routeDisplay); // show route only if the option is enabled
                enemy.findClosestBullet(player.bullets);
                enemy.timeLived++;

                if(enemy.isDead == true && enemy.targetDist<10){
                    spawner.population.enemiesDead.add(enemy);
                }
            }

            // show sensors but only for one enemy
            if (spawner.population.enemies.size() > 0 && routeDisplay) {
                spawner.population.enemies.get(0).showSensors();
                for(Ray ray:spawner.population.enemies.get(0).rays){
                    ray.show();
                }
                spawner.population.enemies.get(0).showInfo();
            }

            for(Projectile projectile:player.bullets){
                projectile.show();
                projectile.move();
            }

            spawner.population.enemies.removeIf(i -> i.isDead);
            player.bullets.removeIf(i -> i.isDead);

            player.show();
            player.rotation = player.calculateAngleToTarget(new PVector(mouseX, mouseY));
            player.move(control);

            UI();

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
        player.shoot();
    }


}
