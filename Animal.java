import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A class representing shared characteristics of animals.
 * 
 * @author Bowen Zhu (k21083430) and Chin Wan (k21016106)
 * @version 2022.03.01 (2)
 */
public abstract class Animal
{
    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's field.
    private Field field;
    // The animal's position in the field.
    private Location location;
    // The animal's gender.
    private boolean gender;
    // A shared random number generator.
    private static final Random rand = Randomizer.getRandom();
    // Gender value use to help randomlly allocate gender. 
    private static final int GENDER_VALUE = 2;
    
    //Below are for diseases
    // Show whether this animal is infected or not. True(is infected) False(not yet infected)
    private boolean exist;
    // Disease's current infect time.
    private int infectTime;
    // The likelihood of naturelly getting disease.
    private static final double APPEAR_PROBABILITY = 0.03;
    // The likelihood of getting disease when nearby a infected animal.
    private static final double INFECT_PROBABILITY = 0.12;
    // The likelihood of dying when infected.
    private static final double DIE_PROBABILITY = 0.07;
    // Maximum amout of time to be infected in step.
    private static final int MAX_INFECT_TIME = 10;
    
    /**
     * Create a new animal at location in field.
     * 
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Animal(Field field, Location location)
    {
        alive = true;
        this.field = field;
        setLocation(location);
    }
    
    /**
     * Make this animal act - that is: make it do
     * whatever it wants/needs to do.
     * @param newAnimals A list to receive newly born animals.
     */
    abstract public void act(List<Animal> newAnimals, String weather);
    
    /**
     * Make this animal act - that is: make it do
     * whatever it wants/needs to do but it's the night version.
     * @param newAnimals A list to receive newly born animals.
     */
    abstract public void nightAct(List<Animal> newAnimals, String weather);
    
    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    protected boolean isAlive()
    {
        return alive;
    }
    
    /**
     * Indicate that the animal is no longer alive.
     * It is removed from the field.
     */
    protected void setDead()
    {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }

    /**
     * Return the animal's location.
     * @return The animal's location.
     */
    protected Location getLocation()
    {
        return location;
    }
    
    /**
     * Place the animal at the new location in the given field.
     * @param newLocation The animal's new location.
     */
    protected void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
    
    /**
     * Return the animal's field.
     * @return The animal's field.
     */
    protected Field getField()
    {
        return field;
    }
    
    /**
     * Set animal's gender. (True for female, False for male).
     */
    protected void setGender()
    {
        int genderInt = rand.nextInt(GENDER_VALUE);
        if(genderInt == 1) { //1 for Female, 0 for Male
            gender = true; // Female
        }
        else {
            gender = false; //Male
        }
    }
    
    /**
     * Return the animal's gender
     * @return the animal's gender.
     */
    protected boolean getGender()
    {
        return gender;
    }
    
    
    //below are for diseases
    /**
     * Return the status of disease.
     * @return the status of disease.
     */
    protected boolean isExist()
    {
        return exist;
    }
    
    /**
     * Indicate that the disease is no longer exist.
     * The animal is healthy now.
     */
    protected void setDisappear()
    {
        exist = false;
    }
    
    /**
     * Indicate that the disease is now exist.
     * The animal is infected.
     */
    protected void setExist()
    {
        exist = true;
    }
    
    /**
     * Set the total infect time of the disease, indicate how long the animal will need to
     * carry this disease.
     */
    protected void setInfectTime() {
        infectTime = rand.nextInt(MAX_INFECT_TIME) + 1;
    }
    
    /**
     * The infect time increase, if it reach 0, then the disease is going to disappear.
     */
    protected void incrementTime()
    {
        infectTime--;
        if(infectTime == 0) {
            setDisappear();
        }
    }
    
