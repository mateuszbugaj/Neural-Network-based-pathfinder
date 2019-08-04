import basicneuralnetwork.NeuralNetwork;
import basicneuralnetwork.activationfunctions.ActivationFunction;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.*;
import java.util.ArrayList;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CORNER;

public class Enemy extends Character{
    int index;

    NeuralNetwork brain;
    double score; // score for genetic algorithm
    float distTraveled = 0; // total dist traveled from spawn to death
    float distTraveledOnRoad = 0; // dist traveled on road in total
    float percentOfDistOnRoad = 0;
    float routeComparedToOthers; // given in spawner to give how good the dist was compared to others
    float generalScore; // given in spawner and it's based on percentOfDistOnRoad and routeComparedToOthers
    ArrayList<PVector> rounte = new ArrayList<>(); // route counted outside the road
    ArrayList<PVector> rounteOnRoad = new ArrayList<>(); // route counted on the road
    ArrayList<PVector> totalRoute = new ArrayList<>(); // total route
    int creationTime; // time measured in frames from beginning of the app to creation of enemy
    int timeLimit = 600; // limits how many frames enemy can live
    int timeLived; // how many frames did enemy lived
    boolean timeLimitActive = true; // is it gonna die after some time or not
    boolean isBest = false; // is it currently the best (will change color)
    int frameCountFactor = 1; // what is current time count factor
    boolean showInfoBoolean = false; // show information about enemy

    PVector targetPos;
    float targetAngle;
    float closestDist = p.width;
    float targetDist;
    float[] RGBPosition = new float[3];

    PVector closestBulletPos;
    float closestBulletAngle;
    float closestBulletDist= 0;

    float turningAcceleration;
    float turningSpeed;
    float maxAcceleration = 8;
    float maxTurningSpeed = 15;

//    float acceleration;
//    float maxSpeedAcceleration = 0.01f;
//    float maxSpeed = 2.5f;

    Ray marchingRay;
    Ray[] roadRays = new Ray[2];
    Ray[] obstacleRays = new Ray[13];
    boolean isOnRoad = false;
    float[] RGBRoadPosition = new float[3];
    float roadAngle;
    float[] rayReadings = new float[roadRays.length];
    float [] obstacleRayReadings = new float[obstacleRays.length];


    int n = 0;
    PImage sprite;


    public Enemy(PApplet p, PVector position, Look look, float speed, float health) {
        super(p, position, look, speed, health);
        sprite = p.loadImage("Sprite.png");
        brain = new NeuralNetwork(RGBPosition.length+RGBRoadPosition.length+rayReadings.length + obstacleRayReadings.length+3,2,20,2);
        brain.setActivationFunction(ActivationFunction.SIGMOID);
        creationTime = p.frameCount;
        createRays();


    }

    public Enemy(PApplet p, PVector position, Look look, float speed, float health, NeuralNetwork brain) {
        super(p, position, look, speed, health);
        this.brain = brain;
        sprite = p.loadImage("Sprite.png");
        creationTime = p.frameCount;
        createRays();

    }

    public void update(){
        if(showInfoBoolean){
            showInfo();
            showSensors();
        }

        // check if enemy is the best and change it's color accordingly
        if(isBest == true){
            look.changeColor(new int[]{230,67,34});
        } else if (isBest == false){
            look.changeColor(new int[]{163,160,59});
        }

        if(health<=0){
            this.isDead = true;
        }

        // if timeLived is bigger than timeLimit then mark as dead
        if(timeLived>timeLimit && timeLimitActive){
            this.isDead = true;
        }

        //calculate dist to the target
        targetDist = position.dist(targetPos);
        if(targetDist<10){
            this.isDead = true;
        }

        // calculate distTraveled
        if(timeLived%5==0){
            if(isOnRoad){
                rounteOnRoad.add(position.copy());
                totalRoute.add(position.copy());
            } else {
                rounte.add(position.copy());
                totalRoute.add(position.copy());
            }
        }


        // calculate score when dist is lower than ever
        if(targetDist<closestDist){
            closestDist = targetDist;
            score = (1/targetDist)*1000;
        }


        // calculate rotations and targetAngle
        n  = (int) (rotation/(p.PI*2));
        rotation-=n*p.PI*2;
        targetAngle = calculateAngleToTarget(targetPos) - rotation;
        if(targetAngle<0){
            targetAngle = (2*p.PI) + targetAngle;
        } else if(targetAngle>p.PI*2){
            targetAngle = targetAngle - (2*p.PI);
        }

        // calculate roadAngle
        roadAngle = marchingRay.bestAngle - rotation;
        if(roadAngle<0){
            roadAngle = (2*p.PI) + roadAngle;
        } else if(roadAngle>p.PI*2){
            roadAngle = roadAngle - (2*p.PI);
        }

        sensors();
        brainActivity();

        //rotation =p.PI/4;
        //speed = 0.5f;
    }

