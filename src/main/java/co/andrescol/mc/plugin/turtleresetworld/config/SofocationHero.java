package co.andrescol.mc.plugin.turtleresetworld.config;

public class SofocationHero {

	private boolean enable;
	private SafePosition position;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public SafePosition getPosition() {
		return position;
	}

	public void setPosition(SafePosition position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return String.format("enable: %b, position: %s", enable, position);
	}
}
