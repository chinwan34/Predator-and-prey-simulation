import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a Mouse.
 * Mice age, move, breed, and die.
 * 
 * @author Bowen Zhu (k21083430) and Chin Wan (k21016106)
 * @version 2022.03.01 (2)
 */
public class Mouse extends Animal
{
    // Characteristics shared by all Mice (class variables).

    // The age at which a Mouse can start to breed.
    private static final int BREEDING_AGE = 19;
    // The age to which a Mouse can live.
    private static final int MAX_AGE = 65;//
    // The likelihood of a Mouse breeding.
    private static final double BREEDING_PROBABILITY = 0.84;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 8;
    // Maximum number of steps a Mouse can go before it has to eat again.
    private static final int MAX_FOOD_LEVEL = 17;
    // Probability of acting at night.
    private static final double ACTING_PROBABILITY_NIGHT = 0.79;
    // food value predator will get, if it eat this Mouse.
    private static final int FOOD_VALUE = 5;
    // The likelihood of a mouse dying from the storm.
    private static final double STORM_DEATH_PROBABILITY = 0.05;
    // A shared random number generator.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).
    
    // The Mouse's age.
    private int age;
    // The Mouse's food level, which is increased by eating plants.
    private int foodLevel;
    // The likelihood of an eagle to find food
    private double findFoodProbability = 1.0;

    /**
     * Create a new Mouse. A Mouse may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the Mouse will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Mouse(boolean randomAge, Field field, Location location)
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
        //set Gender for new Mouse
        setGender();
    }
    
    /**
     * This is what the Mouse does most of the time - it runs 
     * around. Sometimes it will breed or die of old age, or die of disease.
     * @param newMice A list to return newly born Mice.
     */
    public void act(List<Animal> newMice, String weather)
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
                        giveBirth(newMice);  
                    }
                }
                
                newLocation();
            }
        }
    }
    
    /**
     * Mice act less frequently at night, it will not naturelly infect disease
     * at night. Age increment as usual and might die of hunger 
     * or die of age or infect disease from other animals nearby and 
     * might also die of disease is well.
     * @param field The field currently occupied.
     * @param newWolves A list to return newly born Mice.
     */
    public void nightAct(List<Animal> newMice, String weather)
    {
        incrementAge();
        incrementHunger();   
        if (rand.nextDouble() < ACTING_PROBABILITY_NIGHT) {
            if(isAlive()) {
                if (genderDetect() == true) {
                    if (getGender()) {
                        giveBirth(newMice);  
                    }
                }
                newLocation();
            }
        }
    }
    
    /**
     * Find the new Location for mice, if there's plants near by, eat the plant and 
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
     * This could result in the Mouse's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this Mouse more hungry. This could result in the Mouse's death.
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
     * Check whether or not this Mouse is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newMice A list to return newly born Mice.
     */
    private void giveBirth(List<Animal> newMice)
    {
        // New Mice are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Mouse young = new Mouse(false, field, loc);
            newMice.add(young);
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
     * A Mouse can breed if it has reached the breeding age.
     * @return true if the Mouse can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Return the Mouse's food Value
     * @return the Mouse's food Value
     */
    public int getFoodValue() {
        return FOOD_VALUE;
    }
    
    /**
     * The effect of the weather on the mouse
     * @param weather The current weather
     */
    public void weatherEffect(String weather)
    {
        if (weather.equals("sunny")){
            findFoodProbability = 0.8;
        }
        if (weather.equals("rainy")){
            findFoodProbability = 0.75;
        }
        else if (weather.equals("foggy")){
            findFoodProbability = 0.69;
        }
        else if (weather.equals("storm")){
            findFoodProbability = 0.29;
            if (rand.nextDouble() <= STORM_DEATH_PROBABILITY){
                setDead();
            }
        }
    }
}
