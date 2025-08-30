public class Begin {
    public static void main(String[] args) {
        // Simple vehicle hierarchy demonstration
        Vehicle car = new Car("Toyota", "Camry", 4);
        Vehicle bike = new Bike("Yamaha", "MT-07", false);
        
        car.displayInfo();
        bike.displayInfo();
    }
}

class Vehicle {
    protected String brand;
    protected String model;
    
    public Vehicle(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }
    
    public void displayInfo() {
        System.out.println("Vehicle: " + brand + " " + model);
    }
}

class Car extends Vehicle {
    private int doors;
    
    public Car(String brand, String model, int doors) {
        super(brand, model);
        this.doors = doors;
    }
    
    @Override
    public void displayInfo() {
        System.out.println("Car: " + brand + " " + model + " with " + doors + " doors");
    }
}

class Bike extends Vehicle {
    private boolean hasCarrier;
    
    public Bike(String brand, String model, boolean hasCarrier) {
        super(brand, model);
        this.hasCarrier = hasCarrier;
    }
    
    @Override
    public void displayInfo() {
        System.out.println("Bike: " + brand + " " + model + 
                          (hasCarrier ? " with carrier" : " without carrier"));
    }
}