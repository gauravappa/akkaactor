package com.akkapractice.casestudywithmultiplehandler;

import akka.actor.typed.ActorSystem;

public class RacerMain1 {

	public static void main(String[] args) {

		
		ActorSystem<RacerControllerBehavior1.Command> root = ActorSystem.create(RacerControllerBehavior1.create(), "controller");

		root.tell(new RacerControllerBehavior1.StartCommand());
	
	}
	
}
