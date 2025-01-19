package com.onarandombox.MultiverseNetherPortals.utils;

public class ClassChecker {
    public static final boolean isTravelAgentExists = isClassLoaded("org.bukkit.TravelAgent");

    /**
     * Checks if a class is loaded
     * @param className The full-qualified name of the class
     * @return true if the class is loaded, false otherwise
     */
    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
