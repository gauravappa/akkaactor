package com.akkapractice.childactor;

import com.akkapractice.firstactor.FirstAbstractBehavior;

import akka.actor.typed.ActorSystem;

public class ChildMessageMain {

	
	public static void main(String[] args) {
		ActorSystem<String> actorSystem = 
				ActorSystem.create(FirstChildAbstractBehaviour.create(), "FirstActor");
		actorSystem.tell("who are you");
		actorSystem.tell("Create a Child");

	}
}
