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
    private BackgroundMusic bgm = new BackgroundMusic("bruh.wav");
    //private boolean inGame = false;
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
    private URL urlUp, urlDown, urlRight, urlLeft, urlGhostLeft, urlGhostRight, urlGhostUp, urlGhostDown, urlHeart;
    private Image heart;
    private Image titleImage;
    private Image[] startButton;
    private Image[] aboutButton;
    private Image[] exitButton;
    private int selectedButton = 0;

	
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
        aboutScreen
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

        bgm.play();
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
          //  System.out.println("Successfully wrote to the file." + highScore);
        } catch (IOException e) {
            System.out.println("An error on update high score occurred.");
            e.printStackTrace();
        }
    }

    //untuk menampilkan gambar
    private void loadImages() {
        //load url
        urlDown = loadImage("down.gif");
        urlUp = loadImage("up.gif");
        urlLeft = loadImage("left.gif");
        urlRight = loadImage("right.gif");
        urlGhostLeft = loadImage("ghostLeft.gif");
        urlGhostRight = loadImage("ghostRight.gif");
        urlGhostUp = loadImage("ghostUp.gif");
        urlGhostDown = loadImage("ghostDown.gif");
        URL urlTitle = loadImage("title.png");
        urlHeart = loadImage("heart.png");
        URL urlStart1 = loadImage("Start1.png");
        URL urlStart2 = loadImage("Start2.png");
        URL urlAbout1 = loadImage("About1.png");
        URL urlAbout2 = loadImage("About2.png");
        URL urlExit1 = loadImage("Exit1.png");
        URL urlExit2 = loadImage("Exit2.png");

        //get images
        startButton = new Image[2];
        aboutButton = new Image[2];
        exitButton = new Image[2];

        heart = new ImageIcon(urlHeart).getImage();
        titleImage = new ImageIcon(urlTitle).getImage();
        startButton[0] = new ImageIcon(urlStart1).getImage();
        startButton[1] = new ImageIcon(urlStart2).getImage();
        aboutButton[0] = new ImageIcon(urlAbout1).getImage();
        aboutButton[1] = new ImageIcon(urlAbout2).getImage();
        exitButton[0] = new ImageIcon(urlExit1).getImage();
        exitButton[1] = new ImageIcon(urlExit2).getImage();
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
            System.out.print("bangka SIR\n");
            death();

        } else {
            movePacman();
            powerUpLogic(g2d);
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
        Image aboImage,staImage,exiImage;
        //boolean staButton = false, aboButton = false,
//        String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawImage(titleImage,SCREEN_SIZE/2 - 266, SCREEN_SIZE/4, 532,96,this);
        if(selectedButton % 3 == 0){
            staImage = startButton[1];
            aboImage = aboutButton[0];
            exiImage = exitButton[0];
        }
        else if(selectedButton % 3 == 1) {
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
        //g2d.drawString(start, (SCREEN_SIZE)/4, 150);
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
           // newHighScoreb = false;
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
            currentState = GameState.gameOver;
        }

        continueLevel();
    }

    private void detectDeath(int id) {
    	//detect if pacman close to ghost with index id
        if (player.x > (ghosts[id].x - 24) && player.x < (ghosts[id].x + 24)
                && player.y > (ghosts[id].y - 24) && player.y < (ghosts[id].y + 24)
                && currentState == GameState.inGame) {
            dying = true;
            System.out.print("KENAK GHOST SIR\n");
        }
    }
    
    private void drawEntity(Graphics2D g2d, int direction, Entity en) {
    	g2d.drawImage(en.imgs[direction], en.x + 2, en.y + 2, this);
    }

    private void drawPowerUp(Graphics2D g2d, PowerUp pu){
        g2d.drawImage(pu.img, pu.x + 2, pu.y + 2, this);
    }

    private void fixEntityPos(Entity en){
        if(en.y > 672) {
            en.y = 672;
        }
        if(en.x > 672){
            en.x = 672;
        }
        if(en.x < 0){
            en.x = 0;
        }
        if(en.y < 0){
            en.y = 0;
        }
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (ghosts[i].x % BLOCK_SIZE == 0 && ghosts[i].y % BLOCK_SIZE == 0) {
                pos = ghosts[i].getScreenPos(BLOCK_SIZE, N_BLOCKS);

                ghosts[i].moveGhost(screenData[pos]);
            }
            ghosts[i].updateMovement();
            fixEntityPos(ghosts[i]);
            drawGhost(g2d, i);
            
            //detect death
            detectDeath(i);
        }
    }

    private void drawGhost(Graphics2D g2d, int id) {
    	int direction;
        if (ghosts[id].dx == -1) {
        	//draw player with image on index 0 a.k.a left
        	direction = 0;
        } else if (ghosts[id].dx == 1) {
        	direction = 1;
        } else if (ghosts[id].dy == -1) {
        	direction = 2;
        } else {
        	direction = 3;
        }
        drawEntity(g2d, direction, ghosts[id]);
    }

    private void movePacman() {

        int pos;
        short ch;

        if (player.x % BLOCK_SIZE == 0 && player.y % BLOCK_SIZE == 0) {
            pos = player.x / BLOCK_SIZE + N_BLOCKS * (player.y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
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
    	Player.Direction pFacing = player.isFacing;
        int direction;
        switch (pFacing){
            case LEFT:
                direction = 0;
                break;
            case RIGHT:
                direction = 1;
                break;
            case UP:
                direction = 2;
                break;
            default:
                direction = 3;
                break;
        }
        drawEntity(g2d, direction, player);
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
            //spawn the poweup
            int powerKind = numGenerator.nextInt(2);
            switch (powerKind) {
                case 0 :
                    powerList.add(new HeartPower(ghosts[0].x, ghosts[0].y, urlHeart));
                    break;
                case 1 :
                    powerList.add(new DoubleSpeedPower(ghosts[0].x, ghosts[0].y, loadImage("thunder.gif")));
                    break;
            }
            score+=1;
        }

        if(!powerList.isEmpty()){
            PowerUp curPower;
            //check for collision
            for(int i = 0;i<powerList.size();i++){
                curPower = powerList.get(i);
                if(curPower.isCollided(player)){
                    if(curPower instanceof HeartPower){
                        curPower.activatePower(player);
                    }
                    if(curPower instanceof DoubleSpeedPower){
                        curPower.activatePower(player);
                    }
                    powerList.remove(i);
                }
            }

            //draw it
            for(int i = 0;i<powerList.size();i++) {
                drawPowerUp(g2d, powerList.get(i));
            }
        }
//        System.out.print("DONE POWER UP SIR\n");
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
        System.out.print("DONE init SIR\n");
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
        System.out.print(String.format("go %d %d\n", startGhost_x, startGhost_y));
        for (int i = 0; i < N_GHOSTS; i++) {
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            //create new ghost with this configuration
            ghosts[i] = new Ghost(startGhost_x*BLOCK_SIZE, startGhost_y*BLOCK_SIZE, 0, dx, validSpeeds[random], urlGhostLeft, urlGhostRight, urlGhostUp, urlGhostDown);
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
        System.out.print(String.format("done %d %d\n", start_x, start_y));
        player = new Player(start_x*BLOCK_SIZE, start_y*BLOCK_SIZE, 0, 0, PACMAN_SPEED, urlLeft, urlRight, urlUp, urlDown, lives);
        dying = false;
        System.out.print("DONE continue SIR\n");
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
                    break;
                case aboutScreen:
                    if(key == KeyEvent.VK_ENTER) {
                        selectedButton = 0;
                        currentState = GameState.introScreen;
                        repaint();
                    }
                case introScreen:
                    if (key == KeyEvent.VK_ENTER) {
                        if(selectedButton % 3 == 0) {
                            currentState = GameState.inGame;
                            bgm.stop();
                            initGame();
                        }
                        if(selectedButton % 3 == 1){
                            currentState = GameState.aboutScreen;
                        }
                        if(selectedButton % 3 == 2){
                            System.exit(0);
                        }
                    }
                    if(key == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                    if (key == KeyEvent.VK_UP) {
                        selectedButton--;
                        if(selectedButton < 0) {
                            selectedButton = 2;
                        }
                    }
                    else if(key == KeyEvent.VK_DOWN) {
                        selectedButton++;
                        if(selectedButton == 3) {
                            selectedButton = 0;
                        }
                    }
                    break;
                case gameOver:
                    if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
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
