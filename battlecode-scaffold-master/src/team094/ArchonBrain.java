package team094;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {

	private enum Routine {
		TURRET_CLUSTER, CHARGE, SCAVENGE, RANDOM, NONE, SCOUT
	}

	private int turns;
	private Routine current;

	private boolean buildRobot(RobotController rc, RobotType type) {
		ArrayList<Direction> list = new ArrayList<>(Arrays.asList(Statics.directions));
		for (int i = 0; list.size() > 0; i++) {
			if (rc.canBuild(list.get(i), type)) {
				try {
					rc.build(list.get(i), type);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private boolean buildScout(RobotController rc) {
		return buildRobot(rc, RobotType.SCOUT);
	}

	private void intialize(RobotController rc) {
		turns = 0;
		buildScout(rc);
		current = Routine.TURRET_CLUSTER;
	}

	private void buildTurretCluster(RobotController rc) {
		switch (turns) {
		case 1:
		case 2:
		case 4:
			buildRobot(rc, RobotType.SOLDIER);
			break;
		case 3:
			buildRobot(rc, RobotType.TURRET);
			break;
		default:
			current = Routine.NONE;
			turns = 0;
			break;
		}
	}

	private void runTurn(RobotController rc) {
		switch (current) {
		case CHARGE:
			break;
		case NONE:
			break;
		case RANDOM:
			break;
		case SCAVENGE:
			break;
		case SCOUT:
			break;
		case TURRET_CLUSTER:
			buildTurretCluster(rc);
			break;
		default:
			break;
		}
	}

	@Override
	public void run(RobotController rc) {
		intialize(rc);

		while (true) {
			Clock.yield();
			turns++;
			runTurn(rc);
		}
	}

}
