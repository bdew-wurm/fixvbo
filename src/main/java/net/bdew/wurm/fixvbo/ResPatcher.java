package net.bdew.wurm.fixvbo;

public class ResPatcher {
    public static String filterResource(String resource, String data) {
        if (resource.equals("shader.forward_dirlight.fragment")) {
            data = data.replaceAll("if\\s*\\(col\\.a\\s*<\\s*0\\.8\\)\\s*discard;", "");
            FixVBOMod.logInfo(String.format("Loaded resource: %s -> %s", resource, data));
        }
        return data;
    }
}
