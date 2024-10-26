import tester.*;
import javalib.funworld.*;
import javalib.worldimages.*;
import java.awt.*;

// to represent the player
class PlayerShip {
  int posX; // x-coordinate position
  int posY; // y-coordinate position

  PlayerShip(int posX, int posY) {
    this.posX = posX;
    this.posY = posY;
  }

  /* Template
  Fields:
  ... this.posX ... int
  ... this.posY ... int
  Methods:
  ... this.drawShip(WorldScene scene) ... WorldScene
  ... this.moveShip(String key) ... PlayerShip
  Methods on Fields:
  ... this.player.drawShip(scene) ... WorldScene
  ... this.player.moveShip("left") ... PlayerShip
  ... this.player.moveShip("right") ... PlayerShip
   */

  // draws the ship onto the scene
  WorldScene drawShip(WorldScene scene) {
    return scene.placeImageXY(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE), this.posX, this.posY);
  }

  // player can move the ship either "left" or "right" on the scene
  PlayerShip moveShip(String key) {
    if (key.equals("left") && this.posX >= 45) {
      return new PlayerShip(this.posX - 15, this.posY);
    } else if (key.equals("right") && this.posX <= 755) {
      return new PlayerShip(this.posX + 15, this.posY);
    }
    return this;
  }
}

// interface for a list of bullets
interface ILoBullet {
  WorldScene drawLoBullet(WorldScene scene); // draws the list of bullets onto the scene
  ILoBullet moveAllBullets(); // bullets all move on the scene
  ILoBullet addBullet(Bullet bullet); // adds a bullet to the list of bullet when shot
  ILoBullet removeBullet(ILoAlien aliens); // removes the bullet when collided with the alien
  boolean hasHit(Alien alien); // did the bullet hit the alien?
}

// to represent a list of bullet
class ConsLoBullet implements ILoBullet {
  Bullet first;
  ILoBullet rest;

  ConsLoBullet(Bullet first, ILoBullet rest) {
    this.first = first;
    this.rest = rest;
  }

  /* Template
  Fields:
  ... this.first ... Bullet
  ... this.rest ... ILoBullet
  Methods:
  ... this.drawLoBullet(WorldScene scene) ... WorldScene
  ... this.moveAllBullets() ... ILoBullet
  ... this.addBullet(Bullet bullet) ... ILoBullet
  ... this.removeCollided(ILoAlien aliens) ... ILoBullet
  ... this.hasHit(Alien alien) ... boolean
  Methods on Fields:
  ... this.first.drawLoBullet(scene) ... WorldScene
  ... this.first.moveAllBullets() ... WorldScene
  ... this.rest.removeCollided(aliens) ... ILoBullet
  ... this.rest.hasHit(alien) ... boolean
   */

  public WorldScene drawLoBullet(WorldScene scene) {
    return this.rest.drawLoBullet(this.first.drawBullet(scene));
  }

  public ILoBullet moveAllBullets() {
    return new ConsLoBullet(this.first.moveBullet(), this.rest.moveAllBullets());
  }

  public ILoBullet addBullet(Bullet bullet) {
    return new ConsLoBullet(bullet, this);
  }

  public ILoBullet removeBullet(ILoAlien aliens) {
    if (aliens.hasHit(this.first)) {
      return this.rest.removeBullet(aliens);
    } else {
      return new ConsLoBullet(this.first, this.rest.removeBullet(aliens));
    }
  }

  public boolean hasHit(Alien alien) {
    return this.first.isCollision(alien) || this.rest.hasHit(alien);
  }
}

// to represent an empty list of bullet
class MtLoBullet implements ILoBullet {
  MtLoBullet() {}

  /* Template
  Fields:
  Methods:
  ... this.drawLoBullet(WorldScene scene) ... WorldScene
  ... this.moveAllBullets() ... ILoBullet
  ... this.addBullet(Bullet bullet) ... ILoBullet
  ... this.removeCollided(ILoAlien aliens) ... ILoBullet
  ... this.hasHit(Alien alien) ... boolean
  Methods on Fields:
   */

  public WorldScene drawLoBullet(WorldScene scene) {
    return scene;
  }

  public ILoBullet moveAllBullets() {
    return this;
  }

  public ILoBullet addBullet(Bullet bullet) {
    return new ConsLoBullet(bullet, this);
  }

  public ILoBullet removeBullet(ILoAlien aliens) {
    return this;
  }

