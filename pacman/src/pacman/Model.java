package pacman;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.io.File;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.Scanner;
import java.io.FileWriter;

public class Model extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private Font gamerFont = FontLoader.getFontFromFile("ARCADECLASSIC", 36f);
    //private boolean inGame = false;
    private boolean dying = false;
    private boolean newHighScoreb = false;
    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;

    private int N_GHOSTS = 1;
    private int lives, score;
    private int highScore;
    private int[] dx, dy;
    private URL urlUp, urlDown, urlRight, urlLeft, urlGhostLeft, urlGhostRight, urlGhostUp, urlGhostDown;
    private Image heart;
    private Image titleImage;
    private Image[] startButton;
    private Image[] aboutButton;
    private int selectedButton = 0;
    private int req_dx, req_dy;
	
    private Entity playerPacMan;
    private Entity[] ghosts;

    private enum GameState{
        inGame,
        introScreen,
        gameOver
    }
    private GameState currentState = GameState.introScreen;

    private final short levelData[] = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Model() {
        loadImages();
        loadHighScore();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }

    private URL loadImage(String fileName){
        return getClass().getResource("/images/" + fileName);
    }

    //untuk membuka file dan menampilkan highscore
    private static Scanner input;
    private void loadHighScore(){
        try {
            input = new Scanner(Paths.get("highscore.txt"));
            while (input.hasNextInt())
                highScore = input.nextInt();
        }
        catch (IOException ioException) {
            System.err.println("Error opening file. Terminating.");
            System.exit(1);
        }
    }

    //untuk mengupdate highscore
    private void updateHighScore(){
        try {
            FileWriter myWriter = new FileWriter("highscore.txt");
            String otp = Integer.toString(highScore);
            myWriter.write(otp);
            myWriter.close();
          //  System.out.println("Successfully wrote to the file." + highScore);
        } catch (IOException e) {
            System.out.println("An error occurred.");
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
    	URL urlHeart = loadImage("heart.png");
        URL urlStart1 = loadImage("Start1.png");
        URL urlStart2 = loadImage("Start2.png");
        URL urlAbout1 = loadImage("About1.png");
        URL urlAbout2 = loadImage("About2.png");

        //get images
        startButton = new Image[2];
        aboutButton = new Image[2];

        heart = new ImageIcon(urlHeart).getImage();
        titleImage = new ImageIcon(urlTitle).getImage();
        startButton[0] = new ImageIcon(urlStart1).getImage();
        startButton[1] = new ImageIcon(urlStart2).getImage();
        aboutButton[0] = new ImageIcon(urlAbout1).getImage();
        aboutButton[1] = new ImageIcon(urlAbout2).getImage();
    }
    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        dx = new int[4];
        dy = new int[4];

        ghosts = new Entity[MAX_GHOSTS];
        timer = new Timer(40, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        String start = "Press SPACE to start";
        Image aboImage,staImage;
        g2d.setColor(Color.yellow);
        g2d.drawImage(titleImage,SCREEN_SIZE/4, SCREEN_SIZE/4, this);
        if(selectedButton % 2 == 0){
            staImage = startButton[1];
            aboImage = aboutButton[0];
        }
        else{
            staImage = startButton[0];
            aboImage = aboutButton[1];
        }
        g2d.drawImage(staImage,SCREEN_SIZE*2/5, SCREEN_SIZE/2, this);
        g2d.drawImage(aboImage,SCREEN_SIZE*2/5, SCREEN_SIZE/2 + 35, this);
        //g2d.drawString(start, (SCREEN_SIZE)/4, 150);
    }

    private void showGameOverScreen(Graphics2D g2d){
        String gameOverString = "Game Over";
        String scoreString = "Your Score: " + score;
        String newHighScore = "Congrats for your new highscore!";
        g2d.setColor(Color.yellow);
        g2d.setFont(new Font("Monospace", Font.PLAIN, 18));
        g2d.drawString(gameOverString, (SCREEN_SIZE)/4, 150);
        g2d.drawString(scoreString, (SCREEN_SIZE)/4, 200);
        if(newHighScoreb){
            newHighScoreb = false;
            g2d.drawString(newHighScore,(SCREEN_SIZE)/4,200);
        }

        String scoreString = "Your Score  " + score;
        g2d.setColor(Color.yellow);
        g2d.setFont(gamerFont);
        g2d.drawString(gameOverString, (SCREEN_SIZE)/5, 150);
        g2d.drawString(scoreString, (SCREEN_SIZE)/5, 200);
    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void drawHighScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "High Score: " + highScore;
        g.drawString(s, SCREEN_SIZE / 2 - 60, SCREEN_SIZE + 16);
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

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

        lives--;

        if (lives == 0) {
            currentState = GameState.gameOver;
        }

        continueLevel();
    }

    private void detectDeath(int id) {
    	//detect if pacman close to ghost with index id
        if (playerPacMan.x > (ghosts[id].x - 12) && playerPacMan.x < (ghosts[id].x + 12)
                && playerPacMan.y > (ghosts[id].y - 12) && playerPacMan.y < (ghosts[id].y + 12)
                && currentState == GameState.inGame) {

            dying = true;
        }
    }
    
    private void drawEntity(Graphics2D g2d, int direction, Entity en) {
    	g2d.drawImage(en.imgs[direction], en.x + 1, en.y + 1, this);
    }

    private void fixEntityPos(Entity en){
        if(en.y > 336) {
            en.y = 336;
        }
        if(en.x > 336){
            en.x = 336;
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
        int count;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (ghosts[i].x % BLOCK_SIZE == 0 && ghosts[i].y % BLOCK_SIZE == 0) {
                pos = ghosts[i].x / BLOCK_SIZE + N_BLOCKS * (int) (ghosts[i].y / BLOCK_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && ghosts[i].dx != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghosts[i].dy != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghosts[i].dx != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghosts[i].dy != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                    	ghosts[i].dx = 0;
                    	ghosts[i].dx = 0;
                    } else {
                    	ghosts[i].dx = -ghosts[i].dx;
                    	ghosts[i].dy = -ghosts[i].dy;
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghosts[i].dx = dx[count];
                    ghosts[i].dy = dy[count];
                }

            }
            ghosts[i].updateMovement();
//            fixEntityPos(ghosts[i]);
            drawGhost(g2d, i);
            
            //detect death
            detectDeath(i);
        }
    }

    private void drawGhost(Graphics2D g2d, int id) {
    	int direction = -1;
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

        if (playerPacMan.x % BLOCK_SIZE == 0 && playerPacMan.y % BLOCK_SIZE == 0) {
            pos = playerPacMan.x / BLOCK_SIZE + N_BLOCKS * (int) (playerPacMan.y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }
            if(highScore < score){
                highScore = score;
                updateHighScore();
                newHighScoreb = true;

            }
            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                	playerPacMan.dx = req_dx;
                	playerPacMan.dy = req_dy;
                }
            }

            // Check for standstill
            if ((playerPacMan.dx == -1 && playerPacMan.dy == 0 && (ch & 1) != 0)
                    || (playerPacMan.dx == 1 && playerPacMan.dy == 0 && (ch & 4) != 0)
                    || (playerPacMan.dx == 0 && playerPacMan.dy == -1 && (ch & 2) != 0)
                    || (playerPacMan.dx == 0 && playerPacMan.dy == 1 && (ch & 8) != 0)) {
            	playerPacMan.dx = 0;
            	playerPacMan.dy = 0;
            }
        }
        playerPacMan.updateMovement();
        fixEntityPos(playerPacMan);
    }

    private void drawPacman(Graphics2D g2d) {
    	int direction = -1;
        if (req_dx == -1) {
        	//draw player with image on index 0 a.k.a left
        	direction = 0;
        } else if (req_dx == 1) {
        	direction = 1;
        } else if (req_dy == -1) {
        	direction = 2;
        } else {
        	direction = 3;
        }
        drawEntity(g2d, direction, playerPacMan);
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(4));

                if ((levelData[i] == 0)) {
                    //g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
                }

                i++;
            }
        }
    }

    private void initGame() {

        lives = 3;
        score = 0;
        initLevel();
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }
            //create new ghost with this configuration
            ghosts[i] = new Entity(4 * BLOCK_SIZE, 4 * BLOCK_SIZE, 0, dx, validSpeeds[random], urlGhostLeft, urlGhostRight, urlGhostUp, urlGhostDown);
            dx = -dx;
        }
        //create new player
        playerPacMan = new Entity(7 * BLOCK_SIZE, 11 * BLOCK_SIZE, 0, 0, PACMAN_SPEED, urlLeft, urlRight, urlUp, urlDown);
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
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
                    if (key == KeyEvent.VK_LEFT) {
                        req_dx = -1;
                        req_dy = 0;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        req_dx = 1;
                        req_dy = 0;
                    } else if (key == KeyEvent.VK_UP) {
                        req_dx = 0;
                        req_dy = -1;
                    } else if (key == KeyEvent.VK_DOWN) {
                        req_dx = 0;
                        req_dy = 1;
                    } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                        currentState = GameState.introScreen;
                    }
                    break;
                case introScreen:
                    if (key == KeyEvent.VK_SPACE) {
                        if(selectedButton % 2 == 0) {
                            currentState = GameState.inGame;
                            initGame();
                        }
                        else{
                            //about di sini
                        }
                    }
                    if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                        selectedButton ++;
                    }
                    break;
                case gameOver:
                    if (key == KeyEvent.VK_SPACE) {
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
