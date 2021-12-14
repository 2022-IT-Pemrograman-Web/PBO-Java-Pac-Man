package pacman;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

public class Model extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 28);
    private final Font gamerFont = FontLoader.getFontFromFile("ARCADECLASSIC", 62f);
    private final Font gamerFontSmall = FontLoader.getFontFromFile("ARCADECLASSIC", 48f);
    private SoundPlayer highScoreSound = new SoundPlayer("sheesh.wav");
    private boolean dying = false;
    private boolean newHighScoreb = false;
    private final int BLOCK_SIZE = 48;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;
    private int lvlCounter = 1;
    private int N_GHOSTS = 1;
    private int score;
    private int highScore;
    private int[] dx, dy;
    private URL urlHeart;
    private Image heart;
    private Image titleImage;
    private Image ghostEyes;
    private Image[] startButton;
    private Image[] aboutButton;
    private Image[] exitButton;
    private Image[] continueButton;
    private Image[] restartButton;
    private Image[] menuButton;
    private int selectedButtonIntro = 0;
    private int selectedButtonPause = 0;

	private int scoreWeight = 1;
    private Player player;
    private int lives;
    private Ghost[] ghosts;
    private Vector<PowerUp> powerList = new Vector<PowerUp>();
    private int totalPowerUpType = 2;
    Random numGenerator = new Random();
    private enum GameState{
        inGame,
        introScreen,
        gameOver,
        aboutScreen,
        pauseScreen
    }

    private GameState currentState = GameState.introScreen;

    private final short[] levelData = new short[225];

    private final int[] validSpeeds = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Model() {
        loadMap();
        loadImages();
        loadHighScore();
        initVariables();
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
            //cek apakah masi ada level map available
            if(Files.exists(Paths.get(levelName))){
                input = new Scanner(Paths.get(levelName)); //ganti path
            }else{
                input = new Scanner(Paths.get("./src/map/freelevel.txt")); //ganti path
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

    //untuk membuka file dan menampilkan highscore
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
        URL urlEyes = loadImage("ghostEyes.png");
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
        //get images
        startButton = new Image[2];
        aboutButton = new Image[2];
        exitButton = new Image[2];
        restartButton = new Image[2];
        continueButton = new Image[2];
        menuButton = new Image[2];

        ghostEyes = new ImageIcon(urlEyes).getImage();
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
    }
    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(800, 800);
        dx = new int[4];
        dy = new int[4];

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
        Image aboImage,staImage,exiImage;
        g2d.setColor(Color.yellow);
        g2d.drawImage(titleImage,SCREEN_SIZE/2 - 266, SCREEN_SIZE/4, 532,96,this);
        if(selectedButtonIntro % 3 == 0){
            staImage = startButton[1];
            aboImage = aboutButton[0];
            exiImage = exitButton[0];
        }
        else if(selectedButtonIntro % 3 == 1) {
            staImage = startButton[0];
            aboImage = aboutButton[1];
            exiImage = exitButton[0];
        }
        else {
            staImage = startButton[0];
            aboImage = aboutButton[0];
            exiImage = exitButton[1];
        }
        g2d.drawImage(staImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2, 574 ,96,this);
        g2d.drawImage(aboImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 96, 574,96,this);
        g2d.drawImage(exiImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 192, 574,96,this);
    }

    private void showPauseScreen(Graphics2D g2d) {
        Image conImage,resImage,menImage;
        g2d.setColor(Color.yellow);
        String pauseString = "Paused";
        g2d.setFont(gamerFont);
        g2d.drawString(pauseString, (SCREEN_SIZE)/2 - 100, 300);
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
        g2d.drawImage(conImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2, 574 ,96,this);
        g2d.drawImage(resImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 96, 574,96,this);
        g2d.drawImage(menImage,SCREEN_SIZE/2 - 130, SCREEN_SIZE/2 + 192, 574,96,this);
    }
    private void showAboutScreen(Graphics2D g2d) {
        Font smallGamerFont = FontLoader.getFontFromFile("ARCADECLASSIC", 32f);
        String aboutText = "NORMAL PACMAN!";
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
        g2d.setFont(gamerFont);
        g2d.drawString(gameOverString, (SCREEN_SIZE)/5, 300);
        g2d.drawString(scoreString, (SCREEN_SIZE)/5, 400);
        if(newHighScoreb){
            System.out.print("JALAN\n");
            g2d.setColor(Color.yellow);
            g2d.setFont(gamerFontSmall);
            g2d.drawString(newHighScore,(SCREEN_SIZE)/5-100,500);
            highScoreSound.playSoundOnce();
        }
    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 192, SCREEN_SIZE + 32);
        g.drawImage(heart,40,SCREEN_SIZE+2,this);
        String StrLives = ""+ player.getLives();
        g.drawString(StrLives, SCREEN_SIZE/2 - 340, SCREEN_SIZE+32);
        String strLvl = "lvl "+lvlCounter;
        g.drawString(strLvl, SCREEN_SIZE/2 - 240, SCREEN_SIZE+32);
    }

    private void drawHighScore(Graphics2D g) {
        g.setFont(smallFont);
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
            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

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
        if (player.getLives() == 0) {
            SoundPlayer.playSound("death.wav");
            currentState = GameState.gameOver;
        }
        else{
            SoundPlayer.playSound("damaged.wav");
        }

        continueLevel();
    }

    private void detectDeath(int id) {
    	//detect if pacman close to ghost with index id
        if (ghosts[id].detectPlayerCollision(player) && currentState == GameState.inGame) {
            if(!player.canEatGhosts)
                dying = true;
            else
            {
                ghosts[id].death();
            }
        }
    }
    
    private void drawEntity(Graphics2D g2d, Entity en) {
    	g2d.drawImage(en.getCurrentImage(), en.x + 2, en.y + 2, this);
    }

    private void drawPowerUp(Graphics2D g2d, PowerUp pu){
        g2d.drawImage(pu.img, pu.x + 2, pu.y + 2, this);
    }

    private void fixEntityPos(Entity en){
        if(en.y > 672) {
            en.y = 0;
        }
        if(en.x > 672){
            en.x = 0;
        }
        if(en.x < 0){
            en.x = 672;
        }
        if(en.y < 0){
            en.y = 672;
        }
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;

        for (int i = 0; i < N_GHOSTS; i++) {
            if(!ghosts[i].isDead) {
                if (player.isFacing != Entity.Direction.NEUTRAL) {
                    if (ghosts[i].x % BLOCK_SIZE == 0 && ghosts[i].y % BLOCK_SIZE == 0) {
                        pos = ghosts[i].getScreenPos(BLOCK_SIZE, N_BLOCKS);

                        ghosts[i].moveGhost(screenData[pos]);
                    }
                    ghosts[i].updateMovement();
                }
                fixEntityPos(ghosts[i]);
                drawGhost(g2d, i);

                //detect death
                detectDeath(i);
            }
            else {
                g2d.drawImage(ghostEyes,ghosts[i].x,ghosts[i].y,this);
                ghosts[i].respawnIfReady();
            }
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
        if(score % 250 == 0 & score > 0){
            //spawn the power up
            int powerKind = numGenerator.nextInt(2);
            switch (powerKind) {
                case 0 :
                    powerList.add(new HeartPower(ghosts[0].x, ghosts[0].y, urlHeart));
                    break;
                case 1 :
                    powerList.add(new ScoreWeightPower(ghosts[0].x, ghosts[0].y, loadImage("thunder.gif")));
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
                    powerList.remove(i);
                }
            }

            //draw it
            for(int i = 0;i<powerList.size();i++) {
                drawPowerUp(g2d, powerList.get(i));
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
        N_GHOSTS = 1;
    }

    private void initLevel() {
        loadMap();
        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }
        continueLevel();
    }

    private void continueLevel() {

        int dx = 1;
        int random;
        int startGhost_x = numGenerator.nextInt(15), startGhost_y = numGenerator.nextInt(15);
        int ghostOneDimensionPos = startGhost_x + startGhost_y*N_BLOCKS;
        while(screenData[ghostOneDimensionPos] != 16){
            startGhost_x = numGenerator.nextInt(15); startGhost_y = numGenerator.nextInt(15);
            ghostOneDimensionPos = startGhost_x + startGhost_y*N_BLOCKS;

        }
        for (int i = 0; i < N_GHOSTS; i++) {
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
        while(screenData[oneDimensionPos] != 16 && oneDimensionPos == ghostOneDimensionPos){
            start_x = numGenerator.nextInt(15); start_y = numGenerator.nextInt(15);
            oneDimensionPos = start_x + start_y*N_BLOCKS;
        }
        player = new Player(start_x*BLOCK_SIZE, start_y*BLOCK_SIZE, 0, 0, PACMAN_SPEED, lives);
        dying = false;
        scoreWeight = 1;
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
            case inGame:
                playGame(g2d);
                break;
            case aboutScreen:
                showAboutScreen(g2d);
                break;
            case introScreen:
                showIntroScreen(g2d);
                break;
            case pauseScreen:
                showPauseScreen(g2d);
                break;
            case gameOver:
                showGameOverScreen(g2d);
                break;
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
                case inGame:
                    if (!player.getInput(key)) { //if player doesn't get a movement input
                        if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                            currentState = GameState.introScreen;
                        }
                    }
                    if(key == KeyEvent.VK_ESCAPE){
                        SoundPlayer.playSound("esc.wav");
                        currentState = GameState.pauseScreen;
                    }
                    break;
                case aboutScreen:
                    if(key == KeyEvent.VK_ENTER) {
                        selectedButtonIntro = 0;
                        currentState = GameState.introScreen;
                        repaint();
                    }
                    if(key == KeyEvent.VK_ESCAPE){
                        SoundPlayer.playSound("esc.wav");
                        selectedButtonIntro = 0;
                        currentState =GameState.introScreen;
                    }
                    break;
                case introScreen:
                    if (key == KeyEvent.VK_ENTER) {
                        SoundPlayer.playSound("enter.wav");
                        if(selectedButtonIntro % 3 == 0) {
                            currentState = GameState.inGame;
                            SoundPlayer.playSound("bruh.wav");
                            initGame();
                        }
                        if(selectedButtonIntro % 3 == 1){
                            currentState = GameState.aboutScreen;
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
                case pauseScreen:
                    if (key == KeyEvent.VK_ENTER) {
                        SoundPlayer.playSound("enter.wav");
                        if(selectedButtonPause % 3 == 0) {
                            currentState = GameState.inGame;
                        }
                        if(selectedButtonPause % 3 == 1){
                            currentState = GameState.inGame;
                            initGame();
                        }
                        if(selectedButtonPause % 3 == 2){
                            currentState = GameState.introScreen;
                        }
                    }
                    if (key == KeyEvent.VK_ESCAPE){
                        SoundPlayer.playSound("esc.wav");
                        currentState = GameState.inGame;
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
                case gameOver:
                    if (key == KeyEvent.VK_ENTER) {
                        SoundPlayer.playSound("enter.wav");
                        currentState = GameState.introScreen;
                        initGame();
                    }
                    else if(key == KeyEvent.VK_SPACE){
                        SoundPlayer.playSound("esc.wav");
                        currentState = GameState.introScreen;
                        initGame();
                    }
                    break;
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}
