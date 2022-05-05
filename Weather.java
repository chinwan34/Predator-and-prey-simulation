import java.util.Random;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * A simple model of weather
 * Different weather's appear probability, matain time
 * 
 * @author Bowen Zhu and Chin Wan
 * @version 2022.03.01(3)
 */
public class Weather
{
    // List of weathers that could be appear
    private List<String>weathersList;
    // The status of current weather
    private int weatherStatus;
    // The duration time of the current weather
    private int weatherDuration = 1;
    // A shared random number generator.
    private Random rand = Randomizer.getRandom();
    
    // The probability that the weather is foggy.
    private static final double FOGGY_APPEAR_PROBABILITY = 0.12;
    // The probability that the weather is rainy.
    private static final double RAINY_APPEAR_PROBABILITY = 0.05;
    // The probability that the weather is storm.
    private static final double STORM_APPEAR_PROBABILITY = 0.005;
    // Maximum number of steps the weather will maintain in one time.
    private static final int MAX_WEATHER_DURATION = 7;
    
    /**
     * Construct a list of different weathers
     */
    public Weather()
    {
        weathersList = new ArrayList<>(Arrays.asList("sunny", "rainy", "foggy", "storm"));
    }
    
    /**
     * Determine the current weather base on each weather's probability
     * if it's not any of the three(rainy, foggy, storm), then the weather
     * is sunny.
     * @return the current weather.
     */
    public void generateWeather() {
        incrementDuration();
        if(weatherDuration == 0) {
            if (rand.nextDouble() < STORM_APPEAR_PROBABILITY) {
                weatherStatus = 3;
                weatherDuration();
            }
            else if (rand.nextDouble() < FOGGY_APPEAR_PROBABILITY) {
                weatherStatus = 2;
                weatherDuration();
            }
            else if (rand.nextDouble() < RAINY_APPEAR_PROBABILITY) {
                weatherStatus = 1;
                weatherDuration();
            }
            else {
                weatherStatus = 0;
                weatherDuration();
            }
        }
    }
    
    /**
     * Generate a number representing the total duration time of the
     * current weather type.
     * @return the number of weather duration time in step (between 1 and MAX_WEATHER_DURATION).
     */
    private void weatherDuration() {
        Random rand = Randomizer.getRandom();
        weatherDuration = rand.nextInt(MAX_WEATHER_DURATION) + 1;
    }
    
    /**
     * Decrease the duration, when it is 0, change the weather.(Could be the same as last one).
     */
    private void incrementDuration() {
        weatherDuration--;
    }
    
    /**
     * Return the name of the current weather.
     * @param The status of the weather.
     * @return the name of the current weather.
     */
    public String getWeather() {
        return weathersList.get(weatherStatus);
    }
    
    /**
     * Return the remain duration of the current weather.
     * @return the remain duration of the current weather.
     */
    public int getWeatherDuration() {
        return weatherDuration;
    }
    
}