  public boolean hasHit(Alien alien) {
    return false;
  }
}

// to represent a bullet
class Bullet {
  int posX;
  int posY;
  boolean isFired; // did we shoot the bullet?

  Bullet(int posX, int posY, boolean isFired) {
    this.posX = posX;
    this.posY = posY;
    this.isFired = isFired;
  }

  /* Template
  Fields:
  ... this.posX ... int
  ... this.posY ... int
  ... this.isFired ... boolean
  Methods:
  ... this.shoot(String key, int playerPosX, int playerPosY) ... Bullet
  ... this.moveBullet() ... Bullet
  ... this.drawBullet(WorldScene scene) ... WorldScene
  ... this.isCollision(Alien alien) ... boolean
  Methods on Fields:
  ... this.bullet.shoot(" ", this.player.posX, this.player.posY) ... Bullet
  ... this.bullet.moveBullet() ... Bullet
  ... this.bullet.drawBullet(scene) ... WorldScene
  ... this.bullet.isCollision(this.first) ... boolean
   */

  // shoots the bullet
  public Bullet shoot(String key, int playerPosX, int playerPosY) {
    if (key.equals(" ") && !this.isFired) {
      return new Bullet(playerPosX, playerPosY, true);
    }
    return this;
  }

  // bullet moves if shot
  public Bullet moveBullet() {
    if (this.isFired) {
      return new Bullet(this.posX, this.posY - 60, true);
    }
    return this;
  }

  // draws the bullet onto the scene if shot
  public WorldScene drawBullet(WorldScene scene) {
    if (this.isFired) {
      return scene.placeImageXY(new RectangleImage(20, 40, OutlineMode.SOLID, Color.yellow), this.posX, this.posY);
    }
    return scene;
  }

  // did the bullet collide into the alien's hit-box?
  public boolean isCollision(Alien alien) {
    return Math.abs(this.posX - alien.posX) <= 20 && Math.abs(this.posY - alien.posY) <= 30;
  }
}

// an interface for list of alien
interface ILoAlien {
  WorldScene drawLoAlien(WorldScene scene); // draws the list of aliens onto the scene
  ILoAlien moveAllAlien(); // moves all the aliens on the scene
  ILoAlien aliensOnEdge(); // checks if the aliens are on the edge of the scene
  ILoAlien removeCollided(ILoBullet bullets); // removes the aliens if shot
  boolean hasHit(Bullet bullet); // did the aliens get shot?
  int countOnHits(ILoBullet bullets); // counts the aliens that were shot
  boolean hasAlienReachedBottom(); // did the alien reach the bottom of the scene?
}

// to represent a list of alien
class ConsLoAlien implements ILoAlien {
  Alien first;
  ILoAlien rest;

  ConsLoAlien(Alien first, ILoAlien rest) {
    this.first = first;
    this.rest = rest;
  }

    /* Template
  Fields:
  ... this.first ... Alien
  ... this.rest ... ILoAlien
  Methods:
  ... this.drawLoAlien(WorldScene scene) ... WorldScene
  ... this.moveAllAlien() ... ILoAlien
  ... this.aliensOnEdge() ... ILoAlien
  ... this.removeCollided(ILoBullet bullets) ... ILoAlien
  ... this.hasHit(Bullet bullet) ... boolean
  ... this.countOnHits(ILoBullet bullets) ... int
  ... this.hasAliensReachedBottom() ... boolean
  Methods on Fields:
  ... this.first.drawLoAlien(scene) ... WorldScene
  ... this.rest.moveAllAlien() ... ILoAlien
  ... this.first.aliensOnEdge() ... ILoAlien
  ... this.rest.removeCollided(bullets) ... ILoAlien
  ... this.bullet.hasHit(this.first) ... boolean
  ... this.rest.countOnHits(bullets) ... int
  ... this.rest.hasAliensReachedBottom() ... boolean
   */

  public WorldScene drawLoAlien(WorldScene scene) {
    return this.rest.drawLoAlien(this.first.drawAlien(scene));
  }

  public ILoAlien moveAllAlien() {
    return new ConsLoAlien(this.first.moveAlien(), this.rest.moveAllAlien());
  }

  public ILoAlien aliensOnEdge() {
    return new ConsLoAlien(this.first.atEdge(), this.rest.aliensOnEdge());
  }