    public void brainActivity(){
        double inputs[] = new double[brain.getInputNodes()];

        for(int i = 0;i<3;i++){
            inputs[i] = RGBPosition[i]/255;
        }

        for(int i = 3;i<6;i++){
            inputs[i] = RGBPosition[i-3]/255;
        }

        for(int i = 6;i<6+rayReadings.length;i++){
            inputs[i] = rayReadings[i-6];
        }

        for(int i = 6+rayReadings.length;i<6+rayReadings.length+obstacleRayReadings.length;i++){
            inputs[i] = obstacleRayReadings[i-6-rayReadings.length];
        }
        //inputs[inputs.length-4] = turningSpeed;
        inputs[inputs.length-3] = marchingRay.smallestDistToRoad/p.width;
        inputs[inputs.length-2] = targetDist/p.width;
        inputs[inputs.length-1] = isOnRoad ? 1 : 0;
        double[] output = brain.guess(inputs);


        // rotation
        turningAcceleration = p.map((float) output[0],0,1,-maxAcceleration,maxAcceleration);
        if(turningSpeed<maxTurningSpeed && turningSpeed>-maxTurningSpeed){
            turningSpeed+=turningAcceleration;
        } else if(turningSpeed>maxTurningSpeed && turningAcceleration<0){
            turningSpeed+=turningAcceleration;
        } else if(turningSpeed<-maxTurningSpeed && turningAcceleration>0){
            turningSpeed+=turningAcceleration;
        }
        rotation+=p.radians(turningSpeed);


        // speed
//        acceleration = p.map((float) output[1],0,1,-maxSpeedAcceleration,maxSpeedAcceleration);
//        if(speed<maxSpeed && speed>-maxSpeed){
//            speed+=acceleration;
//        } else if(speed>maxSpeed && acceleration<0){
//            speed+=acceleration;
//        } else if(speed<-maxSpeed && acceleration>0){
//            speed+=acceleration;
//        }
    }

    public void sensors(){
        RGBPosition = HtoRGB(targetAngle);
        RGBRoadPosition = HtoRGB(roadAngle);

        // roadRays
        // collect readings from the roadRays and map them and store them in array
        //the point which is the most far away is 0 and the closest is 1
        for(int i = 0; i< roadRays.length; i++){
            rayReadings[i] = p.map(roadRays[i].dist, roadRays[i].raySight,0,0,1);
        }

        for(int i = 0; i< obstacleRays.length; i++){
            obstacleRayReadings[i] = p.map(obstacleRays[i].dist, obstacleRays[i].raySight,0,0,1);
        }

    }

    public float[] HtoRGB(float Hue){
        // turn target angle into RGB color
        float[] RGBResoult;
        float S = 1; // saturation 0-1
        float V = 1; // value 0-1
        float C, X, m;
        float RPrim, GPrim, BPrim;

        float H =p.degrees(Hue);

        C = V * S;
        X = C * (1-p.abs((H/60)%2-1));
        m = V-C;
        RPrim = C;
        GPrim = X;
        BPrim = 0;

        if(H>=0 && H<60){
            RPrim = C;
            GPrim = X;
            BPrim = 0;
        } else if(H>=60 && H<120){
            RPrim = X;
            GPrim = C;
            BPrim = 0;
        } else if(H>=120 && H<180){
            RPrim = 0;
            GPrim = C;
            BPrim = X;
        } else if(H>=180 && H<240){
            RPrim = 0;
            GPrim = X;
            BPrim = C;
        } else if(H>=240 && H<300){
            RPrim = X;
            GPrim = 0;
            BPrim = C;
        } else if(H>=300 && H<360){
            RPrim = C;
            GPrim = 0;
            BPrim = X;
        }
        RGBResoult = new float[]{(RPrim+m)*255,(GPrim+m)*255,(BPrim+m)*255};
        return RGBResoult;
    }


