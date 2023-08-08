package edu.hanu.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
    // use to draw Texture in the UI
    SpriteBatch batch;
    Texture background, gbackground, cbackground, nbackground, rbackground;
    int changeBackground = 0;
    Texture[] birds;

    // animating
    int flapState = 0;

    // moving up & down
    float birdY = 0, velocity = 0, gravity = 2;
    int gameState = 0;

    // pipes
    Texture topTube, bottomTube;
    float gap = 600, maxTubeOffset, tubeVelocity = 8, distanceBetweenTubes;
    Random randomGenerator;
    int numberOfTubes = 4;
    float[] tubeX = new float[numberOfTubes];
    float[] tubeOffset = new float[numberOfTubes];

    // shapes
    ShapeRenderer shapeRenderer;
    Circle birdCircle;
    Rectangle topTubeRectangle, bottomTubeRectangle;
    Rectangle restartRectangle;

    // scoring
    BitmapFont font;
    int score = 0, scoringTube = 0;

    // game over
    Texture gameOver, banner;
    BitmapFont past, current;

    // sounds
    Sound jumpSound, bonkSound;

    // restart
    Texture restart;

    // high score
    Preferences preferences;
    int highScore = 0;

    // share
    int screenHeight, screenWidth;
    float halfScreenWidth, halfScreenHeight;

    @Override
    public void create() {
        batch = new SpriteBatch();
        screenHeight = Gdx.graphics.getHeight();
        screenWidth = Gdx.graphics.getWidth();
        halfScreenWidth = screenWidth / 2f;
        halfScreenHeight = screenHeight / 2f;

        background = new Texture("bg.png");
        gbackground = new Texture("garden.jpg");
        cbackground = new Texture("city.png");
        nbackground = new Texture("night.jpg");
        rbackground = new Texture("rune.jpg");

        // animating
        birds = new Texture[2];
        birds[0] = new Texture("bird.png");
        birds[1] = new Texture("bird2.png");

        // pipes
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");

        maxTubeOffset = halfScreenHeight - gap / 2 - 100;
        randomGenerator = new Random();

        // setups
        distanceBetweenTubes = screenWidth * 0.75f;
        birdY = halfScreenHeight - birds[flapState].getHeight() / 2f;
        for (int i = 0; i < numberOfTubes; i++) {
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * maxTubeOffset;
            tubeX[i] = halfScreenWidth - topTube.getWidth() / 2f + i * distanceBetweenTubes + screenWidth;
        }

        // shapes
        shapeRenderer = new ShapeRenderer();
        birdCircle = new Circle();
        topTubeRectangle = new Rectangle();
        bottomTubeRectangle = new Rectangle();

        // scoring
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(10);

        // game over
        gameOver = new Texture("gameover.png");
        banner = new Texture("wood_banner.png");

        // sounds
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jumping.mp3"));
        bonkSound = Gdx.audio.newSound(Gdx.files.internal("bonk.mp3"));

        // restart
        restart = new Texture("restart.png");
        restartRectangle = new Rectangle();

        // high score
        preferences = Gdx.app.getPreferences("bird");
        preferences.flush();
        highScore = preferences.getInteger("highScore", 0);

        past = new BitmapFont();
        past.setColor(Color.WHITE);
        past.getData().setScale(5);
        current = new BitmapFont();
        current.setColor(Color.WHITE);
        current.getData().setScale(5);
    }

    @Override
    public void render() {
        batch.begin();

        // start drawing
        if (changeBackground == 0) batch.draw(background, 0, 0, screenWidth, screenHeight);
        else if (changeBackground == 1) batch.draw(gbackground, 0, 0, screenWidth, screenHeight);
        else if (changeBackground == 2) batch.draw(cbackground, 0, 0, screenWidth, screenHeight);
        else if (changeBackground == 3) batch.draw(rbackground, 0, 0, screenWidth, screenHeight);
        else if (changeBackground == 4) batch.draw(nbackground, 0, 0, screenWidth, screenHeight);

        // animating
        if (flapState == 0 && gameState != 3) flapState = 1;
        else flapState = 0;
        batch.draw(birds[flapState], halfScreenWidth - birds[flapState].getWidth() / 2f, birdY);

        // the game states
        if (gameState == 1) { // game playing
            // moving up & down
            if (Gdx.input.justTouched()) {
                velocity = -20;
                jumpSound.play(1f, 1f, 0f);
            }

            // pipes
            for (int i = 0; i < numberOfTubes; i++) {
                // replace ube
                if (tubeX[i] < -topTube.getWidth()) {
                    tubeX[i] += numberOfTubes * distanceBetweenTubes;
                } else {
                    tubeX[i] -= tubeVelocity;
                }
                batch.draw(topTube, tubeX[i], halfScreenHeight + gap / 2 + tubeOffset[i]);
                batch.draw(bottomTube, tubeX[i], halfScreenHeight - gap / 2 - bottomTube.getHeight() + tubeOffset[i]);
            }
            if (birdY > screenHeight) birdY = screenHeight;
            if (birdY > 0 || velocity < 0) {
                velocity += gravity;
                birdY -= velocity;
                if (birdY + birds[0].getHeight() > screenHeight)
                    birdY = screenHeight - birds[0].getHeight();
                if (birdY < 0) birdY = 0;
            }

            // scoring
            if (tubeX[scoringTube] < halfScreenWidth - topTube.getWidth()) {
                score++;
                scoringTube++;
                if (scoringTube == numberOfTubes) {
                    scoringTube = 0;
                }
            }

            // shapes
//			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//			shapeRenderer.setColor(Color.RED);

            // bird circle
            birdCircle.set(halfScreenWidth, birdY + birds[flapState].getHeight() / 2f, birds[flapState].getWidth() / 2f - 10);
//			shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius);

            // scoring tube rectangle
            topTubeRectangle.set(tubeX[scoringTube], halfScreenHeight + gap / 2 + tubeOffset[scoringTube], topTube.getWidth(), topTube.getHeight());
            bottomTubeRectangle.set(tubeX[scoringTube], halfScreenHeight - gap / 2 - bottomTube.getHeight() + tubeOffset[scoringTube], bottomTube.getWidth(), bottomTube.getHeight());
//			shapeRenderer.rect(topTubeRectangle.x, topTubeRectangle.y, topTubeRectangle.width, topTubeRectangle.height);
//			shapeRenderer.rect(bottomTubeRectangle.x, bottomTubeRectangle.y, bottomTubeRectangle.width, bottomTubeRectangle.height);
//			shapeRenderer.end();

            // TODO: collision detection
            if (Intersector.overlaps(birdCircle, topTubeRectangle) || Intersector.overlaps(birdCircle, bottomTubeRectangle)) {
                gameState = 3;
                bonkSound.play(0.5f, 1f, 0f);
            }
            // TODO: bird falling down
            if (birdY == 0) {
                gameState = 3;
                bonkSound.play(0.5f, 1f, 0f);
            }
        } else if (gameState == 3) {
            // keep tubes
            batch.draw(topTube, tubeX[scoringTube], halfScreenHeight + gap / 2 + tubeOffset[scoringTube]);
            batch.draw(bottomTube, tubeX[scoringTube], halfScreenHeight - gap / 2 - bottomTube.getHeight() + tubeOffset[scoringTube]);
            if (scoringTube - 1 >= 0) {
                batch.draw(topTube, tubeX[scoringTube - 1], halfScreenHeight + gap / 2 + tubeOffset[scoringTube - 1]);
                batch.draw(bottomTube, tubeX[scoringTube - 1], halfScreenHeight - gap / 2 - bottomTube.getHeight() + tubeOffset[scoringTube - 1]);
            }
            if (scoringTube + 1 < numberOfTubes) {
                batch.draw(topTube, tubeX[scoringTube + 1], halfScreenHeight + gap / 2 + tubeOffset[scoringTube + 1]);
                batch.draw(bottomTube, tubeX[scoringTube + 1], halfScreenHeight - gap / 2 - bottomTube.getHeight() + tubeOffset[scoringTube + 1]);
            }
            // banner
            batch.draw(banner, halfScreenWidth - banner.getWidth() / 2f,
                    halfScreenHeight - banner.getHeight() / 2f);

            // game over
            batch.draw(gameOver, halfScreenWidth - gameOver.getWidth() / 2f,
                    halfScreenHeight - gameOver.getHeight() / 2f + 200);
            // restart
            batch.draw(restart, halfScreenWidth + 200,
                    halfScreenHeight - restart.getHeight() / 2f - 40);
            if (highScore < score) {
                highScore = score;
                preferences.putInteger("highScore", score);
                preferences.flush();
            }
            past.draw(batch, "High score: " + highScore, 200, 950);
            current.draw(batch, "Your score: " + score, 200, 850);

            restartRectangle.set(halfScreenWidth + 200,
                    halfScreenHeight - restart.getHeight() / 2f - 40, restart.getWidth(), restart.getHeight());
            // high score
            if (Gdx.input.justTouched()) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                if (restartRectangle.contains(touchPos.x, touchPos.y)) {
                    gameState = 2;
                    changeBackground++;
                    if (changeBackground == 5) changeBackground = 0;
                    // reset config
                    birdY = halfScreenHeight - birds[flapState].getHeight() / 2f;
                    for (int i = 0; i < numberOfTubes; i++) {
                        tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * maxTubeOffset;
                        tubeX[i] = halfScreenWidth - topTube.getWidth() / 2f + i * distanceBetweenTubes + screenWidth;
                    }
                    scoringTube = 0;
                    score = 0;
                }
            }
        } else { // game start
            if (Gdx.input.justTouched()) {
                gameState = 1;
                velocity = -20;
                jumpSound.play(1f, 1f, 0f);
            }
        }

        // TODO: scoring
        font.draw(batch, score + "", 100, 200);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        gbackground.dispose();
        rbackground.dispose();
        cbackground.dispose();
        nbackground.dispose();
        jumpSound.dispose();
        bonkSound.dispose();
    }
}
