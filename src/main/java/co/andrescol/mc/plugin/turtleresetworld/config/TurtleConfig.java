package co.andrescol.mc.plugin.turtleresetworld.config;

public class TurtleConfig {

	private String version;
	private SofocationHero heroSofocation;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public SofocationHero getHeroSofocation() {
		return heroSofocation;
	}

	public void setHeroSofocation(SofocationHero heroSofocation) {
		this.heroSofocation = heroSofocation;
	}

	@Override
	public String toString() {
		return String.format("version: %s, heroSofocation: %s", version, heroSofocation);
	}

}
