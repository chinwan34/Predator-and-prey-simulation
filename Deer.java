import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a Deer.
 * Deers age, move, breed, and die.
 * 
 * @author Bowen Zhu (k21083430) and Chin Wan (k21016106)
 * @version 2022.03.01 (2)
 */
public class Deer extends Animal
{
    // Characteristics shared by all Deers (class variables).

    // The age at which a Deer can start to breed.
    private static final int BREEDING_AGE = 25;
    // The age to which a Deer can live.
    private static final int MAX_AGE = 120;
    // The likelihood of a Deer breeding.
    private static final double BREEDING_PROBABILITY = 0.5;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 2;
    // Maximum number of steps a Deer can go before it has to eat again.
    private static final int MAX_FOOD_LEVEL = 20;
    // Probability of acting at night.
    private static final double ACTING_PROBABILITY_NIGHT = 0.5;
    // food value predator will get, if it eat this Deer.
    private static final int FOOD_VALUE = 13;
    // The likelihood of a deer dying from the storm.
    private static final double STORM_DEATH_PROBABILITY = 0.03;
    // A shared random number generator.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).
    
    // The Deer's age.
    private int age;
    // The Deer's food level, which is increased by eating plants.    
    private int foodLevel;
    // The likelihood of an eagle to find food
    private double findFoodProbability = 1.0;

    /**
     * Create a new Deer. A Deer may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the Deer will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Deer(boolean randomAge, Field field, Location location)
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
     * This is what the Deer does most of the time - it runs 
     * around. Sometimes it will breed or die of old age, or die of disease.
     * @param newDeers A list to return newly born Deers.
     */
    public void act(List<Animal> newDeers, String weather)
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
                        giveBirth(newDeers);   
                    }
                }
                
                newLocation();
            }
        }
    }
    
    /**
     * Deers act less frequently at night, it will not naturelly infect disease
     * at night. Age increment as usual and might die of hunger 
     * or die of age or infect disease from other animals nearby and 
     * might also die of disease is well.
     * @param field The field currently occupied.
     * @param newWolves A list to return newly born Deers.
     */
    public void nightAct(List<Animal> newDeers, String weather) {
        incrementAge();
        incrementHunger();
        if (rand.nextDouble() < ACTING_PROBABILITY_NIGHT) {   
            if(isAlive()) {
                if (genderDetect() == true) {
                    if (getGender()) {
                        giveBirth(newDeers); 
                    }
                }
                newLocation();
            }
        }
    }
    
    /**
     * Find the new Location for deers, if there's plants near by, eat the plant and 
     * go to its position. If there aren't any plant surround, go to random free adjacent location.
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
     * Increase the age.
     * This could result in the Deer's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this Deer more hungry. This could result in the Deer's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for plants adjacent to the current location.
     * Only the first live plant is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        if(rand.nextDouble() <= findFoodProbability) {
            while(it.hasNext()) {
                Location where = it.next();
                Object plant = field.getObjectAt(where);
                if(plant instanceof Appletree) {
                    Appletree appletree = (Appletree) plant;
                    if(appletree.isAlive()) { 
                        appletree.setDead();
                        foodLevel = foodLevel + appletree.getFoodValue();
                        return where;
                    }
                }
                else if(plant instanceof Rose) {
                    Rose rose = (Rose) plant;
                    if(rose.isAlive()) { 
                        rose.setDead();
                        foodLevel = foodLevel + rose.getFoodValue();
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
     * Check whether or not this Deer is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newDeers A list to return newly born Deers.
     */
    private void giveBirth(List<Animal> newDeers)
    {
        // New Deers are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Deer young = new Deer(false, field, loc);
            newDeers.add(young);
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
     * A Deer can breed if it has reached the breeding age.
     * @return true if the Deer can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Return the Deer's food Value
     * @return the Deer's food Value
     */
    public int getFoodValue() {
        return FOOD_VALUE;
    }
    
    /**
     * The effect of the weather on the deer
     * @param weather The current weather
     */
    public void weatherEffect(String weather)
    {
        if (weather.equals("sunny")){
            findFoodProbability = 1.0;
        }
        if (weather.equals("rainy")){
            findFoodProbability = 0.79;
        }
        else if (weather.equals("foggy")){
            findFoodProbability = 0.69;
        }
        else if (weather.equals("storm")){
            findFoodProbability = 0.23;
            if (rand.nextDouble() <= STORM_DEATH_PROBABILITY){
                setDead();
            }
        }
    }
}