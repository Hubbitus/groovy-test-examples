import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
 
public class WeatherStation {
    public static void main(String[] args){
        WeatherData weatherData = new WeatherData();
 
        CurrentConditionsDisplay currentDisplay = new CurrentConditionsDisplay(weatherData);
        weatherData.setMeasurements(29, 65);
        weatherData.setMeasurements(39, 70);
        weatherData.setMeasurements(42, 72);
    }
}
 
class WeatherData extends Observable{
    public float temperature;
    public float humidity;
 
    public void setMeasurements(float temperature, float humidity){
        this.temperature = temperature;
        this.humidity = humidity;
        setChanged();
        notifyObservers(this);
    };
}
 
class CurrentConditionsDisplay implements Observer{
    private float temperature;
    private float humidity;
    private Observable weatherData;
 
    public CurrentConditionsDisplay(Observable weatherData){
        this.weatherData = weatherData;
        weatherData.addObserver(this);
    }
 
    @Override
    void update(java.util.Observable observable, java.lang.Object data){
        printf("Сейчас значения: %.1f градусов цельсия и %.1f %% влажности\n", data.temperature, data.humidity);
        println observable
    }
}

