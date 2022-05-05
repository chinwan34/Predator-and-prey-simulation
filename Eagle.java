import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a eagle.
 * Eagles age, move, eat preys, and die.
 * 
 * @author Bowen Zhu (k21083430) and Chin Wan (k21016106)
 * @version 2022.03.01 (2)
 */
public class Eagle extends Animal
{
    // Characteristics shared by all eagles (class variables).
    
    // The age at which a eagle can start to breed.
    private static final int BREEDING_AGE = 30;
    // The age to which a eagle can live.
    private static final int MAX_AGE = 180;
    // The likelihood of a eagle breeding.
    private static final double BREEDING_PROBABILITY = 0.7;
    // The maximum number of births6
    private static final int MAX_LITTER_SIZE = 3;
    // Maximum number of steps a eagle can go before it has to eat again.
    private static final int MAX_FOOD_LEVEL = 20;
    // Probability of hunger increase (foodLevel decrease) at night.
    private static final double HUNGER_INCREASE_PROBABILITY_NIGHT = 0.37;
    // The likelihood of an eagle dying from the storm.
    private static final double STORM_DEATH_PROBABILITY = 0.03;
    
    // Eagle hunting range in row (x).
    private static final int EAGLE_HUNT_ROW_VALUE = 2;
    // Eagle hunting range in col (y).
    private static final int EAGLE_HUNT_COL_VALUE = 3;
    
    // A shared random number generator.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).
    
    // The eagle's age.
    private int age;
    // The eagle's food level, which is increased by eating preys.
    private int foodLevel;
    // The likelihood of an eagle to find food
    private double findFoodProbability = 1.0;

    /**
     * Create a eagle. A eagle can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the eagle will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Eagle(boolean randomAge, Field field, Location location)
    {
        super(field, location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(MAX_FOOD_LEVEL);
        }
        else {
            age = 0;
            foodLevel = MAX_FOOD_LEVEL;
        }
        setGender();
    }
    
    /**
     * This is what the eagle does most of the time: it hunts for
     * preys. In the process, it might breed, die of hunger,
     * or die of old age, or die of disease.
     * @param field The field currently occupied.
     * @param newEagles A list to return newly born Eagles.
     */
    public void act(List<Animal> newEagles, String weather)
    {
        incrementAge();
        incrementHunger();
        
        if(isAlive()) {
            diseaseAppear();
            if(isExist()) {
                incrementTime();
                infectHost();
                willDie();
            }
            
            if(isAlive()) {
                if (genderDetect() == true) {
                    if (getGender()) {
                        giveBirth(newEagles);     
                    }
                }
                
                newLocation();
            }            
        }
    }
    
    /**
     * Eagle sleeps at night, age increment as usual and might die of hunger 
     * or die of age or infect disease from other animals nearby and 
     * might also die of disease is well. However, its foodLevel descrease 
     * less frequently compare to day time.
     * Eagle do not give birth at night but some other animals do.
     * @param field The field currently occupied.
     * @param newEagles A list to return newly born Eagles.
     */
    public void nightAct(List<Animal> newEagles, String weather) {
        incrementAge();
        if (rand.nextDouble() < HUNGER_INCREASE_PROBABILITY_NIGHT) {
            incrementHunger();    
        }    
    }
    
    /**
     * Find the new Location for eagles, if there's prey near by, kill the prey and 
     * go to its position. If there aren't any prey surround, go to random free adjacent location.
     */
    private void newLocation() {
        // Move towards a source of food if found.
        Location newLocation = findFood();
        if(newLocation == null) { 
            // No food found - try to move to a free location.
            newLocation = getField().freeAdjacentLocation(getLocation());
        }
        // See if it was possible to move.
        if(newLocation != null) {
            setLocation(newLocation);
        }
        else {
            // Overcrowding.
            setDead();
        }
    }
    
    /**
     * Increase the age. This could result in the eagle's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this eagle more hungry. This could result in the eagle's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for preys adjacent to the current location.
     * Only the first live prey is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.surroundLocations(getLocation(), EAGLE_HUNT_ROW_VALUE, EAGLE_HUNT_COL_VALUE);
        Iterator<Location> it = adjacent.iterator();
        if(rand.nextDouble() <= findFoodProbability) {
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Rabbit) {
                    Rabbit rabbit = (Rabbit) animal;
                    if(rabbit.isAlive()) { 
                        rabbit.setDead();
                        foodLevel = foodLevel + rabbit.getFoodValue();
                        return where;
                    }
                }
                else if(animal instanceof Mouse) {
                    Mouse mouse = (Mouse) animal;
                    if(mouse.isAlive()) { 
                        mouse.setDead();
                        foodLevel = foodLevel + mouse.getFoodValue();
                        return where;
                    }
                }
                
                if(foodLevel > MAX_FOOD_LEVEL) {
                    foodLevel = MAX_FOOD_LEVEL;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this eagle is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newEagles A list to return newly born eagles.
     */
    private void giveBirth(List<Animal> newEagles)
    {
        // New eagles are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Eagle young = new Eagle(false, field, loc);
            newEagles.add(young);
        }
    }
    
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A eagle can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
    
    /**
     * The effect of the weather on the eagle
     * @param weather The current weather
     */
    public void weatherEffect(String weather)
    {
        if (weather.equals("sunny")){
            findFoodProbability = 0.7;
        }
        if (weather.equals("rainy")){
            findFoodProbability = 0.55;
        }
        else if (weather.equals("foggy")){
            findFoodProbability = 0.4;
        }
        else if (weather.equals("storm")){
            findFoodProbability = 0.2;
            if (rand.nextDouble() <= STORM_DEATH_PROBABILITY){
                setDead();
            }
        }
    }
}
