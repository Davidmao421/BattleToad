package team094;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;

import battlecode.common.*;

public class ScoutBrain implements Brain {
	MapLocation enemyCom, teamCom;

	// Queue<Signal> broadcastQueue;
	LinkedList<Signal> broadcastQueue;
	int index;

	public void initialize(RobotController rc) {
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		broadcastQueue = new LinkedList<>();
		index = 0;
	}

	public void radiate(RobotController rc) throws GameActionException {
		Direction d = teamCom.directionTo(rc.getLocation());
		if (rc.canMove(d))
			rc.move(d);
	}

	public void senseBroadcast(RobotController rc) {
		RobotInfo[] robots = rc.senseNearbyRobots();
		MapLocation[] parts = rc.sensePartLocations(-1);

		for (MapLocation part : parts) {
			if (!broadcastQueue.contains(SignalEncoder.encodeParts(part, rc.senseParts(part)))) {
				broadcastQueue.offer(SignalEncoder.encodeParts(part, rc.senseParts(part)));
			}
		}

		for (RobotInfo info : robots) {
			if (info.team == rc.getTeam())
				continue;
			if (rc.getTeam().opponent() == info.team
					&& (info.type == RobotType.ZOMBIEDEN || info.type == RobotType.ARCHON)) {
				if (!broadcastQueue.contains(SignalEncoder.encodeRobot(info.type, info.ID, info.location)))
				broadcastQueue.offer(SignalEncoder.encodeRobot(info.type, info.ID, info.location));
			}
			if (info.team.equals(Team.NEUTRAL)) {
				if (!broadcastQueue.contains(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location)))
				broadcastQueue.offer(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location));
			}
		}

	}

	public void runTurn(RobotController rc) throws GameActionException {
		senseBroadcast(rc);
		// radiate(rc);
		broadcast(rc);
	}

	public void broadcast(RobotController rc) throws GameActionException {
		if (rc.isCoreReady() && index < broadcastQueue.size()) {
			Signal s = broadcastQueue.get(index);
			index++;
			rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], 2 * rc.getType().sensorRadiusSquared);
		}
	}

	public void run(RobotController rc) {
		try {
			initialize(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			Clock.yield();
			try {
				runTurn(rc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * public void run(RobotController rc){ while (true)Clock.yield(); // what
	 * the hell is this }
	 */
}