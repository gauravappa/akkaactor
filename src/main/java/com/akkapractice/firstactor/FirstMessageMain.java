package com.akkapractice.firstactor;

import akka.actor.typed.ActorSystem;

public class FirstMessageMain {

	public static void main(String[] args) {
		
		ActorSystem<String> actorSystem = 
				ActorSystem.create(FirstAbstractBehavior.create(), "FirstActor");
		actorSystem.tell("I am first Actor");
	}
	
}