  public ILoAlien removeCollided(ILoBullet bullets) {
    if (bullets.hasHit(this.first)) {
      return this.rest.removeCollided(bullets);
    } else {
      return new ConsLoAlien(this.first, this.rest.removeCollided(bullets));
    }
  }

  public boolean hasHit(Bullet bullet) {
    return bullet.isCollision(this.first) || this.rest.hasHit(bullet);
  }

  public int countOnHits(ILoBullet bullets) {
    if (bullets.hasHit(this.first)) {
      return 1 + this.rest.countOnHits(bullets);
    }
    return this.rest.countOnHits(bullets);
  }

  public boolean hasAlienReachedBottom() {
    return this.first.posY >= 740 || this.rest.hasAlienReachedBottom();
  }
}

// to represent an empty list of alien
class MtLoAlien implements ILoAlien {
  MtLoAlien() {}

  /* Template
  Fields:
  Methods:
  ... this.drawLoAlien(WorldScene scene) ... WorldScene
  ... this.moveAllAlien() ... ILoAlien
  ... this.aliensOnEdge() ... ILoAlien
  ... this.removeCollided(ILoBullet bullets) ... ILoAlien
  ... this.hasHit(Bullet bullet) ... boolean
  ... this.countOnHits(ILoBullet bullets) ... int
  ... this.hasAliensReachedBottom() ... boolean
  Methods on Fields:
   */

  public WorldScene drawLoAlien(WorldScene scene) {
    return scene;
  }

  public ILoAlien moveAllAlien() {
    return this;
  }

  public ILoAlien aliensOnEdge() {
    return this;
  }

  public ILoAlien removeCollided(ILoBullet bullets) {
    return this;
  }

  public boolean hasHit(Bullet bullet) {
    return false;
  }

  public int countOnHits(ILoBullet bullets) {
    return 0;
  }

  public boolean hasAlienReachedBottom() {
    return false;
  }
}

// to represent an alien
class Alien {
  int posX;
  int posY;
  boolean movingLeft; // is the alien moving left?

  Alien(int posX, int posY, boolean movingLeft) {
    this.posX = posX;
    this.posY = posY;
    this.movingLeft = movingLeft;
  }

  /* Template
  Fields:
  ... this.posX ... int
  ... this.posY ... int
  ... this.movingLeft ... boolean
  Methods:
  ... this.moveLeft() ... Alien
  ... this.moveRight() ... Alien
  ... this.moveDown() ... Alien
  ... this.moveAlien() ... Alien
  ... this.atEdge() ... Alien
  ... this.drawAlien(WorldScene scene) ... WorldScene
  Methods on Fields:
  ... this.alien.moveLeft() ... Alien
  ... this.alien.moveRight() ... Alien
  ... this.alien.moveDown() ... Alien
  ... this.alien.moveAlien() ... Alien
  ... this.alien.atEdge() ... Alien
  ... this.alien.drawAlien(scene) ... WorldScene
   */

  // moves alien left
  public Alien moveLeft() {
    return new Alien(this.posX - 30, this.posY, true);
  }

  // moves alien right
  public Alien moveRight() {
    return new Alien(this.posX + 30, this.posY, false);
  }

  // moves alien down and switches alien to move right
  public Alien moveDown() {
    return new Alien(this.posX, this.posY + 75, !this.movingLeft);
  }

  // moves the alien it is moving left, otherwise, move right
  public Alien moveAlien() {
    if (this.movingLeft) {
      return this.moveLeft();
    }
    return this.moveRight();
  }

  // moves the alien down if it reaches to the edge of the scene
  public Alien atEdge() {
    if (this.posX <= 30 || this.posX >= 770) {
      return this.moveDown();
    }
    return this;
  }

  // draws the alien onto the scene
  public WorldScene drawAlien(WorldScene scene) {
    return scene.placeImageXY(new CircleImage(30, OutlineMode.SOLID, Color.GREEN), this.posX, this.posY);
  }
}

// to represent a score
class Score {
  int posX;
  int posY;
  int score; //

  Score(int score, int posX, int posY) {
    this.score = score; // the amount of points in the score
    this.posX = posX;
    this.posY = posY;
  }

   /* Template
  Fields:
  ... this.posX ... int
  ... this.posY ... int
  ... this.score ... int
  Methods:
  ... this.drawScore(WorldScene scene) ... WorldScene
  ... this.updateScore(int points) ... Score
  Methods on Fields:
  ... this.score.drawScore(scene) ... WorldScene
  ... this.score.updateScore(100) ... Score
   */

