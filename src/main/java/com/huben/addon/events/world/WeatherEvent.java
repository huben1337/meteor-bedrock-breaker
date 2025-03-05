package com.huben.addon.events.world;

public class WeatherEvent {
    public static class Rain {
        static final Rain INSTANCE = new Rain();
       
        public static Rain get() {
            return INSTANCE;
        }
    }
    public static class Thunder {
        static final Thunder INSTANCE = new Thunder();
        
        public static Thunder get() {
            return INSTANCE;
        }
    }
    public static class Clear {
        static final Clear INSTANCE = new Clear();
        
        public static Clear get() {
            return INSTANCE;
        }
    }

    public static class None {
        static final None INSTANCE = new None();
        
        public static None get() {
            return INSTANCE;
        }
    }
}