    /**
     * Look for animals adjacent to the current location
     * Infect animal surround.
     */
    protected void infectHost()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it1 = adjacent.iterator();
        while(it1.hasNext()) {
            Location where = it1.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.isAlive() && willInfect()) { 
                    rabbit.setExist();
                    rabbit.setInfectTime();
                }
            }
            else if(animal instanceof Deer) {
                Deer deer = (Deer) animal;
                if(deer.isAlive() && willInfect()) { 
                    deer.setExist();
                    deer.setInfectTime();
                }
            }
            else if(animal instanceof Mouse) {
                Mouse mouse = (Mouse) animal;
                if(mouse.isAlive() && willInfect()) { 
                    mouse.setExist();
                    mouse.setInfectTime();
                }
            }
            else if(animal instanceof Fox) {
                Fox fox = (Fox) animal;
                if(fox.isAlive() && willInfect()) { 
                    fox.setExist();
                    fox.setInfectTime();
                }
            }
            else if(animal instanceof Wolf) {
                Wolf wolf = (Wolf) animal;
                if(wolf.isAlive() && willInfect()) { 
                    wolf.setExist();
                    wolf.setInfectTime();
                }
            }
            else if(animal instanceof Eagle) {
                Eagle eagle = (Eagle) animal;
                if(eagle.isAlive() && willInfect()) { 
                    eagle.setExist();
                    eagle.setInfectTime();
                }
            }
        }
    }
    
    /**
     * Determine whether the animal is going to infect disease
     * apply random double and compare to the infect porbability.
     */
    protected boolean willInfect()
    {
        if(rand.nextDouble() <= INFECT_PROBABILITY) {
            return true;
        } 
        return false;
    }
    
    /**
     * Look for animals adjacent to see if they are in the same class with the animal
     * at current location, if yes then compare their gender. If they have the different gender, return true
     * if none of the animals adjacent have different gender, then return false.
     * @return True when found a different gender animal, or False when didn't find any.
     */
    protected boolean genderDetect()
    {
        Field field = getField();
        List<Location> adjacent = field.nearLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Object animalItSelf = field.getObjectAt(getLocation());
        if(animalItSelf instanceof Rabbit) {
                while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Rabbit) {
                    Rabbit rabbit = (Rabbit) animal;
                    boolean gender1 = getGender();
                    boolean gender2 = rabbit.getGender();
                    if(gender1 != gender2) {
                        return true;
                    }
                }
            }
            return false;    
        }
        else if(animalItSelf instanceof Deer) {
                while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Deer) {
                    Deer deer = (Deer) animal;
                    boolean gender1 = getGender();
                    boolean gender2 = deer.getGender();
                    if(gender1 != gender2) {
                        return true;
                    }
                }
            }
            return false;    
        }
        else if(animalItSelf instanceof Mouse) {
                while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Mouse) {
                    Mouse mouse = (Mouse) animal;
                    boolean gender1 = getGender();
                    boolean gender2 = mouse.getGender();
                    if(gender1 != gender2) {
                        return true;
                    }
                }
            }
            return false;    
        }
        else if(animalItSelf instanceof Fox) {
                while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Fox) {
                    Fox fox = (Fox) animal;
                    boolean gender1 = getGender();
                    boolean gender2 = fox.getGender();
                    if(gender1 != gender2) {
                        return true;
                    }
                }
            }
            return false;    
        }
        else if(animalItSelf instanceof Wolf) {
                while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Wolf) {
                    Wolf wolf = (Wolf) animal;
                    boolean gender1 = getGender();
                    boolean gender2 = wolf.getGender();
                    if(gender1 != gender2) {
                        return true;
                    }
                }
            }
            return false;    
        }
        else if(animalItSelf instanceof Eagle) {
                while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if(animal instanceof Eagle) {
                    Eagle eagle = (Eagle) animal;
                    boolean gender1 = getGender();
                    boolean gender2 = eagle.getGender();
                    if(gender1 != gender2) {
                        return true;
                    }
                }
            }
            return false;    
        }
        return false;
    }
    
    /**
     * Check if this animal is going to die from disease.
     */
    protected void willDie()
    {
        if(rand.nextDouble() <= DIE_PROBABILITY) {
            Object animal = getField().getObjectAt(getLocation());
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                }
            }
            else if(animal instanceof Deer) {
                Deer deer = (Deer) animal;
                if(deer.isAlive()) { 
                    deer.setDead();
                }
            }
            else if(animal instanceof Mouse) {
                Mouse mouse = (Mouse) animal;
                if(mouse.isAlive()) { 
                    mouse.setDead();
                }
            }
            else if(animal instanceof Fox) {
                Fox fox = (Fox) animal;
                if(fox.isAlive()) { 
                    fox.setDead();
                }
            }
            else if(animal instanceof Wolf) {
                Wolf wolf = (Wolf) animal;
                if(wolf.isAlive()) { 
                     wolf.setDead();
                }
            }
            else if(animal instanceof Eagle) {
                Eagle eagle = (Eagle) animal;
                if(eagle.isAlive()) { 
                    eagle.setDead();
                }
            }
        }
    }
    
    /**
     * If this animal meet the probability, then it naturelly get the disease.
     */
    protected void diseaseAppear()
    {
        if(rand.nextDouble() <= APPEAR_PROBABILITY) {
            setExist();
            setInfectTime();
        }
    }
}