    void createRays(){
        roadRays[0] = new Ray(p,position,p.radians(40),200);
        roadRays[1] = new Ray(p,position,p.radians(-40),200);

        for(int i = 0;i<obstacleRays.length;i++){
            obstacleRays[i] = new Ray(p,position,p.radians(-60+i*10),200);
        }

        marchingRay = new Ray(p,position,p.width);
    }



    public void checkForRoad(Polygon road){
        isOnRoad = road.contains(position.x,position.y);
    }


    public void updateTargetPos(PVector targetPos){
        this.targetPos = targetPos;
    }

    public void showInfo(){
        // everything about enemy

        p.fill(0);

        // global coordinate system
        p.pushMatrix();
        {
            p.translate(position.x,position.y);
            p.stroke(31, 10, 168);
            p.strokeWeight(1);
            p.line(-100, 0, 100, 0);
            p.line(0, -100, 0, 100);
            p.text("X", 90, 0);
        } p.popMatrix();

        // individual coordinate system
        p.pushMatrix();
        {
            p.translate(position.x,position.y);
            p.rotate(rotation);
            p.stroke(70, 151, 163);
            p.strokeWeight(1);
            p.line(-50, 0, 50, 0);
            p.line(0, -50, 0, 50);
            p.text("X", 40, 0);
        } p.popMatrix();

        // line to the target calculated from the angle of rotation
        p.pushMatrix();
        {
            p.translate(position.x,position.y);
            p.rotate(rotation);
            p.stroke(0,50);
            p.line(0,0,p.cos(targetAngle)*targetDist,p.sin(targetAngle)*targetDist);
        } p.popMatrix();

        // line to the closest bullet calculated from the angle of rotation
            p.pushMatrix();
            {
                p.translate(position.x, position.y);
                p.rotate(rotation);
                p.stroke(0);
                p.line(0, 0, p.cos(closestBulletAngle) * closestBulletDist, p.sin(closestBulletAngle) * closestBulletDist);
            }
            p.popMatrix();

        // shortest line to the road
        p.pushMatrix();
        {
            p.translate(position.x, position.y);
            p.strokeWeight(2);
            p.stroke(207, 37, 167);
            p.line(0,0, p.cos(marchingRay.bestAngle)*marchingRay.smallestDistToRoad,p.sin(marchingRay.bestAngle)*marchingRay.smallestDistToRoad);
        }
        p.popMatrix();


        p.text("INFO",15,230);
        p.rectMode(CORNER);
        p.noFill();
        p.stroke(107, 139, 143);
        p.rect(10,280,170,200);
        p.rectMode(CENTER);

        // show obstacleRays
        p.pushMatrix();
        {
            p.translate(20,240);
            for (int i = 0; i < obstacleRays.length; i++) {
                p.fill(p.map(obstacleRays[obstacleRays.length - i - 1].dist, 0, obstacleRays[obstacleRays.length - i - 1].raySight, 0, 255));
                p.rect(15+(150/obstacleRays.length)*i,15,(150/obstacleRays.length),30);
            }
        } p.popMatrix();

        p.line(180,p.map(position.y,0,p.height+p.height/2,280,280+200),position.x,position.y);

        // rotations info
        p.pushMatrix();
        {
            p.fill(0);
            p.textSize(10);
            p.translate(20,300);
            p.text("ID: "+ index,0,0);
            p.text("Rotation: " + (int)p.degrees(rotation),0,10);
            p.text("Target Angle: " +(int)p.degrees(targetAngle),0,20);
            p.text("Dist traveled: "+(int) distTraveled,0,30);
            p.text("Dist traveled on road: "+ (int)distTraveledOnRoad,0,40);
            p.text("% of dist traveled on road: "+(int) percentOfDistOnRoad+"%",0,50);
            p.text("Target RGB pos: ",0,60);
            p.text("R: " +(int) RGBPosition[0],10,70);
            p.text("G: " +(int) RGBPosition[1],10,80);
            p.text("B: " +(int) RGBPosition[2],10,90);
            p.text("Road RGB pos: ",0,100);
            p.text("R: " +(int) RGBRoadPosition[0],10,110);
            p.text("G: " +(int) RGBRoadPosition[1],10,120);
            p.text("B: " +(int) RGBRoadPosition[2],10,130);
            p.text("Is on road: "+ isOnRoad,0,140);
            p.text("Dist to target: "+(int) targetDist,0,150);
            p.text("Dist to road: "+(int) marchingRay.smallestDistToRoad,0,160);
            //p.text("Acceleration: "+acceleration,0,170);
            p.text("Speed: "+speed,0,170);
        } p.popMatrix();


    }