  // draws the score onto the scene
  public WorldScene drawScore(WorldScene scene) {
    return scene.placeImageXY(new TextImage("Score: " + this.score, 24, Color.BLACK), this.posX, this.posY);
  }

  // updates the score by adding points
  public Score updateScore(int points) {
    return new Score(this.score + points, this.posX, this.posY);
  }
}

// to represent a world
class Worlds extends World {
  PlayerShip player;
  ILoBullet bullets;
  ILoAlien aliens;
  Score score;

  Worlds(PlayerShip player, ILoBullet bullets, ILoAlien aliens, Score score) {
    this.player = player;
    this.bullets = bullets;
    this.aliens = aliens;
    this.score = score;
  }

    /* Template
  Fields:
  ... this.player ... PlayerShip
  ... this.bullets ... ILoBullet
  ... this.aliens ... ILoAlien
  ... this.score ... Score
  Methods:
  ... this.onKeyEvent(String key) ... World
  ... this.onTick() ... World
  ... this.gameOver() ... boolean
  ... this.drawGameOver(WorldScene scene) ... WorldScene
  ... this.makeScene() ... WorldScene
  Methods on Fields:
  ... this.world.onKeyEvent("left") ... World
  ... this.world.onTick() ... World
  ... this.world.gameOver() ... boolean
  ... this.world.drawGameOver(scene) ... WorldScene
  ... this.world.makeScene() ... WorldScene
   */

  @Override
  // handles key input
  public World onKeyEvent(String key) {
    if (key.equals(" ")) {
      Bullet newBullet = new Bullet(this.player.posX, this.player.posY, true);
      return new Worlds(this.player.moveShip(key), this.bullets.addBullet(newBullet), this.aliens, this.score);
    } else {
      return new Worlds(this.player.moveShip(key), this.bullets, this.aliens, this.score);
    }
  }

  // handles updating the world
  public World onTick() {
    ILoBullet movedBullets = this.bullets.moveAllBullets();
    ILoAlien movedAliens = this.aliens.moveAllAlien().aliensOnEdge();
    int hits = movedAliens.countOnHits(movedBullets);

    return new Worlds(this.player, movedBullets.removeBullet(movedAliens), movedAliens.removeCollided(movedBullets), this.score.updateScore(hits * 100));
  }

  // check if the game is over
  public boolean gameOver() {
    return this.aliens instanceof MtLoAlien || this.aliens.hasAlienReachedBottom();
  }

  // draws the game over onto the scene if aliens reached earth or defeated
  public WorldScene drawGameOver(WorldScene scene) {
    boolean defeatedAliens = this.aliens instanceof MtLoAlien;
    if (defeatedAliens) {
      scene = scene.placeImageXY(new TextImage("You Saved Earth!", 48, Color.GREEN), 400, 400);
    } else {
      scene = scene.placeImageXY(new TextImage("Earth Is Doomed!", 48, Color.RED), 400, 400);
    }
    return scene;
  }

  @Override
  // makes the scene with all the objects on each clock tick
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(800, 800);
    if (this.gameOver()) {
      return this.drawGameOver(scene);
    }
    return this.score.drawScore(this.aliens.drawLoAlien(this.player.drawShip(this.bullets.moveAllBullets().drawLoBullet(scene))));
  }
}

