package dev.despical.particletext.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VersionComparator {

    public static boolean isNewer(String candidate, String current) {
        int[] candidateParts = parts(candidate);
        int[] currentParts = parts(current);
        int length = Math.max(candidateParts.length, currentParts.length);

        for (int index = 0; index < length; index++) {
            int candidatePart = index < candidateParts.length ? candidateParts[index] : 0;
            int currentPart = index < currentParts.length ? currentParts[index] : 0;

            if (candidatePart != currentPart) {
                return candidatePart > currentPart;
            }
        }

        return false;
    }

    private static int[] parts(String version) {
        String normalized = version == null ? "" : version.strip().replaceFirst("^[vV]", "");
        String[] tokens = normalized.split("[.-]");
        int[] parts = new int[tokens.length];

        for (int index = 0; index < tokens.length; index++) {
            String digits = tokens[index].replaceFirst("^(\\d+).*$", "$1");

            if (!digits.matches("\\d+")) {
                parts[index] = 0;
                continue;
            }

            try {
                parts[index] = Integer.parseInt(digits);
            } catch (NumberFormatException _) {
                parts[index] = Integer.MAX_VALUE;
            }
        }

        return parts;
    }
}