    public void route(boolean display){
        p.strokeWeight(2);
        distTraveled = 0; // set dist traveled to 0 before each computing
        distTraveledOnRoad = 0;
        for(int i =0;i<totalRoute.size();i++){ // for every point in total route
            if(i<totalRoute.size()-1){ // if there is one point more
                if(rounteOnRoad.contains(totalRoute.get(i))){ // if point is present in routeOnRoad
                    p.stroke(166, 23, 149); // purple
                    distTraveled+=totalRoute.get(i).dist(totalRoute.get(i+1))/4; // add dist between points to the dist traveled
                    distTraveledOnRoad+=totalRoute.get(i).dist(totalRoute.get(i+1))/4; // add dist between points to the dist traveled on road
                } else {
                    p.stroke(9, 111, 156); // blue
                    distTraveled+=totalRoute.get(i).dist(totalRoute.get(i+1)); // add dist between points to the dist traveled
                }
                if(display) p.line(totalRoute.get(i).x,totalRoute.get(i).y,totalRoute.get(i+1).x,totalRoute.get(i+1).y); // display route on plane
            }
        }
        percentOfDistOnRoad = (distTraveledOnRoad/distTraveled)*100;
    }

    public void showSensors(){
        //show work of sensors

        p.strokeWeight(1);
        p.stroke(0,15);

        p.pushMatrix();{
            p.translate(position.x,position.y);
            p.rotate(rotation);

            p.noStroke();

            for(int i = 0;i<360;i++){
                p.pushMatrix();
                p.rotate(p.radians(i));
                float[] RGBColor = HtoRGB(p.radians(i));
                p.fill(RGBColor[0],RGBColor[1], RGBColor[2],25);
                p.ellipse(50,0,5,5); // draw ellipse in distance of 400 px from the center
                p.popMatrix();
            }

            //for road
            p.pushMatrix();{
                p.rotate(roadAngle);
                p.stroke(0);
                p.strokeWeight(2);
                p.fill(RGBRoadPosition[0],RGBRoadPosition[1],RGBRoadPosition[2]);
                p.rect(50,0,8,8);

            } p.popMatrix();

            // for player
            p.pushMatrix();{
                p.rotate(targetAngle);
                p.stroke(0);
                p.strokeWeight(2);
                p.fill(RGBPosition[0],RGBPosition[1],RGBPosition[2]);
                p.rect(50,0,12,12);

            } p.popMatrix();

            p.noFill();
            p.stroke(0,15);
            p.ellipse(0,0, roadRays[0].raySight*2, roadRays[0].raySight*2);


        }p.popMatrix();

        // roadRays
        for(Ray ray: roadRays){
            ray.show();
        }

        for(Ray ray: obstacleRays){
            ray.show();
        }
    }

    public void findClosestBullet(ArrayList<Projectile> bullets){
        // Find the closest projectile, it's position and angle.

        closestBulletDist = p.width;
        for(Projectile projectile:bullets){
            if(projectile.position.dist(position)<closestBulletDist){
                closestBulletDist = projectile.position.dist(position);
                closestBulletPos = projectile.position.copy();
                closestBulletAngle = calculateAngleToTarget(closestBulletPos) - rotation;
            }
            if(projectile.position.dist(position)<10){
                projectile.isDead = true;
                health-=1;
            }
        }

        if(bullets.size()==0){
            closestBulletDist = 0;
        }
    }



}


