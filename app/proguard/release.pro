-dontobfuscate

-assumenosideeffects class android.util.Log {
    public static int d(...);
}

-dontwarn javax.annotation.processing.Processor
-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedOptions
