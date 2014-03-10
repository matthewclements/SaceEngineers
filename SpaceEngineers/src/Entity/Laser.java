package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import TileMap.TileMap;

public class Laser extends MapObject {

	private boolean hit;
	private boolean remove;
	private BufferedImage[] sprites;
	private BufferedImage[] hitSprites;

	public Laser(TileMap tm, boolean right) {
		super(tm);
		
		facingRight = right;

		moveSpeed = 3.8;
		if (right)
			dx = moveSpeed;
		else
			dx = -moveSpeed;

		width = 30;
		height = 30;
		cwidth = 14;
		cheight = 14;

		// load sprites
		try {

			BufferedImage spritesheet = ImageIO.read(getClass()
					.getResourceAsStream("/Sprites/Player/space2.png"));
			
			sprites = new BufferedImage[1];
			sprites[0] = spritesheet.getSubimage(128, 0, 27, 34);

			animation = new Animation();
			animation.setFrames(sprites);
			animation.setDelay(70);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setHit() {
		if (hit)
			return;
		hit = true;
		animation.setDelay(70);
		dx = 0;
	}

	public boolean shouldRemove() {
		return remove;
	}

	public void update() {

		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		if(dx == 0 && !hit){
			setHit();
		}

		animation.update();
		if (hit && animation.hasPlayedOnce()) {
			remove = true;
		}
	}

	public void draw(Graphics2D g) {

		setMapPosition();

		super.draw(g);
	}

}
