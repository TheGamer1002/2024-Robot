package frc.robot;

public final class Constants {
    public static final double stickDeadband = 0.1; 
    public static final class Drive {
        /* Drive Constants */
        public static final int wheelDiameter = 4;
        public static final double neoDriveGearRatio = 6.12;
        public static final double angleGearRatio = 21.4286;
        public static final double encoderResolution = 42;
        public static final double falconDriveGearRatio = 6.75;
        public static final boolean isNeo = true; // SET TO FALSE FOR FALCON
        public static final int leftXSign = isNeo ? -1 : 1; // Inverts the controllers leftX sign if we're using a Neo
        public static final String pathPlannerFile = isNeo ? "swerve/neo" : "swerve/falcon";
        public static final double driveGearRatio = isNeo ? neoDriveGearRatio : falconDriveGearRatio;
    }
    public static final class Intake {
        public static final double OFF = 0.0;
        public static final double ON = 0.7;
        public static final double REVERSE = -0.7;
        public static final double DOWN = -37.5;
    }
}
