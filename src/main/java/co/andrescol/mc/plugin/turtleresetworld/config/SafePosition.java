package co.andrescol.mc.plugin.turtleresetworld.config;

public class SafePosition {

	private int x;
	private int y;
	private int z;
	private String world;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	@Override
	public String toString() {
		return String.format("{world: %s, x: %d, y: %d, z: %d}", world, x, y, z);
	}
	
}
