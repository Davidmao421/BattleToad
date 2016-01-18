package team094;

import java.util.*;

import battlecode.common.*;

public class CompareStuff {
	public static boolean isInfected(RobotInfo r) {
		if (r.viperInfectedTurns > 0 || r.zombieInfectedTurns > 0)
			return true;
		else
			return false;
	}

	public static RobotInfo soldierCompare(RobotInfo[] enemies) // returns the
																// enemy that
																// does the most
																// damage, and
																// the lowest
																// health if
																// multiple
	{
		ArrayList<RobotInfo> allDE = new ArrayList<RobotInfo>();
		RobotInfo DE = enemies[0];
		for (RobotInfo i : enemies) {
			if (i.attackPower < DE.attackPower) {
				allDE.clear();
				;
				DE = i;
			} else if (i.attackPower == DE.attackPower) {
				allDE.add(i);
			}
		}
		RobotInfo HE = allDE.get(0);
		for (RobotInfo i : allDE) {
			if (i.health < HE.health)
				HE = i;
		}
		return HE;
	}

	public static RobotInfo soldierCompare(RobotInfo[] enemies, RobotInfo[] zombies) {
		if (enemies.length + zombies.length > 0) {
			RobotInfo[] all = new RobotInfo[enemies.length + zombies.length];
			for (int i = 0; i < enemies.length; i++) {
				all[i] = enemies[i];
			}
			for (int i = enemies.length; i < enemies.length + zombies.length; i++) {
				all[i] = zombies[i - enemies.length];
			}
			RobotInfo maxD = all[0];
			for (RobotInfo r : all) {
				if (maxD.attackPower < r.attackPower) {
					maxD = r;
				} else if (maxD.attackPower == r.attackPower)
					if (maxD.health > r.attackPower)
						maxD = r;
			}
			return maxD;
		}
		return null;
	}

	/*
	 * ArrayList<RobotInfo> allDZ = new ArrayList<RobotInfo>(); RobotInfo DZ =
	 * zombies[0]; for(RobotInfo i: zombies){ if(i.attackPower<DZ.attackPower){
	 * allDZ.clear();; DZ=i; } else if(i.attackPower==DZ.attackPower){
	 * allDZ.add(i); } } RobotInfo HZ = allDZ.get(0); for(RobotInfo i: allDZ) {
	 * if(i.health<HZ.health) HZ=i; } if (allDZ.size()==1) { return
	 * allDZ.get(0); } else { ArrayList<RobotInfo> allDE = new
	 * ArrayList<RobotInfo>(); RobotInfo DE = enemies[0]; for(RobotInfo i:
	 * enemies){ if(i.attackPower<DE.attackPower){ allDE.clear();; DE=i; } else
	 * if(i.attackPower==DE.attackPower){ allDE.add(i); } } RobotInfo HE =
	 * allDE.get(0); //if we want to do stuff with the lowest health enemy
	 * for(RobotInfo i: allDE) { if(i.health<HE.health) HE=i; } if
	 * (allDE.size()==1&&DE.attackPower>DZ.attackPower) //only attacks enemy if
	 * its stronger than zombie { return allDE.get(0); } else { return DZ; } }
	 */
	public static RobotInfo viperCompare(RobotInfo[] enemies, boolean kill) {
		if (enemies.length > 0) {
			if (kill) { // prioritizes weakest zombie not infected to make quick
						// kill
				RobotInfo minH = enemies[0];
				for (RobotInfo r : enemies) {
					if (r.health < minH.health && !isInfected(r))
						minH = r;
				}
				return minH;
			} else { // prioritizes strongest zombie not infected
				RobotInfo maxH = enemies[0];
				for (RobotInfo r : enemies) {
					if (r.health > maxH.health && !isInfected(r))
						maxH = r;
				}
				return maxH;
			}
		}
		return null;
	}

	public static RobotInfo moveAwayFrom(RobotInfo[] enemies, MapLocation myLoc) {
		RobotInfo maxMove = null;
		double maxD = 0;
		if (enemies != null) {
			for (RobotInfo r : enemies) {
				if (r.type.attackRadiusSquared > r.location.distanceSquaredTo(myLoc)
						&& Math.sqrt(r.type.attackRadiusSquared)
								- Math.sqrt(r.location.distanceSquaredTo(myLoc)) > maxD) // TODO:
																							// possibly
																							// allow
																							// for
																							// moving
																							// away
																							// from
																							// non
																							// zombies
				{
					maxMove = r;
					maxD = Math.sqrt(r.type.attackRadiusSquared) - Math.sqrt(r.location.distanceSquaredTo(myLoc));
				}
			}
			return maxMove;
		}
		return null;
	}

	public static boolean containsSignal(Signal s, Iterable<Signal> col) {
		for (Signal i : col)
			if (i.getID() == s.getID() && i.getLocation().equals(s.getLocation())
					&& i.getMessage()[0] == s.getMessage()[0] && i.getMessage()[1] == s.getMessage()[1])
				return true;
		return false;
	}
}