class ExamplesAliensAttack {
  PlayerShip Player = new PlayerShip(400, 760);
  Bullet Bullet = new Bullet(this.Player.posX, this.Player.posY, true);
  Bullet onHit = new Bullet(370, 100, true);
  ILoBullet Bullets = new MtLoBullet();
  ILoBullet firedBullets = new ConsLoBullet(this.Bullet, new ConsLoBullet(this.onHit, new MtLoBullet()));
  Alien alien1 = new Alien(370, 100, true);
  Alien alien2 = new Alien(310, 100, true);
  Alien alien3 = new Alien(250, 100, true);
  Alien alien4 = new Alien(190, 100, true);
  Alien alien5 = new Alien(130, 100, true);
  Alien alien6 = new Alien(430, 100, true);
  Alien alien7 = new Alien(490, 100, true);
  Alien alien8 = new Alien(550, 100, true);
  Alien alien9 = new Alien(610, 100, true);
  Alien alien10 = new Alien(670, 100, true);
  Alien alien11 = new Alien(370, 160, true);
  Alien alien12 = new Alien(310, 160, true);
  Alien alien13 = new Alien(250, 160, true);
  Alien alien14 = new Alien(190, 160, true);
  Alien alien15 = new Alien(130, 160, true);
  Alien alien16 = new Alien(430, 160, true);
  Alien alien17 = new Alien(490, 160, true);
  Alien alien18 = new Alien(550, 160, true);
  Alien alien19 = new Alien(610, 160, true);
  Alien alien20 = new Alien(670, 160, true);
  Alien alien21 = new Alien(370, 220, true);
  Alien alien22 = new Alien(310, 220, true);
  Alien alien23 = new Alien(250, 220, true);
  Alien alien24 = new Alien(190, 220, true);
  Alien alien25 = new Alien(130, 220, true);
  Alien alien26 = new Alien(430, 220, true);
  Alien alien27 = new Alien(490, 220, true);
  Alien alien28 = new Alien(550, 220, true);
  Alien alien29 = new Alien(610, 220, true);
  Alien alien30 = new Alien(670, 220, true);
  ILoAlien Aliens = new ConsLoAlien(this.alien1,
    new ConsLoAlien(this.alien2,
      new ConsLoAlien(this.alien3,
        new ConsLoAlien(this.alien4,
          new ConsLoAlien(this.alien5,
            new ConsLoAlien(this.alien6,
              new ConsLoAlien(this.alien7,
                new ConsLoAlien(this.alien8,
                  new ConsLoAlien(this.alien9,
                    new ConsLoAlien(this.alien10,
                      new ConsLoAlien(this.alien11,
                        new ConsLoAlien(this.alien12,
                          new ConsLoAlien(this.alien13,
                            new ConsLoAlien(this.alien14,
                              new ConsLoAlien(this.alien15,
                                new ConsLoAlien(this.alien16,
                                  new ConsLoAlien(this.alien17,
                                    new ConsLoAlien(this.alien18,
                                      new ConsLoAlien(this.alien19,
                                        new ConsLoAlien(this.alien20,
                                          new ConsLoAlien(this.alien21,
                                            new ConsLoAlien(this.alien22,
                                              new ConsLoAlien(this.alien23,
                                                new ConsLoAlien(this.alien24,
                                                  new ConsLoAlien(this.alien25,
                                                    new ConsLoAlien(this.alien26,
                                                      new ConsLoAlien(this.alien27,
                                                        new ConsLoAlien(this.alien28,
                                                          new ConsLoAlien(this.alien29,
                                                            new ConsLoAlien(this.alien30, new MtLoAlien()))))))))))))))))))))))))))))));
  Alien reachedBottom = new Alien(400, 740, true);
  ILoAlien testAliens = new ConsLoAlien(this.alien1,
    new ConsLoAlien(this.alien2,
      new ConsLoAlien(this.alien3, new MtLoAlien())));
  ILoAlien testAliens2 = new ConsLoAlien(this.reachedBottom, new MtLoAlien());
  ILoAlien noAliens = new MtLoAlien();
  Score Score = new Score(0, 400, 20);
  Worlds myWorld = new Worlds(this.Player, this.Bullets,this.Aliens,this.Score);
  WorldScene scene = new WorldScene(800,800);

