import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing rabbits and foxes.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2016.02.29 (2)
 */
public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    
    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.015;
    // The probability that a wolf will be created in any given grid position.
    private static final double WOLF_CREATION_PROBABILITY = 0.015;
    // The probability that a deer will be created in any given grid position.
    private static final double DEER_CREATION_PROBABILITY = 0.06;
    // The probability that a mouse will be created in any given grid position.
    private static final double MOUSE_CREATION_PROBABILITY = 0.18;
    // The probability that an eagle will be created in any given grid position.
    private static final double EAGLE_CREATION_PROBABILITY = 0.023;
    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.15;
    
    // The probability that a rose will be created in any given grid position.
    private static final double ROSE_CREATION_PROBABILITY = 0.29;
    // The probability that an appletree will be created in any given grid position.
    private static final double APPLETREE_CREATION_PROBABILITY = 0.17;

    // List of animals in the field.
    private List<Animal> animals;
    // List of plants in the field.
    private List<Plant> plants;
    // List of weathers.
    //private List<String> weathers;
    
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    // The current weather of the simulation.
 
    // The current weather of the simulator
    private Weather weather;
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        animals = new ArrayList<>();
        plants = new ArrayList<>();
        
        weather = new Weather();
        
        field = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Rabbit.class, Color.ORANGE);
        view.setColor(Fox.class, Color.BLUE);
        view.setColor(Wolf.class, Color.CYAN);
        view.setColor(Deer.class, Color.PINK);
        view.setColor(Mouse.class, Color.BLACK);
        view.setColor(Eagle.class, Color.YELLOW);
        view.setColor(Rose.class, Color.RED);
        view.setColor(Appletree.class, Color.GREEN);
        // Setup a valid starting point.
        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period,
     * (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(4000);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
            delay(150);   // uncomment this to run more slowly
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox and rabbit.
     */
    public void simulateOneStep()
    {
        step++;
        
        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<>();
        // Provide space for newborn plants.
        List<Plant> newPlants = new ArrayList<>();
        
        weather.generateWeather();
        
        // Let all animals act.
        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            if((step%24)>6 & (step%24) <= 18 ) {
                animal.act(newAnimals, weather.getWeather());    
            }
            else {
                animal.nightAct(newAnimals, weather.getWeather());
            }
            if(! animal.isAlive()) {
                it.remove();
            }
        }
        
        // Lest all plants act
        for(Iterator<Plant> it = plants.iterator(); it.hasNext(); ) {
            Plant plant = it.next();
            plant.act(newPlants, weather.getWeather());    
            if(! plant.isAlive()) {
                it.remove();
            }
        }
        
               
        // Add the newly born animals to the main lists.
        animals.addAll(newAnimals);
        // Add the newly born plants to the main lists.
        plants.addAll(newPlants);
        

        view.showStatus(step, field, weather.getWeather());
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        animals.clear();
        plants.clear();
        populate();
        
        // Show the starting state in the view.
        view.showStatus(step, field, weather.getWeather());
    }
    
    /**
     * Randomly populate the field with foxes and rabbits.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Fox fox = new Fox(true, field, location);
                    animals.add(fox);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Rabbit rabbit = new Rabbit(true, field, location);
                    animals.add(rabbit);
                }
                else if(rand.nextDouble() <= WOLF_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Wolf wolf = new Wolf(true, field, location);
                    animals.add(wolf);
                }
                else if(rand.nextDouble() <= DEER_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Deer deer = new Deer(true, field, location);
                    animals.add(deer);
                }
                else if(rand.nextDouble() <= MOUSE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Mouse mouse = new Mouse(true, field, location);
                    animals.add(mouse);
                }
                else if(rand.nextDouble() <= EAGLE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Eagle eagle = new Eagle(true, field, location);
                    animals.add(eagle);
                }
                else if (rand.nextDouble() <= ROSE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Rose rose = new Rose(true, field, location);
                    plants.add(rose);
                }
                else if (rand.nextDouble() <= APPLETREE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Appletree appletree = new Appletree(true, field, location);
                    plants.add(appletree);
                }
                // else leave the location empty.
            }
        }
    }
    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }
}
