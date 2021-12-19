package pacman;

import java.awt.*;

import java.awt.event.*;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.io.FileWriter;
import java.util.Vector;
import java.awt.Graphics2D;

public class Model extends JPanel implements ActionListener {

    private Dimension d;
    private final Font SMALL_FONT = new Font("Arial", Font.BOLD, 28);
    private final Font GAMER_FONT = FontLoader.getFontFromFile("ARCADECLASSIC", 62f);
    private final Font TINY_FONT = FontLoader.getFontFromFile("ARCADECLASSIC", 31f);
    private final Font GAMER_FONT_SMALL = FontLoader.getFontFromFile("ARCADECLASSIC", 48f);
    private SoundPlayer highScoreSound = new SoundPlayer("sheesh.wav");
    private boolean dying = false;
    private boolean newHighScoreb = false;
    private final int BLOCK_SIZE = 48;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;
    private int lvlCounter = 1;
    private int nGhosts = 1;
    private int score;
    private int scoreBefore = 0; //untuk spawn powerup
    private int highScore;
    private URL urlHeart;
    private Image heart;
    private Image titleImage;
    private Image[] startButton;
    private Image[] aboutButton;
    private Image[] exitButton;
    private Image[] continueButton;
    private Image[] restartButton;
    private Image[] menuButton;
    private Image[] rockButton;
    private Image[] scissorButton;
    private Image[] paperButton;
    private int selectedButtonIntro = 0;
    private int selectedButtonPause = 0;

	private int scoreWeight = 1;
    private Player player;
    private int lives;
    private Ghost[] ghosts;
    private Vector<PowerUp> powerList = new Vector<>();
    Random numGenerator = new Random();
    private enum GameState {
        IN_GAME,
        INTRO_SCREEN,
        GAME_OVER,
        ABOUT_SCREEN,
        PAUSE_SCREEN,
        ONE_LAST_CHANCE
    }

    private GameState currentState = GameState.INTRO_SCREEN;

    private final short[] levelData = new short[225];

    private final int[] validSpeeds = {1, 2, 3, 4, 6, 8};

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;
    private boolean pickedAnswer = false;
    private enum ResultSuit {
        WIN,
        DRAW,
        LOSE,
        NOT_YET
    }
    private ResultSuit verdictDeath = ResultSuit.NOT_YET;
    private MouseHandler handler = new MouseHandler();
    private boolean continuePlaySuit = false;

    public Model() {
        loadMap();
        loadImages();
        loadHighScore();
        initVariables();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
        SoundPlayer.playContinuousSound("intro2.wav");
    }

