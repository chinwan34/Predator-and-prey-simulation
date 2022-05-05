import java.util.Random;
import java.util.List;

/**
 * Write a description of class Plants here.
 *
 * @author Bowen Zhu (k21083430) and Chin Wan (k21016106)
 * @version 2022.03.01 (2)
 */
public class Rose extends Plant
{
    // Characteristics shared by all Roses
    
    // The age to which a Rose can live.
    private static final int MAX_AGE = 12;
    // Number of gap steps between its every births (spreading seeds)
    private static final int GAP_TIME_BETWEEN_BIRTHS = 1;
    // Food value prey will get, if it eat this Rose.
    private static final int MAX_FOOD_VALUE = 14;
    // A shared random number generator.
    private static final Random rand = Randomizer.getRandom();
    // The likelihood of a Rose disperse seeds.
    private static final double CREATE_SEEDS_PROBABILITY = 0.7;
    // The likelihood of a Rose die under the storm.
    private static final double STORM_DEATH_PROBABILITY = 0.12;
    
    // The Rose's food level, which is increased by time.  
    private int foodValue;
    // The Rose's age.
    private int age;
    // The Rose's growth rate, the rate of its food value groth over time.
    private int growthRate;
    // Maximum number of seeds a Rose can disperse at one time.
    private int maxSeedsSize = 9;
    /**
     * Create a new Rose. A Rose may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the Rose will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Rose(boolean randomAge,Field field, Location location)
    {
        // initialise instance variables
        super(field,location);
        age = 0;
        foodValue = 5;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodValue = rand.nextInt(MAX_FOOD_VALUE);
        }
    }

    /**
     * This is what the Rose does most of the time - it grows up.
     * Sometime it disperse its seeds.
     * @param newRoses A list to return newly born Roses.
     * @param weather String that shows current weather.
     */
    public void act(List<Plant> newRoses, String weather){
        incrementAge();
        if (isAlive()){
            weatherEffect(weather);
            if (isAlive()) {
                if (foodValue < MAX_FOOD_VALUE) {
                    foodValue = foodValue + growthRate;
                    if (foodValue > MAX_FOOD_VALUE) {
                        foodValue = MAX_FOOD_VALUE;
                    }
                }
                autoCreation(newRoses);
            }
        }
    }
    
    
    /**
     * Increase the age.
     * This could result in the Rose's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this Rose is to disperse seeds at this step.
     * New seeds will be made into free adjacent locations.
     * @param newRoses A list to return newly born Roses.
     */
    private void autoCreation(List<Plant> newRoses){
        Field field = getField();
        List<Location> free = field.getFreeNearLocations(getLocation());
        int seeds = breed();
        if (rand.nextDouble() <=  CREATE_SEEDS_PROBABILITY) {
            for(int create = 0; create < seeds && free.size() > 0; create++) {
                Location loc = free.remove(0);
                Rose young = new Rose(false, field, loc);
                newRoses.add(young);
            }
        }
    }
    
    /**
     * Generate a number representing the number of seeds,
     * if it can breed.
     * @return The number of seeds (may be zero).
     */
    private int breed()
    {
        int seeds = 0;
        if(canBreed()) {
            seeds = rand.nextInt(maxSeedsSize) + 1;
        }
        return seeds;
    }
    
    /**
     * A Rose can disperse its seeds every certain amount of years.
     * @return true if the Rose can disperse seeds, flase otherwise.
     */
    private boolean canBreed()
    {
        int gapYear = age % GAP_TIME_BETWEEN_BIRTHS;
        if(gapYear == 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Return the Rose's food Value
     * @return the Rose's food Value
     */
    public int getFoodValue(){
        return foodValue;
    }
    
    /**
     * Different weather have different effects on Rose.
     * @param weather String that shows current weather.
     */
    public void weatherEffect(String weather){
        if (weather.equals("sunny")){
            growthRate = 4;
            maxSeedsSize = 9;
        }
        if (weather.equals("rainy")){
            growthRate = 3;
            maxSeedsSize = 8;
        }
        else if (weather.equals("foggy")){
            growthRate = 3;
            maxSeedsSize = 7;
        }
        else if (weather.equals("storm")){
            growthRate = 1;
            maxSeedsSize = 2;
            if (rand.nextDouble() <= STORM_DEATH_PROBABILITY){
                setDead();
            }
        }
    }
}
