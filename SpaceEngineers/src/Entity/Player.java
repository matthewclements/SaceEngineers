package Entity;

import TileMap.*;
import Audio.AudioPlayer;

import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Player extends MapObject {

	// player stuff
	private int health;
	private int maxHealth;
	private int jetFuel;
	private int maxFuel;
	private boolean dead;
	private boolean flinching;
	private long flinchTimer;

	// jetpack
	private boolean jetpack;	
	private int jetCost;

	// laser
	private boolean firing;
	private int laserDamage;
	private ArrayList<Laser> lasers;

	// animations
	private ArrayList<BufferedImage[]> sprites;
	private final int[] numFrames = { 3, 3, 1, 1, 1, 1 };

	// animations actions
	private static final int LASER = 0;
	private static final int WALKING = 1;
	private static final int IDLE = 2;
	private static final int JUMPING = 3;
	private static final int FALLING = 3;
	private static final int JUMPLASER = 4;
	private static final int JETPACK = 5;

	private HashMap<String, AudioPlayer> sfx;

	public Player(TileMap tm) {
		super(tm);

		width = 36; // width between sprites on sheet - including sprite
		height = 34; // height between sprites on sheet - including sprite
		cwidth = 20; // width of sprite in game
		cheight = 20; // height of sprite in game

		moveSpeed = 0.3;
		maxSpeed = 1.6;
		stopSpeed = 0.4;
		fallSpeed = 0.15;
		maxFallSpeed = 4.0;
		jumpStart = -4.8;
		stopJumpSpeed = 0.3;

		facingRight = true;

		health = maxHealth = 5;
		jetFuel = maxFuel = 25000;

		jetCost = 50;

		laserDamage = 5;
		lasers = new ArrayList<Laser>();

		// load sprites
		try {
			BufferedImage spritesheet = ImageIO.read(getClass()
					.getResourceAsStream("/Sprites/Player/space2.png"));

			sprites = new ArrayList<BufferedImage[]>();

			for (int i = 0; i < numFrames.length; i++) {
				BufferedImage[] bi = new BufferedImage[numFrames[i]];
				for (int j = 0; j < numFrames[i]; j++) {

					if (i == 0) {
						if (j == 2) {
							bi[j] = spritesheet.getSubimage(80, i * height, 45,
									height);
						} else {
							bi[j] = spritesheet.getSubimage(j * width, i
									* height, width, height);
						}

					} else if (i == 1) {
						bi[j] = spritesheet.getSubimage(j * width, i * height,
								width, height);
					} else if (i == 2) {
						bi[j] = spritesheet.getSubimage(1 * width, 77, width,
								height);
					} else if (i == 3) {
						bi[j] = spritesheet.getSubimage(69, 113, width, 40);
					} else if (i == 4) {
						bi[j] = spritesheet.getSubimage(44, 159, 43, 41);
					} else if (i == 5) {
						bi[j] = spritesheet.getSubimage(7, 200, 35, 40);
					}
				}

				sprites.add(bi);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		animation = new Animation();
		currentAction = IDLE;
		animation.setFrames(sprites.get(IDLE));
		animation.setDelay(400);

		sfx = new HashMap<String, AudioPlayer>();
		sfx.put("jump", new AudioPlayer("/SFX/jump.mp3"));
		sfx.put("scratch", new AudioPlayer("/SFX/scratch.mp3"));

	}

	public int getHealth() {
		return health;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getFuel() {
		return jetFuel;
	}

	public int getMaxFire() {
		return maxFuel;
	}

	public void setFiring() {
		if (!jetpack) {
			firing = true;
		} else {
			firing = false;
		}

	}

	public void setJetpack(boolean b) {
		jetpack = b;
	}

	public void checkAttack(ArrayList<Enemy> enemies) {

		// loop though enemies
		for (int i = 0; i < enemies.size(); i++) {

			Enemy e = enemies.get(i);

			// lasers
			for (int j = 0; j < lasers.size(); j++) {
				if (lasers.get(j).intersects(e)) {
					e.hit(laserDamage);
					lasers.get(j).setHit();
					break;
				}
			}

			// check for enemy collision
			if (intersects(e)) {
				hit(e.getDamage());
			}
		}
	}

	public void hit(int damage) {
		if (flinching)
			return;
		health -= damage;
		if (health < 0)
			health = 0;
		if (health == 0)
			dead = true;
		flinching = true;
		flinchTimer = System.nanoTime();
	}

	private void getNextPosition() {

		// movement
		if (left) {
			dx -= moveSpeed;
			if (dx < -maxSpeed) {
				dx = -maxSpeed;
			}
		} else if (right) {
			dx += moveSpeed;
			if (dx > maxSpeed) {
				dx = maxSpeed;
			}
		} else {
			if (dx > 0) {
				dx -= stopSpeed;
				if (dx < 0) {
					dx = 0;
				}
			} else if (dx < 0) {
				dx += stopSpeed;
				if (dx > 0) {
					dx = 0;
				}
			}
		}

		// cannot move while attacking
		if ((currentAction == LASER) && !(jumping || falling)) {
			dx = 0;
		}

		// jumping
		if (jumping && !falling) {
			sfx.get("jump").play();
			dy = jumpStart;
			falling = true;
		}

		// falling
		if (falling) {

			if (dy > 0 && jetpack && (jetFuel > 0))
				dy += jumpStart * 0.7;
			else
				dy += fallSpeed;

			dy += fallSpeed;

			if (dy > 0)
				jumping = false;
			if (dy < 0 && !jumping)
				dy += stopJumpSpeed;

			if (dy > maxFallSpeed)
				dy = maxFallSpeed;

		}
	}

	public void update() {

		// update position
		getNextPosition();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);

		// check attack has stopped
		if (currentAction == LASER || currentAction == JUMPLASER) {
			if (animation.hasPlayedOnce())
				firing = false;
		}

		// laser attack
		if (firing && currentAction != LASER) {
			Laser l = new Laser(tileMap, facingRight);
			if (facingRight) {
				l.setPosition(x + 20, y);
			} else {
				l.setPosition(x - 20, y);
			}
			lasers.add(l);
		}

		// update lasers
		for (int i = 0; i < lasers.size(); i++) {
			lasers.get(i).update();
			if (lasers.get(i).shouldRemove()) {
				lasers.remove(i);
				i--;
			}
		}
		
		// update jetpack
		jetFuel += 5;
		if(jetFuel > maxFuel) jetFuel = maxFuel;
		if(jetpack){
			jetFuel -= jetCost;
		}
		if(jetFuel <= 0) jetFuel = 0;

		// check done flinching
		if (flinching) {
			long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
			if (elapsed > 1000) {
				flinching = false;
			}
		}

		// set animatioin
		if (firing) {
			if (dy == 0) {
				if (currentAction != LASER) {
					currentAction = LASER;
					animation.setFrames(sprites.get(LASER));
					animation.setDelay(50);
					// width = 36;
				}
			} else {
				if (currentAction != JUMPLASER) {
					currentAction = JUMPLASER;
					animation.setFrames(sprites.get(JUMPLASER));
					animation.setDelay(50);
					// width = 41;
				}
			}
		} else if (dy > 0) {

			if (jetpack && (jetFuel > 0)) {
				if (currentAction != JETPACK) {
					currentAction = JETPACK;
					animation.setFrames(sprites.get(JETPACK));
					// animation.setDelay(100);
					width = 35;
				}
			} else if (currentAction != FALLING) {
				currentAction = FALLING;
				animation.setFrames(sprites.get(FALLING));
				animation.setDelay(100);
				width = 36;
			}
		} else if (dy < 0) {
			if (currentAction != JUMPING) {
				currentAction = JUMPING;
				animation.setFrames(sprites.get(JUMPING));
				animation.setDelay(-1);
				width = 36;
			}
		} else if (left || right) {
			if (currentAction != WALKING) {
				currentAction = WALKING;
				animation.setFrames(sprites.get(WALKING));
				animation.setDelay(40);
				width = 36;
			}
		} else {
			if (currentAction != IDLE) {
				currentAction = IDLE;
				animation.setFrames(sprites.get(IDLE));
				animation.setDelay(400);
				width = 36;
			}
		}

		animation.update();

		// set direction
		if (currentAction != LASER) {
			if (right)
				facingRight = true;
			if (left)
				facingRight = false;
		}
	}

	public void draw(Graphics2D g) {

		setMapPosition();

		// draw lasers
		for (int i = 0; i < lasers.size(); i++) {
			lasers.get(i).draw(g);
		}

		// draw player
		if (flinching) {
			long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
			if (elapsed / 100 % 2 == 0) {
				return;
			}
		}

		super.draw(g);
	}

}
