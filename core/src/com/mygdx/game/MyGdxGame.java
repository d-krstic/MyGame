package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	private OrthographicCamera camera;
	private Rectangle knight;
	private Texture knightImage;
	private Array<Rectangle> enemies;
	private Texture enemyImage;
	private Array<Rectangle> coins;
	private Texture coinImage;
	private Texture heartImage;
	private long lastEnemyTime;
	private long lastCoinTime;
	private Sound coinSound;
	private Sound hitSound;

	public BitmapFont font;

	// static variables
	private static int HP = 3;
	private static int COLLECTED_COINS = 0;
	private static final long CREATE_ENEMY_TIME = 2000000000;    // ns
	private static final long CREATE_COIN_TIME = 2000000000;    // ns
	private static int SPEED_ENEMY = 50;    // pixels per second
	private static int SPEED_COIN = 100;    // pixels per second
	private static int SPEED = 80;
	
	@Override
	public void create () {
		font = new BitmapFont();
		font.getData().setScale(2);

		// load textures
		img = new Texture(Gdx.files.internal("badlogic.jpg"));
		knightImage = new Texture(Gdx.files.internal("knight.png"));
		enemyImage = new Texture(Gdx.files.internal("enemy.png"));
		coinImage = new Texture(Gdx.files.internal("coin.png"));
		heartImage = new Texture(Gdx.files.internal("heart.png"));
		coinSound = Gdx.audio.newSound(Gdx.files.internal("coin_pickup.mp3"));
		hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();

		// create knight rectangle
		knight = new Rectangle();
		knight.x = 20; // 20 pixels from left edge
		knight.y = 20;
		knight.width = knightImage.getWidth();
		knight.height = knightImage.getHeight();

		enemies = new Array<Rectangle>();
		coins = new Array<Rectangle>();
		spawnCoin();
		spawnEnemy();
	}

	@Override
	public void render () {
		// clear screen
		ScreenUtils.clear((float) 0.79, 1, (float)0.79, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// check if game is over
		if(HP == 0) {
			batch.begin();
			font.setColor(Color.RED);
			font.draw(batch, "GAME OVER", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
			batch.end();
			return;
		}

		// check for user input
		if (Gdx.input.isKeyPressed(Input.Keys.A)) commandMoveLeft();
		if (Gdx.input.isKeyPressed(Input.Keys.D)) commandMoveRight();
		if (Gdx.input.isKeyPressed(Input.Keys.S)) commandMoveDown();
		if (Gdx.input.isKeyPressed(Input.Keys.W)) commandMoveUp();

		// check if we need to create a new enemy/coin
		if (TimeUtils.nanoTime() - lastEnemyTime > CREATE_ENEMY_TIME) spawnEnemy();
		if (TimeUtils.nanoTime() - lastCoinTime > CREATE_COIN_TIME) spawnCoin();

		// move enemies and coins, also removes ones off screen
		for (Iterator<Rectangle> it = enemies.iterator(); it.hasNext(); ) {
			Rectangle enemy = it.next();
			enemy.x -= SPEED_ENEMY * Gdx.graphics.getDeltaTime();
			if (enemy.x + enemyImage.getHeight() < 0) {
				it.remove();
			}
			if (enemy.overlaps(knight)) {
				HP--;
				it.remove();
				hitSound.play();
			}
		}

		for (Iterator<Rectangle> it = coins.iterator(); it.hasNext(); ) {
			Rectangle coin = it.next();
			coin.x -= SPEED_COIN * Gdx.graphics.getDeltaTime();
			if (coin.x + coinImage.getHeight() < 0) {
				it.remove();
			}
			if (coin.overlaps(knight)) {
				COLLECTED_COINS++;
				coinSound.play();
				it.remove();
			}
		}

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the rocket, astronauts, asteroids
		batch.begin();
		{
			for (Rectangle e : enemies) {
				batch.draw(enemyImage, e.x, e.y);
			}
			for (Rectangle c : coins) {
				batch.draw(coinImage, c.x, c.y);
			}
			batch.draw(knightImage, knight.x, knight.y);

			// draws number of collected coins and hearts
			font.setColor(Color.YELLOW);
			font.draw(batch, "" + COLLECTED_COINS, Gdx.graphics.getWidth() - 50, Gdx.graphics.getHeight() - 20);
			for(int i = 0; i < HP; i++) {
				batch.draw(heartImage, i*35, Gdx.graphics.getHeight()-35);
			}
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		font.dispose();
		knightImage.dispose();
		enemyImage.dispose();
		coinImage.dispose();
		coinSound.dispose();
		hitSound.dispose();
	}

	// adds an enemy to array of enemies
	public void spawnEnemy() {
		Rectangle enemy = new Rectangle();
		enemy.x = Gdx.graphics.getWidth();
		enemy.y = MathUtils.random(0, Gdx.graphics.getHeight() - enemyImage.getHeight());
		enemy.width = enemyImage.getWidth();
		enemy.height = enemyImage.getHeight();
		enemies.add(enemy);
		lastEnemyTime = TimeUtils.nanoTime();
	}

	// adds a coin to array of coins
	public void spawnCoin() {
		Rectangle coin = new Rectangle();
		coin.x = Gdx.graphics.getWidth();
		coin.y = MathUtils.random(0, Gdx.graphics.getHeight() - coinImage.getHeight());
		coin.width = coinImage.getWidth();
		coin.height = coinImage.getHeight();
		coins.add(coin);
		lastCoinTime = TimeUtils.nanoTime();
	}

	public void commandMoveLeft() {
		knight.x -= SPEED * Gdx.graphics.getDeltaTime();
		if (knight.x < 0) knight.x = 0;
	}

	public  void commandMoveRight() {
		knight.x += SPEED * Gdx.graphics.getDeltaTime();
		if (knight.x > Gdx.graphics.getWidth() - knightImage.getWidth())
			knight.x = Gdx.graphics.getWidth() - knightImage.getWidth();
	}

	public void commandMoveDown() {
		knight.y -= SPEED * Gdx.graphics.getDeltaTime();
		if (knight.y < 0) knight.y = 0;
	}

	public  void commandMoveUp() {
		knight.y += SPEED * Gdx.graphics.getDeltaTime();
		if (knight.y > Gdx.graphics.getHeight() - knightImage.getHeight())
			knight.y = Gdx.graphics.getHeight() - knightImage.getHeight();
	}
}