    private URL loadImage(String fileName){
        return getClass().getResource("/images/" + fileName);
    }
    //load map
    private static Scanner input;
    private void loadMap(){
        int i = 0;
        try {
            String levelName = String.format("./src/map/level%d.txt", lvlCounter);
            //cek if there is still level map available
            if(Files.exists(Paths.get(levelName))){
                input = new Scanner(Paths.get(levelName)); //ganti path
            }else{
                input = new Scanner(Paths.get("./src/map/freelevel.txt")); //change path
            }
            while (input.hasNext()) {
                String[] data = input.nextLine().split(",");
                for (String datum : data) {
                    levelData[i] = Short.parseShort(datum);
                    i++;
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error opening Level file. Terminating.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    //to open file and show highscore
    private void loadHighScore(){
        try {
            input = new Scanner(Paths.get("../highscore.txt"));
            while (input.hasNextInt())
                highScore = input.nextInt();
        }
        catch (IOException ioException) {
            System.err.println("Error opening high score file. Terminating.");
            System.exit(1);
        }
    }

    //untuk mengupdate highscore
    private void updateHighScore(){
        try {
            FileWriter myWriter = new FileWriter("../highscore.txt");
            String otp = Integer.toString(highScore);
            myWriter.write(otp);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error on update high score occurred.");
            e.printStackTrace();
        }
    }

    //untuk menampilkan gambar
    private void loadImages() {
        //load url
        URL urlTitle = loadImage("title.png");
        urlHeart = loadImage("heart.png");
        URL urlStart1 = loadImage("Start1.png");
        URL urlStart2 = loadImage("Start2.png");
        URL urlAbout1 = loadImage("About1.png");
        URL urlAbout2 = loadImage("About2.png");
        URL urlExit1 = loadImage("Exit1.png");
        URL urlExit2 = loadImage("Exit2.png");
        URL urlContinue1 = loadImage("cont1.png");
        URL urlContinue2 = loadImage("cont2.png");
        URL urlRestart1 = loadImage("restart1.png");
        URL urlRestart2 = loadImage("restart2.png");
        URL urlMenu1 = loadImage("menu1.png");
        URL urlMenu2 = loadImage("menu2.png");
        URL urlRock1 = loadImage("Rock1.png");
        URL urlRock2 = loadImage("Rock2.png");
        URL urlPaper1 = loadImage ("Paper1.png");
        URL urlPaper2 = loadImage ("Paper2.png");
        URL urlScissor1 = loadImage ("Scissor1.png");
        URL urlScissor2 = loadImage ("Scissor2.png");
        //get images
        startButton = new Image[2];
        aboutButton = new Image[2];
        exitButton = new Image[2];
        restartButton = new Image[2];
        continueButton = new Image[2];
        menuButton = new Image[2];
        paperButton = new Image[2];
        rockButton = new Image[2];
        scissorButton = new Image[2];
        heart = new ImageIcon(urlHeart).getImage();
        titleImage = new ImageIcon(urlTitle).getImage();
        startButton[0] = new ImageIcon(urlStart1).getImage();
        startButton[1] = new ImageIcon(urlStart2).getImage();
        aboutButton[0] = new ImageIcon(urlAbout1).getImage();
        aboutButton[1] = new ImageIcon(urlAbout2).getImage();
        exitButton[0] = new ImageIcon(urlExit1).getImage();
        exitButton[1] = new ImageIcon(urlExit2).getImage();
        continueButton[0] = new ImageIcon(urlContinue1).getImage();
        continueButton[1] = new ImageIcon(urlContinue2).getImage();
        restartButton[0] = new ImageIcon(urlRestart1).getImage();
        restartButton[1] = new ImageIcon(urlRestart2).getImage();
        menuButton[0] = new ImageIcon(urlMenu1).getImage();
        menuButton[1] = new ImageIcon(urlMenu2).getImage();
        paperButton[0] = new ImageIcon(urlPaper1).getImage();
        paperButton[1] = new ImageIcon(urlPaper2).getImage();
        rockButton[0] = new ImageIcon(urlRock1).getImage();
        rockButton[1] = new ImageIcon(urlRock2).getImage();
        scissorButton[0] = new ImageIcon(urlScissor1).getImage();
        scissorButton[1] = new ImageIcon(urlScissor2).getImage();
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(800, 800);

        ghosts = new Ghost[MAX_GHOSTS];
        timer = new Timer(40, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {
            death();
        }
        else {
            movePacman();
            powerUpLogic(g2d);
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
        Image aboImage = aboutButton[0];
        Image staImage = startButton[0];
        Image exiImage = exitButton[0];
        pickedAnswer = false;
        g2d.setColor(Color.yellow);
        boolean startButt = false, aboutButt = false, exitButt = false;
        g2d.drawImage(titleImage,SCREEN_SIZE/2 - 266, SCREEN_SIZE/4, 532,96,this);
        ImageButton buttonStart = new ImageButton(SCREEN_SIZE/2 - 130, SCREEN_SIZE/2, 574 ,96,
                loadImage("Start1.png"), 0);
        ImageButton buttonAbort = new ImageButton(SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 96, 574,96,
                loadImage("About1.png"), 1);
        ImageButton buttonExit = new ImageButton(SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 192, 574,96,
                loadImage("Exit1.png"), 2);

        if(handler.cor_x >= 244 && handler.cor_x <= 470) {
            if (handler.cor_y>= 372&& handler.cor_y <= 444) {
                staImage = startButton[1];
                aboImage = aboutButton[0];
                exiImage = exitButton[0];
                selectedButtonIntro = 0;
            }
            else if(handler.cor_y >= 468 && handler.cor_y <= 540){
                staImage = startButton[0];
                aboImage = aboutButton[1];
                exiImage = exitButton[0];
                selectedButtonIntro = 1;
            }
            else if(handler.cor_y >= 552 && handler.cor_y <= 648){
                staImage = startButton[0];
                aboImage = aboutButton[0];
                exiImage = exitButton[1];
                selectedButtonIntro = 2;
            }
        }

        if(selectedButtonIntro == 0 || handler.cor_x >= 244 && handler.cor_x <= 470 && handler.cor_y>= 372&& handler.cor_y <= 444){
            staImage = startButton[1];
            aboImage = aboutButton[0];
            exiImage = exitButton[0];
        }
        else if(selectedButtonIntro == 1 || handler.cor_x >= 244 && handler.cor_x <= 470 && handler.cor_y >= 468 && handler.cor_y <= 540){
            staImage = startButton[0];
            aboImage = aboutButton[1];
            exiImage = exitButton[0];
        }
        else{
            staImage = startButton[0];
            aboImage = aboutButton[0];
            exiImage = exitButton[1];
        }

        if(handler.isClicking){
            if(buttonStart.isClicked(handler.cor_x, handler.cor_y)){
                startButt = true;
            }
            else if(buttonAbort.isClicked(handler.cor_x, handler.cor_y)){
                aboutButt = true;
            }
            else if(buttonExit.isClicked(handler.cor_x, handler.cor_y)){
                exitButt = true;
            }
        }
        if(startButt){
            currentState = GameState.IN_GAME;
            initGame();
        }
        else if(aboutButt){
            currentState = GameState.ABOUT_SCREEN;
        }
        else if(exitButt){
            System.exit(0);
        }
        g2d.drawImage(staImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2, 574 ,96,this);
        g2d.drawImage(aboImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 96, 574,96,this);
        g2d.drawImage(exiImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 192, 574,96,this);
    }

    private void showPauseScreen(Graphics2D g2d) {
        Image conImage,resImage,menImage;
        g2d.setColor(Color.yellow);
        String pauseString = "Paused";
        g2d.setFont(GAMER_FONT);
        g2d.drawString(pauseString, (SCREEN_SIZE)/2 - 100, 300);
        ImageButton buttonCon = new ImageButton(SCREEN_SIZE/2 - 130, SCREEN_SIZE/2, 574 ,96,
                loadImage("Start1.png"), 0);
        ImageButton buttonRes = new ImageButton(SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 96, 574,96,
                loadImage("About1.png"), 1);
        ImageButton buttonMen = new ImageButton(SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 192, 574,96,
                loadImage("Exit1.png"), 2);
        if(selectedButtonPause % 3 == 0){
            conImage = continueButton[1];
            resImage = restartButton[0];
            menImage = menuButton[0];
        }
        else if(selectedButtonPause % 3 == 1) {
            conImage = continueButton[0];
            resImage = restartButton[1];
            menImage = menuButton[0];
        }
        else {
            conImage = continueButton[0];
            resImage = restartButton[0];
            menImage = menuButton[1];
        }
        if(handler.cor_x>= 246&& handler.cor_x <=463) {
            if (handler.cor_y >= 372 && handler.cor_y <= 443) {
                conImage = continueButton[1];
                resImage = restartButton[0];
                menImage = menuButton[0];
                selectedButtonPause = 0;
            }
            else if(handler.cor_y >= 473 && handler.cor_y <= 543){
                conImage = continueButton[0];
                resImage = restartButton[1];
                menImage = menuButton[0];
                selectedButtonPause = 1;
            }
            else if(handler.cor_y >= 564 && handler.cor_y <= 640){
                conImage = continueButton[0];
                resImage = restartButton[0];
                menImage = menuButton[1];
                selectedButtonPause = 2;
            }
        }
        if(handler.isClicking && !pickedAnswer){
            if(buttonCon.isClicked(handler.cor_x, handler.cor_y)){
                currentState = GameState.IN_GAME;
                handler.isClicking = false;
            }
            else if(buttonRes.isClicked(handler.cor_x, handler.cor_y)){
                currentState = GameState.IN_GAME;
                initGame();
                handler.isClicking = false;
            }else if(buttonMen.isClicked(handler.cor_x, handler.cor_y)){
                currentState = GameState.INTRO_SCREEN;
                handler.isClicking = false;
            }
        }
        g2d.drawImage(conImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2, 574 ,96,this);
        g2d.drawImage(resImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 96, 574,96,this);
        g2d.drawImage(menImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 192, 574,96,this);
    }
    private void showAboutScreen(Graphics2D g2d) {
        Font smallGamerFont = FontLoader.getFontFromFile("ARCADECLASSIC", 32f);
        String aboutText = "NOT SO NORMAL PACMAN!";
        String aboutText2 = "Built By ";
        String aboutText3 = "Anak Agung Yatestha Parwata  5025201234";
        String aboutText4 = "Januar Evan  5025201210";
        String aboutText5 =  "Putu Ravindra Wiguna  5025201237";
        String aboutText6 = "Ridzki Raihan Alfaza  5025201178";
        g2d.setColor(Color.yellow);
        g2d.setFont(smallGamerFont);
        g2d.drawString(aboutText, (SCREEN_SIZE)/5 - 100, 100);
        g2d.drawString(aboutText2, (SCREEN_SIZE)/5- 100, 300);
        g2d.drawString(aboutText3, (SCREEN_SIZE)/5- 100, 320);
        g2d.drawString(aboutText4, (SCREEN_SIZE)/5- 100, 340);
        g2d.drawString(aboutText5, (SCREEN_SIZE)/5- 100, 360);
        g2d.drawString(aboutText6, (SCREEN_SIZE)/5- 100, 380);
    }

    private void showGameOverScreen(Graphics2D g2d){
        String gameOverString = "Game Over";
        String scoreString = "Your Score " + score;
        String newHighScore = "Congrats new highscore!";
        g2d.setColor(Color.yellow);
        g2d.setFont(GAMER_FONT);
        g2d.drawString(gameOverString, (SCREEN_SIZE)/5, 300);
        g2d.drawString(scoreString, (SCREEN_SIZE)/5, 400);
        if(newHighScoreb){
            g2d.setColor(Color.yellow);
            g2d.setFont(GAMER_FONT_SMALL);
            g2d.drawString(newHighScore,(SCREEN_SIZE)/5-100,500);
            highScoreSound.playSoundOnce();
        }
    }

    private void playOneLastChance(Graphics2D g2d){
        //set msg
        String info1 = "You are in the limbo";
        String info2 = "If you win in this battle";
        String info3 = "You will be revived";
        String info4 = "Choose Rock | Paper | Scissor?";

        g2d.setColor(Color.yellow);
        g2d.setFont(TINY_FONT);
        int chosenValue = 0;
        g2d.drawString(info1, (SCREEN_SIZE)/4, 100);
        g2d.drawString(info2, (SCREEN_SIZE)/4, 150);
        g2d.drawString(info3, (SCREEN_SIZE)/4, 200);
        g2d.drawString(info4, (SCREEN_SIZE)/4, 250);
        //buat 3 rock paper scissor button
        ImageButton buttonRock = new ImageButton(SCREEN_SIZE/3 - 210, 2*SCREEN_SIZE/3-80, 200, 200,
                loadImage("Rock1.png"), 0);
        ImageButton buttonPaper = new ImageButton(SCREEN_SIZE/3 + 12, 2*SCREEN_SIZE/3-80, 200, 200,
                loadImage("Paper1.png"), 1);
        ImageButton buttonScissor = new ImageButton(2*SCREEN_SIZE/3 + 12, 2*SCREEN_SIZE/3-80, 200, 200,
                loadImage("Scissor1.png"), 2);
        Image rocImage,papImage,sciImage;
        g2d.setColor(Color.yellow);
        g2d.setFont(TINY_FONT);
        rocImage = rockButton[0];
        papImage = paperButton[0];
        sciImage = scissorButton[0];
        if(handler.cor_y>= 398&& handler.cor_y <=600) {
            if (handler.cor_x >= 30 && handler.cor_x <= 229) {
                rocImage = rockButton[1];
            }
            else if(handler.cor_x >= 251 && handler.cor_x <= 451){
                papImage = paperButton[1];
            }
            else if(handler.cor_x >= 472 && handler.cor_x <= 692){
                sciImage = scissorButton[1];
            }
        }
        g2d.drawImage(rocImage,SCREEN_SIZE/3 - 210, 2*SCREEN_SIZE/3-80, 200, 200,this);
        g2d.drawImage(papImage,SCREEN_SIZE/3 + 12, 2*SCREEN_SIZE/3-80, 200, 200,this);
        g2d.drawImage(sciImage,2*SCREEN_SIZE/3 + 12, 2*SCREEN_SIZE/3-80, 200, 200,this);
//        gambar sir
//        kalau diklik dan belum ambil jawaban
        if(handler.isClicking && !pickedAnswer){
            if(buttonRock.isClicked(handler.cor_x, handler.cor_y)){
                chosenValue = buttonRock.getReturnValue();
                System.out.println(chosenValue);
                pickedAnswer = true;
            }
            else if(buttonPaper.isClicked(handler.cor_x, handler.cor_y)){
                chosenValue = buttonRock.getReturnValue();
                System.out.println(chosenValue);
                pickedAnswer = true;
            }else if(buttonScissor.isClicked(handler.cor_x, handler.cor_y)){
                chosenValue = buttonRock.getReturnValue();
                System.out.println(chosenValue);
                pickedAnswer = true;
            }
        }
        //kalau udah ambi jawaban tapi belum simulasi hasil
        if(pickedAnswer && (verdictDeath == ResultSuit.NOT_YET)){
            int res = numGenerator.nextInt(3);
            System.out.println(res);
            if(chosenValue - res == 1){
                //WIN CERITANYA
                verdictDeath = ResultSuit.WIN;
                player.increaseLives();
            }else if(res - chosenValue == 2){
                verdictDeath = ResultSuit.WIN;
                player.increaseLives();
            } else if(res == chosenValue){
                //DRAW CERITANYA
                verdictDeath = ResultSuit.DRAW;
            }else{
                //YAH KALAH
                verdictDeath = ResultSuit.LOSE;
                dying = true;
            }
        }
        //kalau udah ada result dan di klik lagi, baru lanjut main
        if(verdictDeath != ResultSuit.NOT_YET){
            switch (verdictDeath){
                case WIN:
                    String win = "YOU WIN, YOU MAY LIVE ONCE AGAIN";
                    g2d.drawString(win, (SCREEN_SIZE)/4, 300);
                    break;
                case DRAW:
                    String draw = "A DRAW. CLOSE ONE. TRY AGAIN";
                    g2d.drawString(draw, (SCREEN_SIZE)/4, 300);
                    break;
                case LOSE:
                    String lose = "YOU LOSE";
                    g2d.drawString(lose, (SCREEN_SIZE)/4, 300);
                    break;
            }
            String explain = "Press enter to try again";
            g2d.drawString(explain, (SCREEN_SIZE)/4, 350);
            if(continuePlaySuit){
                pickedAnswer = false;
                continuePlaySuit = false;
                if(verdictDeath != ResultSuit.DRAW){
                    currentState = GameState.IN_GAME;
                }
                verdictDeath = ResultSuit.NOT_YET;

            }
        }
    }

    private void drawScore(Graphics2D g) {
        g.setFont(SMALL_FONT);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 192, SCREEN_SIZE + 32);
        g.drawImage(heart,40,SCREEN_SIZE+2,this);
        int temp = player.getLives();
        if(player.getLives() < 0) temp = 0;
        String StrLives = ""+ temp;
        g.drawString(StrLives, SCREEN_SIZE/2 - 340, SCREEN_SIZE+32);
        String strLvl = "lvl "+lvlCounter;
        g.drawString(strLvl, SCREEN_SIZE/2 - 240, SCREEN_SIZE+32);
    }

    private void drawHighScore(Graphics2D g) {
        g.setFont(SMALL_FONT);
        g.setColor(new Color(5, 181, 79));
        String s = "High Score: " + highScore;
        g.drawString(s, SCREEN_SIZE / 2 - 120, SCREEN_SIZE + 32);
    }
    private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if (screenData[i] >= 16) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;
            player.increaseLives();
            SoundPlayer.playSound("levelup.wav");
            lives = player.getLives();
            if (nGhosts < MAX_GHOSTS) {
                nGhosts++;
            }

            int maxSpeed = 5;
            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }
            lvlCounter++;
            initLevel();
        }
    }

    private void death() {

        player.decreaseLives();
        lives = player.getLives();
        if (player.getLives() <= 0) {
            if(player.getLives() == 0){
                currentState= GameState.ONE_LAST_CHANCE;
            }
            else{
                SoundPlayer.playSound("death.wav");
                currentState = GameState.GAME_OVER;
            }
        }
        else{
            SoundPlayer.playSound("damaged.wav");
        }
        scoreWeight = 1;
        continueLevel();
    }

    private void detectDeath(int id) {
    	//detect if pacman close to ghost with index id
        if (ghosts[id].detectPlayerCollision(player) && currentState == GameState.IN_GAME) {
            if(!player.canEatGhosts)
                dying = true;
            else
            {
                score += 25;
                ghosts[id].death();
            }
        }
    }
    
    private void drawEntity(Graphics2D g2d, Entity en) {
    	g2d.drawImage(en.getCurrentImage(), en.x + 2, en.y + 2, this);
    }

    private void drawImageObject(Graphics2D g2d, ImageObject io){
        g2d.drawImage(io.img, io.x + 2, io.y + 2, this);
    }

    private void fixEntityPos(Entity en){
        if(en.y > 672) {
            en.y = 0;
            //kalau ternyata di atasnya ada border atas, teleport ke bawah
            if((screenData[en.x/BLOCK_SIZE]&2) == 2){
                en.y = 672;
            }
        }
        if(en.x > 672){
            en.x = 0;
            //kalau di kiri ada tembok ke kiri
            if((screenData[en.y/BLOCK_SIZE*N_BLOCKS]&1) == 1){
                en.x = 672;
            }
        }
        if(en.x < 0){
            en.x = 672;
            if((screenData[14 + en.y/BLOCK_SIZE*N_BLOCKS]&4)==4){
                en.x = 0;
            }
        }
        if(en.y < 0 ){
            en.y = 672;
            if((screenData[en.x/BLOCK_SIZE + 210]&8) == 8){
                en.y = 0;
            }
        }
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;

        for (int i = 0; i < nGhosts; i++) {
            if(!ghosts[i].isDead) {
                if (player.isFacing != Entity.Direction.NEUTRAL) {
                    if (ghosts[i].x % BLOCK_SIZE == 0 && ghosts[i].y % BLOCK_SIZE == 0) {
                        pos = ghosts[i].getScreenPos(BLOCK_SIZE, N_BLOCKS);

                        ghosts[i].moveGhost(screenData[pos]);
                    }
                    ghosts[i].updateMovement();
                }
                fixEntityPos(ghosts[i]);

                //detect death
                detectDeath(i);
            }
            else {
                ghosts[i].respawnIfReady();
            }
            drawGhost(g2d, i);
        }
    }

    private void drawGhost(Graphics2D g2d, int id) {
        drawEntity(g2d,ghosts[id]);
    }

    private void movePacman() {

        int pos;
        short ch;

        if (player.x % BLOCK_SIZE == 0 && player.y % BLOCK_SIZE == 0) {
            pos = player.x / BLOCK_SIZE + N_BLOCKS * (player.y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                SoundPlayer.playSound("eating.wav");
                score+=scoreWeight;
            }
            if(highScore <= score){
                highScore = score;
                updateHighScore();
                newHighScoreb = true;
            }else{
                newHighScoreb = false;
            }
            player.movePlayer(ch);

        }
        player.updateMovement();
        fixEntityPos(player);
    }

    private void drawPacman(Graphics2D g2d) {
        drawEntity(g2d, player);
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(8));

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 2);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 2, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 2, y, x + BLOCK_SIZE - 2,
                            y + BLOCK_SIZE - 2);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 2, x + BLOCK_SIZE - 2,
                            y + BLOCK_SIZE - 2);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 20, y + 20, 12, 12);
                }

                i++;
            }
        }
    }

    private void powerUpLogic(Graphics2D g2d){
        //cek spawn
        if(((score- scoreBefore) >= 250 ) & score > 0){
            //spawn the power up
            scoreBefore = score;
            System.out.println(scoreBefore);
            int powerKind = numGenerator.nextInt(3);
            switch (powerKind) {
                case 0 :
                    powerList.add(new HeartPower(ghosts[0].x, ghosts[0].y, urlHeart));
                    break;
                case 1 :
                    powerList.add(new ScoreWeightPower(ghosts[0].x, ghosts[0].y, loadImage("thunder.gif")));
                    break;
                case 2 :
                    powerList.add(new EatGhostPower(ghosts[0].x, ghosts[0].y, loadImage("superPellet.png")));
                    break;
            }
            score+=scoreWeight;
        }

        if(!powerList.isEmpty()){
            PowerUp curPower;
            //check for collision
            for(int i = 0;i<powerList.size();i++){
                curPower = powerList.get(i);
                if(curPower.isCollided(player)){
                    if(curPower instanceof HeartPower){
                        curPower.activatePower(player);
                        SoundPlayer.playSound("addlives.wav");
                    }
                    if(curPower instanceof ScoreWeightPower){
                        scoreWeight = curPower.activatePower(scoreWeight);
                        System.out.println(scoreWeight);
                        SoundPlayer.playSound("bonuspoint.wav");
                    }
                    if(curPower instanceof EatGhostPower){
                        curPower.activatePower(player);
                        SoundPlayer.playSound("bonuspoint.wav");
                    }
                    powerList.remove(i);
                }
            }

            //draw it
            for(int i = 0;i<powerList.size();i++) {
                drawImageObject(g2d, powerList.get(i));
            }
        }
    }

    private void initGame() {

//        player.setLives(3);
        lives = 3;
        score = 0;
        initLevel();
        currentSpeed = 3;
        lvlCounter = 1;
        nGhosts = 1;
        scoreWeight = 1;
        scoreBefore = 0;
        verdictDeath = ResultSuit.NOT_YET;
        pickedAnswer = false;
        continuePlaySuit = false;
        powerList.clear();
    }

    private void initLevel() {
        loadMap();
        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];

        }
        continueLevel();
    }

    private boolean isSpawnInside(int x, int y){
        //cek atas
        if(y > 0){
            int temp = y-1;
            //kalau di atas ada border atas (tidak bisa masuk dari atas)
            if((screenData[x + temp*N_BLOCKS]&8) == 8){
                return true;
            }
        }
        //cek bawah
        if(y < 14){
            int temp = y+1;
            //cek di bawah ada border bawah, kalau ada berarti spawn di dalam
            if((screenData[x + temp*N_BLOCKS]&2) == 2){
                return true;
            }
        }
        //cek kiri
        if(x > 0){
            int temp = x-1;
            if((screenData[temp + y*N_BLOCKS]&4) == 4){
                return true;
            }
        }
        //cek kanan
        if(x < 14){
            int temp = x+1;
            return (screenData[temp + y * N_BLOCKS] & 1) == 1;
        }
        return false;
    }

    private void continueLevel() {

        int dx = 1;
        int random;
        int startGhost_x = numGenerator.nextInt(15), startGhost_y = numGenerator.nextInt(15);
        int ghostOneDimensionPos = startGhost_x + startGhost_y*N_BLOCKS;
        while(isSpawnInside(startGhost_x, startGhost_y)){
            startGhost_x = numGenerator.nextInt(15); startGhost_y = numGenerator.nextInt(15);
            ghostOneDimensionPos = startGhost_x + startGhost_y*N_BLOCKS;

        }
        for (int i = 0; i < nGhosts; i++) {
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            //create new ghost with this configuration
            ghosts[i] = new Ghost(startGhost_x*BLOCK_SIZE, startGhost_y*BLOCK_SIZE, 0, dx, validSpeeds[random]);
            dx = -dx;
        }
        //create new player
        //create a good start pos
        int start_x = numGenerator.nextInt(15), start_y = numGenerator.nextInt(15);
        int oneDimensionPos = start_x + start_y*N_BLOCKS;
        while(isSpawnInside(start_x, start_y) || oneDimensionPos == ghostOneDimensionPos){
            start_x = numGenerator.nextInt(15); start_y = numGenerator.nextInt(15);
            oneDimensionPos = start_x + start_y*N_BLOCKS;
        }
        player = new Player(start_x*BLOCK_SIZE, start_y*BLOCK_SIZE, 0, 0, PACMAN_SPEED, lives);
        dying = false;
        pickedAnswer = false;
        scoreWeight = 1;
        verdictDeath = ResultSuit.NOT_YET;
        continuePlaySuit = false;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        drawHighScore(g2d);
        switch (currentState){
            case IN_GAME:
                playGame(g2d);
                break;
            case ABOUT_SCREEN:
                showAboutScreen(g2d);
                break;
            case INTRO_SCREEN:
                showIntroScreen(g2d);
                break;
            case PAUSE_SCREEN:
                showPauseScreen(g2d);
                break;
            case GAME_OVER:
                showGameOverScreen(g2d);
                break;
            case ONE_LAST_CHANCE:
                playOneLastChance(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            switch (currentState){
                case IN_GAME:
                    if (!player.getInput(key)) { //if player doesn't get a movement input
                        if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                            currentState = GameState.INTRO_SCREEN;
                        }
                    }
                    if(key == KeyEvent.VK_ESCAPE){
                        SoundPlayer.playSound("esc.wav");
                        currentState = GameState.PAUSE_SCREEN;
                    }
                    break;
                case ABOUT_SCREEN:
                    if(key == KeyEvent.VK_ENTER) {
                        selectedButtonIntro = 0;
                        currentState = GameState.INTRO_SCREEN;
                        repaint();
                    }
                    if(key == KeyEvent.VK_ESCAPE){
                        SoundPlayer.playSound("esc.wav");
                        selectedButtonIntro = 0;
                        currentState = GameState.INTRO_SCREEN;
                    }
                    break;
                case INTRO_SCREEN:
                    if (key == KeyEvent.VK_ENTER) {
                        SoundPlayer.playSound("enter.wav");
                        if(selectedButtonIntro % 3 == 0) {
                            currentState = GameState.IN_GAME;
                            SoundPlayer.playSound("bruh.wav");
                            initGame();
                        }
                        if(selectedButtonIntro % 3 == 1){
                            currentState = GameState.ABOUT_SCREEN;
                        }
                        if(selectedButtonIntro % 3 == 2){
                            System.exit(0);
                        }
                    }
                    if(key == KeyEvent.VK_ESCAPE) {
                        SoundPlayer.playSound("esc.wav");
                        System.exit(0);
                    }
                    if (key == KeyEvent.VK_UP) {
                        SoundPlayer.playSound("topdown.wav");
                        selectedButtonIntro--;
                        if(selectedButtonIntro < 0) {
                            selectedButtonIntro = 2;
                        }
                    }
                    else if(key == KeyEvent.VK_DOWN) {
                        SoundPlayer.playSound("topdown.wav");
                        selectedButtonIntro++;
                        if(selectedButtonIntro == 3) {
                            selectedButtonIntro = 0;
                        }
                    }
                    break;
                case PAUSE_SCREEN:
                    if (key == KeyEvent.VK_ENTER) {
                        SoundPlayer.playSound("enter.wav");
                        if(selectedButtonPause % 3 == 0) {
                            currentState = GameState.IN_GAME;
                        }
                        if(selectedButtonPause % 3 == 1){
                            currentState = GameState.IN_GAME;
                            initGame();
                        }
                        if(selectedButtonPause % 3 == 2){
                            currentState = GameState.INTRO_SCREEN;
                        }
                    }
                    if (key == KeyEvent.VK_ESCAPE){
                        SoundPlayer.playSound("esc.wav");
                        currentState = GameState.IN_GAME;
                    }
                    if (key == KeyEvent.VK_UP) {
                        SoundPlayer.playSound("topdown.wav");
                        selectedButtonPause--;
                        if(selectedButtonPause < 0) {
                            selectedButtonPause = 2;
                        }
                    }
                    else if(key == KeyEvent.VK_DOWN) {
                        SoundPlayer.playSound("topdown.wav");
                        selectedButtonPause++;
                        if(selectedButtonPause == 3) {
                            selectedButtonPause = 0;
                        }
                    }
                    break;
                case ONE_LAST_CHANCE:
                    if(key == KeyEvent.VK_ENTER){
                        continuePlaySuit = true;
                    }
                    break;
                case GAME_OVER:
                    if (key == KeyEvent.VK_ENTER) {
                        SoundPlayer.playSound("enter.wav");
                        currentState = GameState.INTRO_SCREEN;
                        initGame();
                    }
                    else if(key == KeyEvent.VK_SPACE){
                        SoundPlayer.playSound("esc.wav");
                        currentState = GameState.INTRO_SCREEN;
                        initGame();
                    }
                    break;
            }
        }
    }

    //mouse listener
    static class MouseHandler implements MouseListener,MouseMotionListener{
        private int cor_x, cor_y;
        private boolean isClicking = false;
        public MouseHandler(){
            super();
            cor_x =0;
            cor_y=0;
        }

        @Override
        public void mouseClicked(MouseEvent event){
            cor_x = event.getX();
            cor_y = event.getY();
        }

        @Override
        public void mousePressed(MouseEvent event){
            isClicking = true;
            cor_x = event.getX();
            cor_y = event.getY();
        }

        @Override
        public void mouseReleased(MouseEvent event){
            isClicking = false;
            cor_x = event.getX();
            cor_y = event.getY();
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event){
        }
        @Override
        public void mouseDragged(MouseEvent event){

        }
        @Override
        public void mouseMoved(MouseEvent event){
            cor_x = event.getX();
            cor_y = event.getY();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}
