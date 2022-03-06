package com.akkapractice.casestudy1;

import akka.actor.typed.ActorSystem;

public class RacerMain {

	public static void main(String[] args) {
		
		ActorSystem<RacerControllerBehavior.Command> root = ActorSystem.create(RacerControllerBehavior.create(), "controller");

		root.tell(new RacerControllerBehavior.StartCommand());
	}
	
}
