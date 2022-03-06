package com.akkapractice.casestudy1;

import java.io.Serializable;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RacerBehaviour extends AbstractBehavior<RacerBehaviour.Command>{


	private final double defaultAverageSpeed = 48.2;
	private int averageSpeedAdjustmentFactor;
	private Random random;	
	
	private double currentSpeed = 0;
	private double currentPosition = 0;
	private int raceLength;
	
	public interface Command extends Serializable {

	}

	public static class StartCommand implements Command{
		
		private static final long serialVersionUID=1L;
		
		private int raceLength;
		
		public StartCommand(int raceLength) {
			
			this.raceLength=raceLength;
			
		}
		
		public int getRaceLength() {
			
			return raceLength;
			
		}
		
	}
	
	public static class PositionCommand implements Command {

		private static final long serialVersionUID=1L;
		
		ActorRef<RacerControllerBehavior.Command> controller;
		
		public PositionCommand(ActorRef<RacerControllerBehavior.Command> controller) {
			
			this.controller=controller;
			
		}
		
		public ActorRef<RacerControllerBehavior.Command> getController(){
			
			return controller;
		}
		
		
		
	}
	
	private RacerBehaviour(ActorContext<Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static Behavior<RacerBehaviour.Command> create(){
	
		return Behaviors.setup(RacerBehaviour::new);
		
	}

	private double getMaxSpeed() {
		return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed() {
		if (currentPosition < (raceLength / 4)) {
			currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
		}
		else {
			currentSpeed = currentSpeed * (0.5 + random.nextDouble());
		}
	
		if (currentSpeed > getMaxSpeed()) 
			currentSpeed = getMaxSpeed();
		
		if (currentSpeed < 5)
			currentSpeed = 5;
		
		if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
			currentSpeed = getMaxSpeed() / 2;
		}
	}
		

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(StartCommand.class, message->{
					
					raceLength=message.getRaceLength();
					random = new Random();
					averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
					
					
					return this;
				})
				.onMessage(PositionCommand.class, message->{
					
						determineNextSpeed();
						currentPosition += getDistanceMovedPerSecond();
						if (currentPosition > raceLength )
							currentPosition  = raceLength;
						
						message.getController().tell(new RacerControllerBehavior.RacerUpdateCommand(getContext().getSelf(), (int)currentPosition));
					
					return this;
				})
				.build();
	}
	
}
