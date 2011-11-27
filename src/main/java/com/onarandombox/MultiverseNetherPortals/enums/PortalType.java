package com.onarandombox.MultiverseNetherPortals.enums;

import java.util.HashMap;
import java.util.Map;

public enum PortalType {
    NETHER,
    END;

    private static final Map<String, PortalType> lookupNames;

    public static PortalType parse(String s) {
        return lookupNames.get(s.toLowerCase());
    }

    public String toString() {
        return super.toString().toLowerCase();
    }

    static {
        lookupNames = new HashMap<String, PortalType>();

        for (PortalType t : PortalType.values()) {
            lookupNames.put(t.toString(), t);
        }
    }
}
