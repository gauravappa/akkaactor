package com.akkapractice.childactor;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FirstChildAbstractBehaviour extends AbstractBehavior<String>{

	//contructor
	private FirstChildAbstractBehaviour(ActorContext<String> context) {
		super(context);
	}
	
	
	//boilerplate code for creating object of behavior
	//setup method uses a lamda as parameter which takes
	//ActorContext as parameter which we used below to create
	//Object of FirstAbstractBehavior Class 
	public static Behavior<String> create(){
		
		return Behaviors.setup(FirstChildAbstractBehaviour::new);
		
	}

	
	//this method will define how to process message once it 
	// is received
	@Override
	public Receive<String> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				
				.onMessageEquals("who are you", ()->{
					
					System.out.println(getContext().getSelf().path());
					return this;
				})
				.onMessageEquals("Create a Child", ()->{
					
					ActorRef<String> childActor=getContext()
					.spawn(FirstChildAbstractBehaviour.create(), "SecondActor");
				
					childActor.tell("who are you");
	
					return this;
				})
				.onAnyMessage(message->{
					System.out.println("Recieved Message :: "+message);
					return this;
					
				})
				.build();
	}

}