//import basicneuralnetwork.NeuralNetwork;
//        import processing.core.PApplet;
//        import processing.core.PImage;
//        import processing.core.PVector;
//
//        import java.awt.*;
//        import java.util.ArrayList;
//
//public class Enemy extends Character{
//    int index;
//
//    NeuralNetwork brain;
//    double score; // score for genetic algorithm
//    float distTraveled = 0; // total dist traveled from spawn to death
//    ArrayList<PVector> rounte = new ArrayList<>(); // route counted outside the road
//    ArrayList<PVector> rounteOnRoad = new ArrayList<>(); // route counted on the road
//    ArrayList<PVector> totalRoute = new ArrayList<>(); // total route
//    int creationTime; // time measured in frames from beginning of the app to creation of enemy
//    int timeLimit = 600; // limits how many frames enemy can live
//    int timeLived; // how many frames did enemy lived
//    boolean timeLimitActive = true;
//    boolean isBest = false;
//    int frameCountFactor = 1;
//    PVector targetPos;
//    float targetAngle;
//    float closestDist = p.width;
//    float targetDist;
//
//    PVector closestBulletPos;
//    float closestBulletAngle;
//    float closestBulletDist= 0;
//
//    float turningAcceleration;
//    float turningSpeed;
//    float maxTurningSpeed = 15;
//
//    Ray[] roadRays = new Ray[4];
//    boolean isOnRoad = true;
//
//    //double[] readingsProjectile = new double[8];
//    float angleStep = p.radians(10);
//    float[][] sensors = new float[12][2];
//    float[] rayReadings = new float[roadRays.length];
//    double[] readingsPlayer = new double[sensors.length];
//
//    Ray marchingRay;
//    float distToRoad;
//
//    int n = 0;
//    PImage sprite;
//
//    public Enemy(PApplet p, PVector position, Look look, float speed, float health) {
//        super(p, position, look, speed, health);
//        sprite = p.loadImage("Sprite.png");
//        brain = new NeuralNetwork(sensors.length+rayReadings.length+2,2,8,1);
//        creationTime = p.frameCount;
//        createRays();
//        createSensors();
//
//    }
//
//    public Enemy(PApplet p, PVector position, Look look, float speed, float health, NeuralNetwork brain) {
//        super(p, position, look, speed, health);
//        this.brain = brain;
//        sprite = p.loadImage("Sprite.png");
//        creationTime = p.frameCount;
//        createRays();
//        createSensors();
//    }
//
//    public void update(){
//
//        // check if enemy is the best and change it's color accordingly
//        if(isBest == true){
//            look.changeColor(new int[]{230,67,34});
//        } else if (isBest == false){
//            look.changeColor(new int[]{163,160,59});
//        }
//
//        if(health<=0){
//            this.isDead = true;
//        }
//
//        // if timeLived is bigger than timeLimit then mark as dead
//        if(timeLived>timeLimit && timeLimitActive){
//            this.isDead = true;
//        }
//
//        //calculate dist to the target
//        targetDist = position.dist(targetPos);
//        if(targetDist<10){
//            this.isDead = true;
//        }
//
//        // calculate distTraveled
//        if(timeLived%5==0){
//            if(isOnRoad){
//                rounteOnRoad.add(position.copy());
//                totalRoute.add(position.copy());
//            } else {
//                rounte.add(position.copy());
//                totalRoute.add(position.copy());
//            }
//        }
//
//
//        // calculate score when dist is lower than ever
//        if(targetDist<closestDist){
//            closestDist = targetDist;
//            score = (1/targetDist)*1000;
//        }
//
//
//        // calculate rotations and targetAngle to be from range of -PI to PI
//        n  = (int) (rotation/(p.PI*2));
//        rotation-=n*p.PI*2;
//        targetAngle = calculateAngleToTarget(targetPos) - rotation;
//
//        if(targetAngle<-p.PI){
//            targetAngle = (2*p.PI) + targetAngle;
//        } else if(targetAngle>p.PI){
//            targetAngle = targetAngle - (2*p.PI);
//        }
//
//        sensors();
//        brainActivity();
//
//        rotation = 0;
//        speed = 0.1f;
//    }
//
//    public void brainActivity(){
//        double inputs[] = new double[brain.getInputNodes()];
//
//        for(int i = 0;i<readingsPlayer.length;i++){
//            inputs[i] = readingsPlayer[i];
//        }
//
//        for(int i = readingsPlayer.length;i<readingsPlayer.length+rayReadings.length;i++){
//            inputs[i] = rayReadings[i-readingsPlayer.length];
//        }
//
//        inputs[inputs.length-2] = targetDist/p.width;
//        inputs[inputs.length-1] = isOnRoad ? 1 : 0;
////        for(int i = 18;i<18+8;i++){
////            inputs[i] = readingsProjectile[i-18];
////        }
//        double output;
//        output = brain.guess(inputs)[0];
//
//        turningAcceleration = p.map((float) output,0,1,-8f,8f);
//
//        if(turningSpeed<maxTurningSpeed && turningSpeed>-maxTurningSpeed){
//            turningSpeed+=turningAcceleration;
//        } else if(turningSpeed>maxTurningSpeed && turningAcceleration<0){
//            turningSpeed+=turningAcceleration;
//        } else if(turningSpeed<-maxTurningSpeed && turningAcceleration>0){
//            turningSpeed+=turningAcceleration;
//        }
//
//        rotation+=p.radians(turningSpeed);
//    }
//
//    public void sensors(){
//        // turn target angle into reading of a sensor
//        for(int i=0;i<sensors.length;i++){
//            if(i==6){ // special "ass" sensor which require special conditions
//                if(targetAngle<sensors[i][0] || targetAngle>sensors[i][1]){
//                    readingsPlayer[i] = 1;
//                } else {
//                    readingsPlayer[i] = 0;
//                }
//            } else // the rest of the sensors
//                if(targetAngle<sensors[i][0] && targetAngle>sensors[i][1]){
//                    readingsPlayer[i] = 1;
//                } else {
//                    readingsPlayer[i] = 0;
//                }
//        }
//
//        // roadRays
//        // collect readings from the roadRays and map them and store them in array
//        //the point which is the most far away is 0 and the closest is 1
//        for(int i = 0;i<roadRays.length;i++){
//            rayReadings[i] = p.map(roadRays[i].dist,roadRays[i].raySight,0,0,1);
//        }
//
//    }
//
//    void createRays(){
//        roadRays[0] = new Ray(p,position,p.radians(60),200);
//        roadRays[1] = new Ray(p,position,p.radians(160),200);
//        roadRays[2] = new Ray(p,position,p.radians(-60),200);
//        roadRays[3] = new Ray(p,position,p.radians(-160),200);
//        marchingRay = new Ray(p,position,300);
//    }
//
//    void createSensors(){
//        sensors[0][0] = p.radians(10);
//        sensors[0][1] = p.radians(-10);
//        sensors[1][0] = p.radians(-10);
//        sensors[1][1] = p.radians(-25);
//        sensors[2][0] = p.radians(-25);
//        sensors[2][1] = p.radians(-40);
//        sensors[3][0] = p.radians(-40);
//        sensors[3][1] = p.radians(-60);
//        sensors[4][0] = p.radians(-60);
//        sensors[4][1] = p.radians(-120);
//        sensors[5][0] = p.radians(-120);
//        sensors[5][1] = p.radians(-160);
//        sensors[6][0] = p.radians(-160);
//        sensors[6][1] = p.radians(160);
//        sensors[7][0] = p.radians(160);
//        sensors[7][1] = p.radians(120);
//        sensors[8][0] = p.radians(120);
//        sensors[8][1] = p.radians(60);
//        sensors[9][0] = p.radians(60);
//        sensors[9][1] = p.radians(40);
//        sensors[10][0] = p.radians(40);
//        sensors[10][1] = p.radians(25);
//        sensors[11][0] = p.radians(25);
//        sensors[11][1] = p.radians(10);
//    }
//
//    public void checkForRoad(Polygon road){
//        isOnRoad = road.contains(position.x,position.y);
//    }
//
//
//    public void updateTargetPos(PVector targetPos){
//        this.targetPos = targetPos;
//    }
//
//    public void showInfo(){
//        // everything about enemy
//        p.fill(0);
//
//        // global coordinate system
//        p.pushMatrix();
//        {
//            p.translate(position.x,position.y);
//            p.stroke(31, 10, 168);
//            p.strokeWeight(1);
//            p.line(-100, 0, 100, 0);
//            p.line(0, -100, 0, 100);
//            p.text("X", 90, 0);
//        } p.popMatrix();
//
//        // individual coordinate system
//        p.pushMatrix();
//        {
//            p.translate(position.x,position.y);
//            p.rotate(rotation);
//            p.stroke(70, 151, 163);
//            p.strokeWeight(1);
//            p.line(-50, 0, 50, 0);
//            p.line(0, -50, 0, 50);
//            p.text("X", 40, 0);
//        } p.popMatrix();
//
//        // line to the target calculated from the angle of rotation
//        p.pushMatrix();
//        {
//            p.translate(position.x,position.y);
//            p.rotate(rotation);
//            p.stroke(0,50);
//            p.line(0,0,p.cos(targetAngle)*targetDist,p.sin(targetAngle)*targetDist);
//        } p.popMatrix();
//
//        // line to the closest bullet calculated from the angle of rotation
//        p.pushMatrix();
//        {
//            p.translate(position.x, position.y);
//            p.rotate(rotation);
//            p.stroke(0);
//            p.line(0, 0, p.cos(closestBulletAngle) * closestBulletDist, p.sin(closestBulletAngle) * closestBulletDist);
//        }
//        p.popMatrix();
//
//        // shortest line to the road
//        p.pushMatrix();
//        {
//            p.translate(position.x, position.y);
//            p.stroke(207, 37, 167);
//            p.line(0,0, p.cos(marchingRay.bestAngle)*marchingRay.smallestDistToRoad,p.sin(marchingRay.bestAngle)*marchingRay.smallestDistToRoad);
//        }
//        p.popMatrix();
//
//        // rotations info
//        p.pushMatrix();
//        {
//            p.fill(0);
//            p.textSize(10);
//            p.translate(position.x - 40,position.y + 30);
//            p.text("ID: "+ index,0,0);
//            p.text("Rotation: " + (int)p.degrees(rotation),0,10);
//            p.text("Target Angle: " +(int)p.degrees(targetAngle),0,20);
//            p.text("n: " +n,0,30);
//
//            int active = 0;
//            for(int i = 0; i< readingsPlayer.length; i++){
//                if(readingsPlayer[i]>0){
//                    active=i;
//                }
//            }
//            p.text("Active sensor: "+ active,0,40);
//            p.text("Dist traveled: "+ distTraveled,0,50);
//            p.text("Is on road: "+ isOnRoad,0,60);
//        } p.popMatrix();
//
//
//    }
//
//    public void showRoute(boolean display){
//        p.strokeWeight(2);
//        distTraveled = 0;
//        for(int i =0;i<totalRoute.size();i++){
//            if(i<totalRoute.size()-1){
//                if(rounteOnRoad.contains(totalRoute.get(i))){
//                    p.stroke(166, 23, 149);
//                    distTraveled+=totalRoute.get(i).dist(totalRoute.get(i+1))/4;
//                } else {
//                    p.stroke(9, 111, 156);
//                    distTraveled+=totalRoute.get(i).dist(totalRoute.get(i+1));
//                }
//                if(display) p.line(totalRoute.get(i).x,totalRoute.get(i).y,totalRoute.get(i+1).x,totalRoute.get(i+1).y);
//            }
//        }
//    }
//
//    public void showSensors(){
//        //show work of sensors
//
//        //p.noStroke();
//        p.strokeWeight(1);
//        p.stroke(0,15);
//
//        p.pushMatrix();{
//            p.translate(position.x,position.y);
//            p.rotate(rotation);
//
//            // for player
//
//            for(int i = 0;i<sensors.length;i++){
//                if(readingsPlayer[i]>0){
//                    p.fill(0,40);
//                } else {
//                    p.fill(0,15);
//                }
//                p.triangle(0,0,p.cos(sensors[i][0])*80,p.sin(sensors[i][0])*80,p.cos(sensors[i][1])*80,p.sin(sensors[i][1])*80);
//
//            }
//
//            p.noFill();
//            p.stroke(0,15);
//            p.ellipse(0,0,roadRays[0].raySight*2,roadRays[0].raySight*2);
//
//        }p.popMatrix();
//    }
//
//    public void findClosestBullet(ArrayList<Projectile> bullets){
//        // Find the closest projectile, it's position and angle.
//
//        closestBulletDist = p.width;
//        for(Projectile projectile:bullets){
//            if(projectile.position.dist(position)<closestBulletDist){
//                closestBulletDist = projectile.position.dist(position);
//                closestBulletPos = projectile.position.copy();
//                closestBulletAngle = calculateAngleToTarget(closestBulletPos) - rotation;
//            }
//            if(projectile.position.dist(position)<10){
//                projectile.isDead = true;
//                health-=1;
//            }
//        }
//
//        if(bullets.size()==0){
//            closestBulletDist = 0;
//        }
//    }
//
//
//
//}