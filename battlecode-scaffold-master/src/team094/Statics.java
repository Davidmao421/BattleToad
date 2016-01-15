package team094;

import java.util.List;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Statics {

	public static Direction[] directions = Direction.values();

	public static int sqrDist(MapLocation loc1 , MapLocation loc2){
		return (loc1.x-loc2.x)*(loc1.x-loc2.x) + (loc1.y-loc2.y)*(loc1.y-loc2.y);
	}
	
	public static MapLocation closestLoc(MapLocation loc, MapLocation[] list){
		MapLocation closest = null; 
		int dist = Integer.MAX_VALUE;
		for (MapLocation l : list){
			int td = sqrDist(loc,l);
			if (td < dist){
				dist = td;
				closest = l;
			}
		}
		return closest;
	}
	
}
