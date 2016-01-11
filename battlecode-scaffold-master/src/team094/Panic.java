package team094;

import battlecode.common.MapLocation;

public class Panic {
	
	public MapLocation[] locs;
	int id;
	public MapLocation[] getLocs() {
		return locs;
	}
	public void setLocs(MapLocation[] locs) {
		this.locs = locs;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Panic(int id, MapLocation...locs) {
		super();
		this.locs = locs;
		this.id = id;
	}
	
	

}
