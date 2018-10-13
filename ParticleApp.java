import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;

import javafx.scene.transform.Rotate;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.embed.swing.JFXPanel;
import java.util.Random;

//looks like that I can create a JavaFX panel and added to Jpanel

public class ParticleApp extends JFXPanel {
 
    final Group root = new Group();
    final Xform world = new Xform();

    //camera
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -100;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;

    //mouse & keyboard control stuff
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    //paritcles
    final Xform particleGroup = new Xform();
    private static final int PARTICLE_NUM = 400;

    //the sphere
    private static final double RADIUS = 20.0; 

    //animation
    private Timeline timeline;

    public ParticleApp(int width, int height){

        root.getChildren().add(world);
        root.setDepthTest(DepthTest.ENABLE);

        buildCamera();

        buildMolecule();
 
        Scene scene = new Scene(root, width, height, true);
        scene.setFill(Color.BLACK);

        handleMouse(scene, world);

        initParticlesPos();
 
        scene.setCamera(camera);

        setScene(scene);

    }

    private void initParticlesPos(){
        int index = 0;
        double[] pos = new double[3];
        for(Node particle : particleGroup.getChildren()){
            gen3DPos(pos, index);
            particle.setTranslateX(pos[0]);
            particle.setTranslateY(pos[1]);
            particle.setTranslateZ(pos[2]);
            index++;
        }
    }

    // //generate a perfect sphere
    // private void initParticlesPos(){
    //     //layer is the z
    //     int layer = 25;
    //     int center = layer / 2 + 1;
    //     double layerGap = 2 * RADIUS / layer;
    //     int layerNum = PARTICLE_NUM / layer;
    //     double surRadius = 0;
    //     int layerIndex = 0;
    //     for (Node particle : particleGroup.getChildren()) {
    //         double zpos = (layer - center) * layerGap;
    //         surRadius = Math.sqrt((RADIUS * RADIUS) - (zpos * zpos));
    //         double xpos = Math.cos(layerIndex * 2.0 * Math.PI / layerNum) * surRadius;
    //         double ypos = Math.sin(layerIndex * 2.0 * Math.PI / layerNum) * surRadius;
    //         particle.setTranslateX(xpos);
    //         particle.setTranslateY(ypos);
    //         particle.setTranslateZ(zpos);
    //         layerIndex++;
    //         if(layerIndex >= layerNum){
    //             layerIndex = 0;
    //             layer--;
    //         }
    //     }
    // }
 
    //function to build the camera
    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);
 
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private void buildMolecule() {
        //======================================================================
        // THIS IS THE IMPORTANT MATERIAL FOR THE TUTORIAL
        //======================================================================

        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.RED);
        redMaterial.setSpecularColor(Color.WHITE);

        for(int i = 0; i < PARTICLE_NUM; i++){
			Xform particleXform = new Xform();
			Sphere particle = new Sphere(0.15);

			particle.setMaterial(redMaterial);

			particleXform.getChildren().add(particle);
			particleGroup.getChildren().add(particleXform);
   		}

        world.getChildren().addAll(particleGroup);

    }

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX); 
                mouseDeltaY = (mousePosY - mouseOldY); 
                
                double modifier = 1.0;
                
                if (me.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER;
                } 
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }     
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);  
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);  
                }
                else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);  
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);  
                }
            }
        });
    }

    public void framePause(){
        if(timeline != null){
            timeline.pause();
        }
    }

    public void frameResume(){
        if(timeline != null){
            timeline.play();
        }
    }

    public void frameStop(){
        if(timeline != null){
            timeline.stop();
        }
    }

    public void setFrame(int dur){
        timeline = new Timeline();
        int index = 0;
        int frame = 5;
        //int dur = 10000; //2.4s

        for (Node particle : particleGroup.getChildren()) {

            double[] positionVal = new double[3];
            gen3DPos(positionVal, index);

            KeyValue preX = new KeyValue(particle.translateXProperty(), particle.getTranslateX());
            KeyValue preY = new KeyValue(particle.translateYProperty(), particle.getTranslateY());
            KeyValue preZ = new KeyValue(particle.translateZProperty(), particle.getTranslateZ());

            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, preX, preY, preZ));
            for(int i = 1; i < frame; i++){
                gen3DPos(positionVal, i);

                KeyValue nextX = new KeyValue(particle.translateXProperty(), positionVal[0]);
                KeyValue nextY = new KeyValue(particle.translateYProperty(), positionVal[1]);
                KeyValue nextZ = new KeyValue(particle.translateZProperty(), positionVal[2]);

                timeline.getKeyFrames().add(new KeyFrame(new Duration(i * dur / frame), nextX, nextY, nextZ));
            }
            timeline.getKeyFrames().add(new KeyFrame(new Duration(dur), preX, preY, preZ));

            index++;
        }

         // play 2.4s of animation
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    //make the molecule in circle
    //not finish
    private static void gen3DPos(double[] position, int index){
        Random rand = new Random();
        double valX, valY, valZ;

        valX = (rand.nextDouble() * 2 * RADIUS) - RADIUS;
        double leftOver = Math.sqrt((RADIUS * RADIUS) - (valX * valX));
        valY = (rand.nextDouble() * 2 * leftOver) - leftOver;

        valZ = Math.sqrt((RADIUS * RADIUS) - (valX * valX) - (valY * valY));
        if(rand.nextInt(2) == 0){
            valZ = -valZ;
        }

        position[0] = valX; // position x
        position[1] = valY; // position y
        position[2] = valZ; // position z
    }

    private static boolean isInRange(double valX, double valY, double valZ){
    	return (valX * valX) + (valY * valY) + (valZ * valZ) <= RADIUS * RADIUS;
    }
}