  // test the method drawShip in the class PlayerShip
  boolean testDrawShip(Tester t) {
    return t.checkExpect(this.Player.drawShip(this.scene), this.scene.placeImageXY(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE), this.Player.posX, this.Player.posY));
  }

  // test the method moveShip in the class PlayerShip
  boolean testMoveShip(Tester t) {
    return t.checkExpect(this.Player.moveShip("left"), new PlayerShip(this.Player.posX - 15, this.Player.posY)) &&
      t.checkExpect(this.Player.moveShip("right"), new PlayerShip(this.Player.posX + 15, this.Player.posY));
  }

  // test the method drawLoBullet in the interface ILoBullet
  boolean testDrawLoBullet(Tester t) {
    return t.checkExpect(this.Bullets.drawLoBullet(this.scene), this.scene);
  }

  // test the method moveAllBullets in the interface ILoBullet
  boolean testMoveAllBullets(Tester t) {
    return t.checkExpect(this.Bullets.moveAllBullets(), new MtLoBullet());
  }

  // test the method addBullet in the interface ILoBullet
  boolean testAddBullet(Tester t) {
    return t.checkExpect(this.Bullets.addBullet(this.Bullet), new ConsLoBullet(this.Bullet, new MtLoBullet()));
  }

  // test the method removeBullet in the interface ILoBullet
  boolean testRemoveBullet(Tester t) {
    return t.checkExpect(this.Bullets.removeBullet(this.Aliens), new MtLoBullet());
  }

  // test the method hasHit in the interface ILoBullet
  boolean testHasHitBullet(Tester t) {
    return t.checkExpect(this.Bullets.hasHit(this.alien1), false) &&
      t.checkExpect(this.firedBullets.hasHit(this.alien1), true);
  }

  // test the method shoot in the class Bullet
  boolean testShoot(Tester t) {
    return t.checkExpect(this.Bullet.shoot(" ", this.Player.posX, this.Player.posY), new Bullet(this.Player.posX, this.Player.posY, true));
  }

  // test the method moveBullet in the class Bullet
  boolean testMoveBullet(Tester t) {
    return t.checkExpect(this.Bullet.moveBullet(), new Bullet(this.Player.posX, this.Player.posY - 60, true)) &&
      t.checkExpect(this.onHit.moveBullet(), new Bullet(370, 40, true));
  }

  // test the method drawBullet in the class Bullet
  boolean testDrawBullet(Tester t) {
    return t.checkExpect(this.Bullet.drawBullet(this.scene), this.scene.placeImageXY(new RectangleImage(20,40,OutlineMode.SOLID, Color.YELLOW), this.Player.posX, this.Player.posY));
  }

  // test the method isCollision in the class bullet
  boolean testIsCollision(Tester t) {
    return t.checkExpect(this.onHit.isCollision(this.alien1), true) &&
      t.checkExpect(this.Bullet.isCollision(this.alien1), false);
  }

  // test the method drawLoAlien in the interface ILoAlien
  boolean testDrawLoAlien(Tester t) {
    return t.checkExpect(this.testAliens.drawLoAlien(this.scene), this.scene.placeImageXY(new CircleImage(30,OutlineMode.SOLID,Color.GREEN), 370, 100)
      .placeImageXY(new CircleImage(30, OutlineMode.SOLID, Color.GREEN), 310,100).placeImageXY(new CircleImage(30, OutlineMode.SOLID, Color.GREEN), 250,100)) &&
      t.checkExpect(this.noAliens.drawLoAlien(this.scene), this.scene);
  }

  // test the method aliensOnEdge in the interface ILoAlien
  boolean testMoveAllAlien(Tester t) {
    return t.checkExpect(this.testAliens.moveAllAlien(), new ConsLoAlien(new Alien(340,100,true),
      new ConsLoAlien(new Alien(280, 100, true),
        new ConsLoAlien(new Alien(220, 100, true), new MtLoAlien())))) && t.checkExpect(this.noAliens.moveAllAlien(), new MtLoAlien());
  }

  // test the method aliensOnEdge in the interface ILoAlien
  boolean testAliensOnEdge(Tester t) {
    return t.checkExpect(this.testAliens.aliensOnEdge(), new ConsLoAlien(new Alien(370,100,true),
      new ConsLoAlien(new Alien(310, 100, true), new ConsLoAlien(new Alien(250, 100, true), new MtLoAlien())))) &&
      t.checkExpect(this.noAliens.aliensOnEdge(), new MtLoAlien());
  }

  // test the method removeCollided in the interface ILoAlien
  boolean testRemoveCollided(Tester t) {
    return t.checkExpect(this.testAliens.removeCollided(this.firedBullets), new ConsLoAlien((new Alien(310, 100, true)),
      new ConsLoAlien(new Alien(250, 100, true), new MtLoAlien()))) && t.checkExpect(this.noAliens.removeCollided(this.firedBullets), new MtLoAlien());
  }

  // test the method hasHit in the interface ILoAlien
  boolean testHasHitAlien(Tester t) {
    return t.checkExpect(this.testAliens.hasHit(this.onHit), true) &&
      t.checkExpect(this.noAliens.hasHit(this.onHit), false);
  }

  // test the method countOnHits in the interface ILoAlien
  boolean testCountOnHits(Tester t) {
    return t.checkExpect(this.testAliens.countOnHits(this.firedBullets), 1) &&
      t.checkExpect(this.noAliens.countOnHits(this.firedBullets), 0);
  }

  // test the method hasAliensReachedBottom in the interface ILoAlien
  boolean testHasAliensReachedBottom(Tester t) {
    return t.checkExpect(this.Aliens.hasAlienReachedBottom(), false) &&
      t.checkExpect(this.testAliens.hasAlienReachedBottom(), false) &&
      t.checkExpect(this.testAliens2.hasAlienReachedBottom(), true);
  }

  // test the method moveLeft in the class Alien
  boolean testMoveLeft(Tester t) {
    return t.checkExpect(this.alien1.moveLeft(), new Alien(340,100,true)) &&
      t.checkExpect(this.alien2.moveLeft(), new Alien(280,100,true));
  }

  // test the method moveRight in the class Alien
  boolean testMoveRight(Tester t) {
    return t.checkExpect(this.alien1.moveRight(), new Alien(400, 100, false)) &&
      t.checkExpect(this.alien2.moveRight(), new Alien(340, 100, false));
  }

  // test the method moveDown in the class Alien
  boolean testMoveDown(Tester t) {
    return t.checkExpect(this.alien1.moveDown(), new Alien(370,175,false)) &&
      t.checkExpect(this.alien2.moveDown(), new Alien(310,175,false));
  }

  // test the method moveAlien in the class Alien
  boolean testMoveAlien(Tester t) {
      return t.checkExpect(this.alien1.moveAlien(), new Alien(340,100,true)) &&
        t.checkExpect(this.alien2.moveAlien(), new Alien(280,100,true));
  }

  // test the method drawAlien in the class Alien
  boolean testDrawAliens(Tester t) {
    return t.checkExpect(this.alien1.drawAlien(this.scene), this.scene.placeImageXY(new CircleImage(30, OutlineMode.SOLID, Color.GREEN), 370, 100)) &&
      t.checkExpect(this.alien2.drawAlien(this.scene), this.scene.placeImageXY(new CircleImage(30, OutlineMode.SOLID, Color.GREEN), 310,100));
  }

  // test the method drawScore in the class Score
  boolean testDrawScore(Tester t) {
    return t.checkExpect(this.Score.drawScore(this.scene), this.scene.placeImageXY(new TextImage("Score: " + 0, 24, Color.BLACK), 400, 20));
  }

  // test the method updateScore in the class Score
  boolean testUpdateScore(Tester t) {
    return t.checkExpect(this.Score.updateScore(100), new Score(100, 400,20));
  }

  // Test the method onKeyEvent in the class Worlds
  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(this.myWorld.onKeyEvent("left"), new Worlds(this.Player.moveShip("left"), this.Bullets, this.Aliens, this.Score)) &&
      t.checkExpect(this.myWorld.onKeyEvent("right"), new Worlds(this.Player.moveShip("right"), this.Bullets, this.Aliens, this.Score)) &&
      t.checkExpect(this.myWorld.onKeyEvent(" "), new Worlds(this.Player, this.Bullets.addBullet(new Bullet(this.Player.posX, this.Player.posY, true)), this.Aliens, this.Score));
  }

  // test the method onTick in the class Worlds
  boolean testOnTick(Tester t) {
    return t.checkExpect(this.myWorld.onTick(), new Worlds(this.Player, this.Bullets.moveAllBullets(), this.Aliens.moveAllAlien(), this.Score));
  }

  // test the method gameOver in the class Worlds
  boolean testGameOver(Tester t) {
    return t.checkExpect(this.myWorld.gameOver(), false) &&
      t.checkExpect(new Worlds(this.Player, this.Bullets, this.testAliens2, this.Score).gameOver(), true);
  }

  // test the method drawGameOver in the class Worlds
  boolean testDrawGameOver(Tester t) {
    return t.checkExpect(this.myWorld.drawGameOver(this.scene), this.scene.placeImageXY(new TextImage("Earth Is Doomed!", 48, Color.RED), 400, 400));
  }

  // test the method makeScene in the class Worlds
  boolean testMakeScene(Tester t) {
    return t.checkExpect(this.myWorld.makeScene(), this.Player.drawShip(this.Aliens.drawLoAlien(this.Bullets.drawLoBullet(this.Score.drawScore(this.scene)))));
  }

  // to display the game
  boolean testBigBang(Tester t) {
    Worlds w = new Worlds(this.Player, this.Bullets, this.Aliens, this.Score);
    int worldWidth = 800;
    int worldHeight = 800;
    double tickRate = 0.1;

    return w.bigBang(worldWidth, worldHeight, tickRate);
  }
}