package pacman;

public class GameObject {
    //every game object has a coordinate location on 2d plane
    protected int x;
    protected int y;

    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
