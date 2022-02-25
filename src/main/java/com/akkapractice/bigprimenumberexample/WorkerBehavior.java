package com.akkapractice.bigprimenumberexample;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command>{

	
	public static class Command implements Serializable{
		
		private static final long serialVersionUID=1L;
		
		private String message;
		private ActorRef<ManagerBehavior.Command> sender;
		public Command(String message, ActorRef<ManagerBehavior.Command> sender) {
			super();
			this.message = message;
			this.sender = sender;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}
		public String getMessage() {
			return message;
		}
		public ActorRef<ManagerBehavior.Command> getSender() {
			return sender;
		}
	
	}
	
	private WorkerBehavior(ActorContext<Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static Behavior<Command> create(){
		
		return Behaviors.setup(WorkerBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onAnyMessage(command->{
					
					if(command.getMessage().equals("start")){
						
						BigInteger bigInteger = new BigInteger(2000,new Random());
						
						command.getSender().tell(new ManagerBehavior.ResultCommand(bigInteger));	
					}
					return this;
				})
				
				.build();
	}
	
	
	
	

}
